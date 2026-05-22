package BDD.orm.dao;


import java.util.List;
import Model.*;

public interface IUsuarioDAO {

    void addUsuario(String nombre, String password, String correo);

    User getUsuario(String nombre);
    User getUsuario(String key, String val);

    void updateUsuario(String nombre, String nuevoPassword, String nuevoCorreo);

    void deleteUsuario(String nombre);

    List<User> getUsuarios();
    public List<String> getNombresItemsUsuario(int userId);
    public void updateUsuario(User usuario);
    public void añadirItemAInventario(int userId, int itemId);
    public List<String> getNombresItemsUsuario(String nombre);


}
