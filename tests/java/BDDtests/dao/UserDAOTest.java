package BDDtests.dao;
import BDD.orm.FactorySession;
import BDD.orm.Session;
import BDD.orm.dao.IUsuarioDAO;
import BDD.orm.dao.UsuarioDAOImpl;
import Model.User;
import org.junit.Test;

public class UserDAOTest {


    @Test
    public void registerTest() {
        IUsuarioDAO usuarioDAO = new UsuarioDAOImpl();
        usuarioDAO.addUsuario("yerald", "1234", "yerald@gmail.com");
        ///
        // POST ==> hi ha un nou usuari a la taula POU
        //
    }
    @Test
    public void loginTest() {
            IUsuarioDAO usuarioDAO = new UsuarioDAOImpl();
            usuarioDAO.getUsuario("mail", "hugo@gmail.com");
            IUsuarioDAO usuarioDAO2 = new UsuarioDAOImpl();
            usuarioDAO2.getUsuario("hugo");
    }

}
