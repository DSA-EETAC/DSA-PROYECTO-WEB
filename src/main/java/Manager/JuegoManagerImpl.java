package Manager;

import BDD.orm.FactorySession;
import BDD.orm.Session;
import BDD.orm.dao.IItemDAO;
import BDD.orm.dao.IUsuarioDAO;
import BDD.orm.dao.ItemDAOImpl;
import BDD.orm.dao.UsuarioDAOImpl;
import Model.DetalleGrupo;
import Model.Item;
import Model.User;
import Model.Grupo;
import org.apache.log4j.Logger;
import java.util.*;


public class JuegoManagerImpl implements JuegoManager {

    private static JuegoManager instance;
    private static Session conn;
    final static Logger log = Logger.getLogger(JuegoManagerImpl.class.getName());

    // Estructuras de datos
    private IUsuarioDAO usuarioDAO;
    private IItemDAO itemDAO;
    // Constructor privado
    private JuegoManagerImpl() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.itemDAO = new ItemDAOImpl();
    }

    public static JuegoManager getInstance() {
        if (instance == null) {
            instance = new JuegoManagerImpl();
            log.info("Nueva instancia creada.");
        }
        return instance;
    }

    @Override
    public boolean registrarUsuario(User u) {
        log.info("INICIO registrarUsuario: nombre=" + u.getNombre());

        if (usuarioDAO.getUsuario(u.getNombre()) != null) {
            log.warn("FIN registrarUsuario: ERROR - El usuario ya existe.");
            return false;
        }

        usuarioDAO.addUsuario(u.getNombre(), u.getPassword(), u.getMail());
        log.info("FIN registrarUsuario: Usuario registrado correctamente.");
        return true;
    }

    @Override
    public User consultarUsuario(String nombre) {
        log.info("Consulta usuario: " + nombre);
        return usuarioDAO.getUsuario(nombre);    }

    @Override
    public User procesarLogin(String nombre, String password) {
        log.info("INICIO procesarLogin: nombre=" + nombre);
        User u = usuarioDAO.getUsuario(nombre);
        if (u != null && u.getPassword().equals(password)) {
            log.info("FIN procesarLogin: Login exitoso.");

            return u;
        }
        return null;
    }
    @Override
    public boolean comprarObjeto(String nombreJugador, String nombreObjeto) {
        User jugador = usuarioDAO.getUsuario(nombreJugador);

        // ¡OJO! Tu ItemDAOImpl no tiene getItemPorNombre, usa el metodo genérico getItem
        Item item = itemDAO.getItem("nombre", nombreObjeto);

        if (jugador != null && item != null) {
            if (jugador.getMonedas() >= item.getPrecio()) {

                // Casteamos a (int) porque tu item.getPrecio() devuelve double
                jugador.setMonedas(jugador.getMonedas() - (int)item.getPrecio());

                // Guardamos en Base de Datos
                usuarioDAO.updateUsuario(jugador);
                usuarioDAO.añadirItemAInventario(jugador.getId(), item.getId());

                return true;
            }
        }
        return false;
    }
    @Override
    public List<String> obtenerInventarioUsuario(int idUsuario) {
        // Llamamos directamente al metodo del DAO que recibe el userId
        List<String> items = usuarioDAO.getNombresItemsUsuario(idUsuario);

        // Si el DAO devuelve null (por si acaso), devolvemos una lista vacía para evitar errores
        if (items == null) {
            return new ArrayList<>();
        }
        return items;
    }

    @Override
    public int obtenerNumeroUsuarios() {
        List<User> lista = usuarioDAO.getUsuarios();
        return (lista != null) ? lista.size() : 0;
    }
    @Override
    public List<User> obtenerUsuarios(){
        List<User> lista = usuarioDAO.getUsuarios();
        return lista;
    }

    @Override
    public void clear() {
    }

    @Override
    public List<Item> obtenerItemsTienda() {
        System.out.println(this.itemDAO.getItems());
        return this.itemDAO.getItems();
    }

    @Override
    public List<Grupo> obtenerGrupos() {
        Session session = null;
        List<Grupo> listaGrupos = new ArrayList<>();
        try {
            session = FactorySession.openSession();

            // Usamos tu ORM para traer todos los registros de la tabla Grupo
            List<Object> objetos = session.findAll(Grupo.class);

            if (objetos != null) {
                for (Object obj : objetos) {
                    listaGrupos.add((Grupo) obj); // Casteamos de Object a Grupo
                }
            }
        } catch (Exception e) {
            log.error("Error al obtener los grupos: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return listaGrupos;
    }

    @Override
    public boolean unirUsuarioAGrupo(int idUsuario, int  idGrupo) {
        log.info("INICIO unirUsuarioAGrupo: usuario=" + idUsuario + ", grupo=" + idGrupo);

        try {
            // 1. Obtenemos el usuario real desde el DAO
            User u = usuarioDAO.getUsuario(idUsuario);

            if (u != null) {
                // 2. Le asignamos el ID del grupo (convirtiéndolo de String a int)
                u.setId_grupo(idGrupo);

                // 3. Guardamos los cambios usando el DAO (igual que haces en comprarObjeto)
                usuarioDAO.updateUsuario(u);

                log.info("FIN unirUsuarioAGrupo: Usuario " + idUsuario + " unido al grupo " + idGrupo + " con éxito.");
                return true;
            } else {
                log.warn("FIN unirUsuarioAGrupo: El usuario " + idUsuario + " no existe.");
            }
        } catch (Exception e) {
            log.error("Error al unir usuario al grupo: ", e);
        }

        return false;
    }
    @Override
    public DetalleGrupo obtenerDetalleGrupoUsuario(int id) {
        log.info("Buscando grupo y miembros para el usuario: " +  id);

        User u = usuarioDAO.getUsuario(id);

        // Si el usuario no existe, o su id_grupo es null o es 0, significa que no tiene grupo
        if (u == null || u.getId_grupo() < 0 || u.getId_grupo() == 0) {
            return new DetalleGrupo(); // Devuelve el objeto con tieneGrupo = false
        }

        int idGrupoDelUsuario = u.getId_grupo();
        String nombreGrupo = "Equipo " + idGrupoDelUsuario; // Nombre por defecto

        // 1. Conseguimos el nombre real del grupo buscando en tu lista de grupos existente
        List<Grupo> listaGrupos = obtenerGrupos();
        for (Grupo g : listaGrupos) {
            if (g.getId() == idGrupoDelUsuario) { // Asumiendo que tu clase Grupo tiene getId() y getNombre()
                nombreGrupo = g.getNombre();
                break;
            }
        }

        // 2. Conseguimos los nombres de los miembros filtrando tu lista de usuarios existente
        List<User> todosLosUsuarios = obtenerUsuarios();
        List<String> nombresMiembros = new ArrayList<>();
        for (User user : todosLosUsuarios) {
            if (user.getId_grupo() > 0 && user.getId_grupo() == idGrupoDelUsuario) {
                nombresMiembros.add(user.getNombre());
            }
        }

        // Devolvemos el detalle completo preparado para el Frontend
        return new DetalleGrupo(nombreGrupo, nombresMiembros);
    }
}






