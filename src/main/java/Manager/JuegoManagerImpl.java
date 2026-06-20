package Manager;

import BDD.orm.FactorySession;
import BDD.orm.Session;
import BDD.orm.dao.IItemDAO;
import BDD.orm.dao.IUsuarioDAO;
import BDD.orm.dao.ItemDAOImpl;
import BDD.orm.dao.UsuarioDAOImpl;
import Model.*;
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
    @Override
    public java.util.List<Model.Evento> obtenerListaEventos(){

        Session session = null;
        List<Model.Evento> listaEventos = new ArrayList<>();
        try {
            session = FactorySession.openSession();
            // el ORM busca todos los registros de la tabla 'evento'
            List<Object> objetos = session.findAll(Model.Evento.class);

            if (objetos != null) {
                for (Object obj : objetos) {
                    listaEventos.add((Model.Evento) obj);
                }
            }
        } catch (Exception e) {
            log.error("Error al obtener los eventos desde la BD: ", e);
        } finally {
            if (session != null) session.close();
        }
        return listaEventos;
    }

    @Override
    public boolean registrarInscripcion (InscripcionRequest request){
        Session session = null;
        try {
            // buscamos al usuario para obtener su ID numérico real
            User u = usuarioDAO.getUsuario(request.getUsername());
            if (u == null) return false;

            session = FactorySession.openSession();

            // creamos el objeto puente y lo guardamos con el ORM
            InscripcionEvento nuevaInscripcion = new InscripcionEvento(u.getId(), request.getIdEvento());
            session.save(nuevaInscripcion);

            log.info("Inscripción real guardada en BD: Usuario " + u.getId() + " en " + request.getIdEvento());
            return true;
        } catch (Exception e) {
            log.error("Error al inscribir en evento (¿Quizás ya estaba inscrito?): ", e);
            return false;
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public boolean sumarMonedas(String nombreJugador, int cantidad){
        log.info("INICIO sumarMonedas: Añadiendo " + cantidad + " a " + nombreJugador);
        User jugador = usuarioDAO.getUsuario(nombreJugador);

        if (jugador != null) {
            // Sumamos las monedas al saldo actual
            jugador.setMonedas(jugador.getMonedas() + cantidad);

            // Guardamos el nuevo saldo en la base de datos
            usuarioDAO.updateUsuario(jugador);
            log.info("FIN sumarMonedas: Saldo actualizado correctamente.");
            return true;
        }
        return false;
    }
    @Override
    public List<String> obtenerInventarioUsuarioPorNombre(String nombreUsuario) {
        // 1. Buscamos el usuario por nombre para obtener su ID real
        User u = usuarioDAO.getUsuario(nombreUsuario);
        if (u != null) {
            return obtenerInventarioUsuario(u.getId());
        }
        return new ArrayList<>();
    }

    @Override
    public DetalleGrupo obtenerDetalleGrupoUsuarioPorNombre(String nombre) {
        User u = usuarioDAO.getUsuario(nombre);
        if (u != null) {
            return obtenerDetalleGrupoUsuario(u.getId());
        }
        return new DetalleGrupo();
    }
    @Override
    public boolean repartirPremios(String idEvento) {

        List<JugadorRanking> ranking = obtenerRankingEvento(idEvento);
        // Reparticion de premios
        if (ranking.size() > 0) {
            sumarMonedas(ranking.get(0).getNombreJugador(), 1000); // 1º Puesto
        }
        if (ranking.size() > 1) {
            sumarMonedas(ranking.get(1).getNombreJugador(), 500);  // 2º Puesto
        }
        if (ranking.size() > 2) {
            sumarMonedas(ranking.get(2).getNombreJugador(), 100);  // 3º Puesto
        }

        return true;
    }
    @Override
    public List<JugadorRanking> obtenerRankingEvento(String idEvento) {
        Session session = null;
        List<JugadorRanking> ranking = new ArrayList<>();
        try {
            session = FactorySession.openSession();

            // Traemos todas las inscripciones (dependiendo de tu ORM, lo ideal es un filtro en BD,
            // pero esto funciona filtrando en Java)
            List<Object> objetos = session.findAll(InscripcionEvento.class);

            if (objetos != null) {
                for (Object obj : objetos) {
                    InscripcionEvento inscripcion = (InscripcionEvento) obj;

                    // Solo nos interesan los que están apuntados a ESTE evento
                    if (inscripcion.getEvento_id().equals(idEvento)) {
                        // Buscamos el nombre del usuario a partir de su ID
                        User u = usuarioDAO.getUsuario(inscripcion.getUser_id());
                        if (u != null) {
                            ranking.add(new JugadorRanking(u.getNombre(), inscripcion.getPuntuacion()));
                        }
                    }
                }
            }

            // Ordenamos la lista de mayor a menor puntuación
            ranking.sort((j1, j2) -> Integer.compare(j2.getPuntuacion(), j1.getPuntuacion()));

        } catch (Exception e) {
            log.error("Error al obtener ranking del evento: ", e);
        } finally {
            if (session != null) session.close();
        }
        return ranking;
    }
    @Override
    public boolean sumarPuntosAInscripcion(String idEvento, String nombreJugador, int puntosNuevos) {
        Session session = null;
        try {
            //Buscamos al usuario para obtener su ID real
            User u = usuarioDAO.getUsuario(nombreJugador);
            if (u == null) {
                log.warn("sumarPuntos: El jugador " + nombreJugador + " no existe.");
                return false;
            }

            session = FactorySession.openSession();

            // Obtenemos todas las inscripciones (filtraremos en Java)
            List<Object> objetos = session.findAll(InscripcionEvento.class);

            if (objetos != null) {
                for (Object obj : objetos) {
                    InscripcionEvento inscripcion = (InscripcionEvento) obj;

                    if (inscripcion.getEvento_id().equals(idEvento) && inscripcion.getUser_id() == u.getId()) {

                        int nuevaPuntuacion = inscripcion.getPuntuacion() + puntosNuevos;
                        inscripcion.setPuntuacion(nuevaPuntuacion);

                        // Actualizamos el registro en MariaDB usando tu ORM
                        session.update(inscripcion);

                        log.info("¡Puntos sumados! " + nombreJugador + " ahora tiene " + nuevaPuntuacion + " pts en " + idEvento);
                        return true;
                    }
                }
            }
            log.warn("sumarPuntos: No se encontró la inscripción del jugador " + nombreJugador + " en el evento " + idEvento);

        } catch (Exception e) {
            log.error("Error crítico al sumar puntos en el evento: ", e);
        } finally {
            if (session != null) session.close();
        }
        return false;
    }

    @Override
    public boolean abandonarEvento(String username, String idEvento) {
        log.info("INICIO abandonarEvento: Usuario=" + username + " | Evento=" + idEvento);

        Session session = null;
        try {
            session = FactorySession.openSession();

            // 1. Buscamos el ID real del usuario en MariaDB usando su nombre
            User usuario = (User) session.get(User.class, "nombre", username);
            if (usuario == null) {
                log.warn("El usuario " + username + " no existe.");
                return false;
            }

            // 2. Fabricamos una inscripción "señuelo" con las IDs clave
            Model.InscripcionEvento inscripcion = new Model.InscripcionEvento();
            inscripcion.setUser_id(usuario.getId());
            inscripcion.setEvento_id(idEvento);

            // 3. Le decimos al ORM que la fulmine (usando el delete que arreglamos antes)
            session.delete(inscripcion);

            log.info("FIN abandonarEvento: Inscripción borrada correctamente.");
            return true;

        } catch (Exception e) {
            log.error("Error crítico en abandonarEvento", e);
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<User> obtenerUsuariosEvento(String idEvento) {
        Session session = null;
        List<User> usuariosEvento = new ArrayList<>();
        try {
            session = FactorySession.openSession();

            List<Object> objetos = session.findAll(InscripcionEvento.class);

            if (objetos != null) {
                for (Object obj : objetos) {
                    InscripcionEvento inscripcion = (InscripcionEvento) obj;

                    if (inscripcion.getEvento_id().equals(idEvento)) {
                        User u = usuarioDAO.getUsuario(inscripcion.getUser_id());
                        if (u != null) {
                            usuariosEvento.add(u);
                        }
                    }
                }
            }
            log.info("Usuarios del evento " + idEvento + ": " + usuariosEvento.size());

        } catch (Exception e) {
            log.error("Error al obtener usuarios del evento: ", e);
        } finally {
            if (session != null) session.close();
        }
        return usuariosEvento;
    }

    @Override
    public boolean procesarFinPartida(FinPartidaVO datos) {
        log.info("Procesando fin de partida para: " + datos.getUsername());
        Session session = null;

        try {
            session = FactorySession.openSession();

            // 1. Buscar al usuario
            User usuario = (User) session.get(User.class, "nombre", datos.getUsername());
            if (usuario == null) {
                log.warn("Usuario no encontrado.");
                return false;
            }

            // 2. Sumar el dinero/puntuación al usuario
            usuario.setMonedas(usuario.getMonedas() + datos.getMonedasGanadas());
            session.update(usuario);

            // 3. Guardar el récord en el historial
            Partida nuevaPartida = new Partida(usuario.getId(), datos.getMonedasGanadas());
            session.save(nuevaPartida);

            // 4. Eliminar los objetos consumidos del inventario
            if (datos.getObjetosConsumidos() != null && !datos.getObjetosConsumidos().isEmpty()) {
                List<Inventario> inventarioCompleto = session.findAll(Inventario.class);

                for (Integer itemId : datos.getObjetosConsumidos()) {
                    for (Inventario inv : inventarioCompleto) {
                        // Buscamos si el usuario tiene ese objeto
                        if (inv.getUser_id() == usuario.getId() && inv.getItem_id() == itemId) {
                            if (inv.getQuantity() > 1) {
                                // Si tiene más de 1, le restamos 1
                                inv.setQuantity(inv.getQuantity() - 1);
                                session.update(inv);
                            } else {
                                // Si solo le queda 1, borramos la fila entera
                                session.delete(inv);
                            }
                            break; // Objeto restado, pasamos al siguiente
                        }
                    }
                }
            }

            log.info("Partida procesada con éxito.");
            return true;

        } catch (Exception e) {
            log.error("Error al procesar la partida", e);
            return false;
        } finally {
            if (session != null) session.close();
        }
    }

}






