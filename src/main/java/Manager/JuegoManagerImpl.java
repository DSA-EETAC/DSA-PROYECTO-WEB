package Manager;

import Model.User;
import org.apache.log4j.Logger;
import java.util.*;

public class JuegoManagerImpl implements JuegoManager {

    private static JuegoManager instance;
    final static Logger log = Logger.getLogger(JuegoManagerImpl.class.getName());

    // Estructuras de datos
    private Map<String, User> usuariosBD;

    // Constructor privado
    private JuegoManagerImpl() {
        this.usuariosBD = new HashMap<>();
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

        if (usuariosBD.containsKey(u.getNombre())) {
            log.warn("FIN registrarUsuario: ERROR - El usuario ya existe.");
            return false;
        }

        usuariosBD.put(u.getNombre(), u);
        log.info("FIN registrarUsuario: Usuario registrado correctamente.");
        return true;
    }

    @Override
    public User consultarUsuario(String nombre) {
        log.info("Consulta usuario: " + nombre);
        return usuariosBD.get(nombre);
    }

    @Override
    public User procesarLogin(String nombre, String password) {
        log.info("INICIO procesarLogin: nombre=" + nombre);

        User u = usuariosBD.get(nombre);

        // Verificamos si existe y si la contraseña coincide
        if (u != null && u.getPassword().equals(password)) {
            log.info("FIN procesarLogin: Login exitoso.");
            return u;
        }

        log.warn("FIN procesarLogin: Credenciales incorrectas.");
        return null;
    }

    @Override
    public int obtenerNumeroUsuarios() {
        return usuariosBD.size();
    }

    @Override
    public void clear() {
        this.usuariosBD.clear();
        log.info("Estructuras de datos limpiadas (clear).");
    }
}






