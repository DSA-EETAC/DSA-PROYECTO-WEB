package Manager;
import Model.User;

public interface JuegoManager {

    boolean registrarUsuario(User u);

    User consultarUsuario(String nombre);

    User procesarLogin(String nombre, String password);

    int obtenerNumeroUsuarios();

    void clear();

}
