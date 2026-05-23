package Service;

import Model.User;
import Manager.JuegoManagerImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import javax.ws.rs.core.Response;

public class Sistema_JuegoTest {
    private Sistema_Juego sistema;

    @Before
    public void setUp() {
        sistema = new Sistema_Juego();
        // Limpiamos la memoria del Singleton antes de cada test para evitar fallos por datos viejos
        JuegoManagerImpl.getInstance().clear();
    }

    @After
    public void tearDown() {
        // Limpiamos de nuevo al terminar
        JuegoManagerImpl.getInstance().clear();
        sistema = null;
    }

    @Test
    public void testRegistrarUsuario_Ok() {
        User nuevo = new User("Yerald", "Artdg@u596", "yerald@mail.com");

        Response respuesta = sistema.registrarUsuario(nuevo);

        // En lugar de boolean, comprobamos que el status sea 201 (Created)
        Assert.assertEquals("El código de respuesta debería ser 201.", 201, respuesta.getStatus());

        // Verificamos que el objeto devuelto sea el usuario
        User u = (User) respuesta.getEntity();
        Assert.assertEquals("Yerald", u.getNombre());
    }

    @Test
    public void testRegistrarUsuario_Duplicado() {
        User user1 = new User("Yerald", "Artdg@u596", "yerald@mail.com");
        User user2 = new User("Yerald", "87@hbiefWQ", "javier@mail.com");

        sistema.registrarUsuario(user1); // Primer registro exitoso
        Response respuesta = sistema.registrarUsuario(user2); // Segundo registro (duplicado)

        // Comprobamos que el status sea 409 (Conflict)
        Assert.assertEquals("Debería devolver un error 409 por nombre duplicado.", 409, respuesta.getStatus());
    }

    @Test
    public void testLogin_NoExiste() {
        // Probamos login con un usuario que no ha sido registrado
        User intento = new User("NoExiste", "1234", "");

        Response respuesta = sistema.login(intento);

        // Comprobamos que el status sea 401 (Unauthorized)
        Assert.assertEquals("Si el usuario no existe, el login debe fallar con 401.", 401, respuesta.getStatus());
    }
}