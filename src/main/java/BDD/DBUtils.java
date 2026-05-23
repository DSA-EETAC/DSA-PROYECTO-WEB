package BDD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtils {

    public static final String DB_NAME = "templerun";
    public static final String DB_HOST = "localhost";
    public static final String DB_USER = "user";
    public static final String DB_PASS = "templerun";
    public static final String DB_PORT = "3306";

    public static String getDb() {
        return DB_NAME;
    }

    public static String getDbHost(){
        return DB_HOST;
    }

    public static String getDbUser() {
        return DB_USER;
    }

    public static String getDbPasswd() {
        return DB_PASS;
    }

    public static  String getDbPort() {
        return DB_PORT;
    }

    public static Connection getConnection() throws SQLException {
        String db = DBUtils.getDb();
        String host = DBUtils.getDbHost();
        String port = DBUtils.getDbPort();
        String user = DBUtils.getDbUser();
        String pass = DBUtils.getDbPasswd();


        String URL = "jdbc:mariadb://"+host+":"+port+"/"+db+" useR: :"+user+" pass" +pass;
	System.out.println(URL);
        Connection connection = DriverManager.getConnection("jdbc:mariadb://"+host+":"+port+"/"+
                db+"?user="+user+"&password="+pass);

        return connection;
    }

    public static void main(String[] args) throws SQLException {
	    System.out.println("DBUTILS!!!!!");
	    Connection c = DBUtils.getConnection();
    }

}
