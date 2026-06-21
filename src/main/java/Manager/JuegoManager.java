package Manager;
import Model.*;

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

    List<Grupo> obtenerGrupos();

    java.util.List<Model.Evento> obtenerListaEventos();

    boolean registrarInscripcion(InscripcionRequest request);

    boolean unirUsuarioAGrupo(String nombreUsuario, int idGrupo);
    public DetalleGrupo obtenerDetalleGrupoUsuario(String nombreUsuario);

    boolean sumarMonedas(String nombreJugador, int cantidad);
//    List<String> obtenerInventarioUsuarioPorNombre(String nombreUsuario);
//    DetalleGrupo obtenerDetalleGrupoUsuarioPorNombre(String nombre);

    List<JugadorRanking> obtenerRankingEvento(String idEvento);
    boolean repartirPremios(String idEvento);

    boolean sumarPuntosAInscripcion(String idEvento, String nombreJugador, int puntosNuevos);
    public boolean abandonarEvento(String username, String idEvento);

    List<User> obtenerUsuariosEvento(String idEvento);//Añadido para sacar los Usuarios de un evento

    public boolean procesarFinPartida(Model.FinPartidaVO datos);
}
