package Service;

import Manager.JuegoManager;
import Manager.JuegoManagerImpl;
import Model.FinPartidaVO;
import Model.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class JuegoManagerTest {

    private JuegoManager manager;

    private final String USUARIO_TEST = "explorador_test_" + System.currentTimeMillis();

    @Before
    public void setUp() {
        System.out.println("--- CONFIGURANDO ENTORNO DE PRUEBA ---");
        manager = JuegoManagerImpl.getInstance();

        User usuarioPrueba = new User();
        usuarioPrueba.setNombre(USUARIO_TEST);
        usuarioPrueba.setPassword("1234");
        usuarioPrueba.setMail("test@test.com");

        manager.registrarUsuario(usuarioPrueba);

        System.out.println("Usuario '" + USUARIO_TEST + "' creado para el test.");
    }

    @Test
    public void testProcesarFinPartida() {
        System.out.println("--- EJECUTANDO TEST DE FIN DE PARTIDA ---");

        FinPartidaVO paqueteUnity = new FinPartidaVO();
        paqueteUnity.setUsername(USUARIO_TEST);
        paqueteUnity.setMonedasGanadas(1500);
        paqueteUnity.setObjetosConsumidos(Arrays.asList(1, 3));

        boolean resultado = manager.procesarFinPartida(paqueteUnity);

        Assert.assertTrue("La función debe devolver TRUE si todo ha ido bien", resultado);

        User usuarioActualizado = manager.consultarUsuario(USUARIO_TEST);

        Assert.assertNotNull("El usuario debería existir en la BBDD", usuarioActualizado);
        Assert.assertEquals("El usuario debe tener exactamente 1500 monedas", 1500, usuarioActualizado.getMonedas());

        System.out.println("¡ÉXITO! Las monedas se guardaron correctamente en MariaDB.");
    }

    @After
    public void tearDown() {
        System.out.println("--- LIMPIANDO BASE DE DATOS ---");
        System.out.println("Limpieza omitida (No hay método de borrado en la interfaz).");
    }
}