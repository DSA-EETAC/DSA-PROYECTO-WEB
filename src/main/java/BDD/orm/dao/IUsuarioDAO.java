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


}
