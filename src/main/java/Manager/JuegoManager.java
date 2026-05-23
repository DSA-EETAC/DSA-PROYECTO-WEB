package Manager;
import Model.User;
import Model.Item;

import java.util.List;

public interface JuegoManager {

    boolean registrarUsuario(User u);

    User consultarUsuario(String nombre);

    User procesarLogin(String nombre, String password);

    boolean comprarObjeto(String nombreJugador, String nombreObjeto);

    List<String> obtenerInventarioUsuario(String nombreUsuario);

    int obtenerNumeroUsuarios();

    List<User> obtenerUsuarios();

    void clear();

    List<Item> obtenerItemsTienda();

}
