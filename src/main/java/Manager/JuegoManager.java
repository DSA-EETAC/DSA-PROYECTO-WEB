package Manager;
import Model.DetalleGrupo;
import Model.Grupo;
import Model.User;
import Model.Item;

import java.util.List;

public interface JuegoManager {

    boolean registrarUsuario(User u);

    User consultarUsuario(String nombre);

    User procesarLogin(String nombre, String password);

    boolean comprarObjeto(String nombreJugador, String nombreObjeto);

    List<String> obtenerInventarioUsuario(int idUsuario);

    int obtenerNumeroUsuarios();

    List<User> obtenerUsuarios();

    void clear();

    List<Item> obtenerItemsTienda();

    List<Grupo> obtenerGrupos();

    boolean unirUsuarioAGrupo(int idUsuario, int idGrupo);
    public DetalleGrupo obtenerDetalleGrupoUsuario(int id);


}
