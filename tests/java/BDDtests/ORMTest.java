package BDDtests;
import BDD.orm.FactorySession;
import BDD.orm.Session;
import Model.User;
import org.junit.Test;

public class ORMTest {


    @Test
    public void registerTest() {

        Session session = FactorySession.openSession();
        User nuevoJugador = new User("aaron ", "1234", "aaron@gmail.com");
        session.save(nuevoJugador);

        ///
        // POST ==> hi ha un nou usuari a la taula POU
        //
    }

    @Test
    public void loginTest() {

        Session session = FactorySession.openSession();

        User jugadorRecuperado = (User) session.get(User.class, 6);
        System.out.println("Usuario recuperado:" +jugadorRecuperado.getNombre());


        User jugadorRecuperado2 = (User) session.get(User.class, "mail", "hugo@gmail.com");

        System.out.println("Usuario recuperado:" +jugadorRecuperado2.getNombre());

    }
}
