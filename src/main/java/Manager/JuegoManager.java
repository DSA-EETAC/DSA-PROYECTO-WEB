package Manager;
import Model.Usuario;

public interface JuegoManager {

    boolean registrarUsuario(Usuario u);

    Usuario consultarUsuario(String nombre);

    Usuario procesarLogin(String nombre, String password);

    int obtenerNumeroUsuarios();

    void clear();

}
