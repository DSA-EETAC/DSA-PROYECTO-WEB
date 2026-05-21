package BDD.orm;


import BDD.DBUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class FactorySession {

    public static Session openSession() {
        try {
            // Llamamos a tu DBUtils para usar "userDB" y "ds4m0l4"
            Connection conn = DBUtils.getConnection();

            return new SessionImpl(conn);

        } catch (Exception e) {
            System.err.println("¡FALLO DE CONEXIÓN! Revisa que MariaDB esté encendido.");
            e.printStackTrace();
            // Si la conexión falla, devuelve una sesión rota (null), que es el error que ves.
            return new SessionImpl(null);
        }
    }

    public static Connection getConnection()  {
        String db = DBUtils.getDb();
        String host = DBUtils.getDbHost();
        String port = DBUtils.getDbPort();
        String user = DBUtils.getDbUser();
        String pass = DBUtils.getDbPasswd();


        Connection connection = null;
        try {
            DriverManager.getConnection("jdbc:mariadb://"+host+":"+port+"/"+
                    db+"?user="+user+"&password="+pass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }

    public static Session openSession(String url, String user, String password) {
        return null;
    }
}
