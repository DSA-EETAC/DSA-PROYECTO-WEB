package BDD.orm.dao;

import BDD.orm.FactorySession;
import BDD.orm.Session;
import Model.*;

import java.util.HashMap;
import java.util.List;

public class UsuarioDAOImpl implements IUsuarioDAO {


    public void addUsuario(String nombre, String password, String correo) {
        Session session = null;
        int employeeID = 0;
        try {
            session = FactorySession.openSession();
            User usuario = new User(nombre, password, correo);
            session.save(usuario);
        }
        catch (Exception e) {
            // LOG
        }
        finally {
            session.close();
        }
    }

    public User getUsuario(String nombre) {return getUsuario("nombre", "nombre"); }
    public User getUsuario(String key, String val) {
        Session session = null;
        User usuario = null;
        try {
            session = FactorySession.openSession();
            usuario = (User)session.get(User.class, key, val);
        }
        catch (Exception e) {
            // LOG
        }
        finally {
            session.close();
        }

        return usuario;
    }


    public void updateUsuario(String nombre, String newPassword, String newCorreo) {
        User usuario = this.getUsuario(nombre);
        usuario.setNombre(nombre);
        usuario.setPassword(newPassword);
        usuario.setMail(newCorreo);

        Session session = null;
        try {
            session = FactorySession.openSession();
            session.update(usuario);
        }
        catch (Exception e) {
            // LOG
        }
        finally {
            session.close();
        }
    }


    public void deleteUsuario(String nombre) {
        User usuario = this.getUsuario(nombre);
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.delete(usuario);
        }
        catch (Exception e) {
            // LOG
        }
        finally {
            session.close();
        }

    }


    public List<User> getUsuarios() {
        Session session = null;
        List<User> usuarioList =null;
        try {
            session = FactorySession.openSession();
            usuarioList = session.findAll(User.class);
        }
        catch (Exception e) {
            // LOG
        }
        finally {
            session.close();
        }
        return usuarioList;
    }


    public List<User> getEmployeeByDept(int deptID) {

        // SELECT e.name, d.name FROM Employees e, DEpt d WHERE e.deptId = d.ID AND e.edat>87 AND ........

//        Connection c =

        Session session = null;
        List<User> usuarioList =null;
        try {
            session = FactorySession.openSession();


            HashMap<String, Integer> params = new HashMap<String, Integer>();
            params.put("deptID", deptID);

            usuarioList = session.findAll(User.class, params);
        }
        catch (Exception e) {
            // LOG
        }
        finally {
            session.close();
        }
        return usuarioList;
    }

    /*

    public void customQuery(xxxx) {
        Session session = null;
        List<Employee> employeeList=null;
        try {
            session = FactorySession.openSession();
            Connection c = session.getConnection();
            c.createStatement("SELECT * ")

        }
*/

}
