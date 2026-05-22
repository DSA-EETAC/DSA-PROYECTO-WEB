package Manager;
import Model.User;

import java.util.List;

public interface JuegoManager {

    boolean registrarUsuario(User u);

    User consultarUsuario(String nombre);

    User procesarLogin(String nombre, String password);

    boolean comprarObjeto(String nombreJugador, String nombreObjeto);

    List<String> obtenerInventarioUsuario(String nombreUsuario);

    int obtenerNumeroUsuarios();

    void clear();

}
