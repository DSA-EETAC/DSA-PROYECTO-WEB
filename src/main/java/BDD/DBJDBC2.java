package BDD;

import java.sql.*;

public class DBJDBC2 extends DBJDBC{


    public static void insert() throws SQLException{
        Connection connection = DBUtils.getConnection();

        // SQL INJECTION
        String theQuery = "INSERT INTO user (id, nombre, password, mail) VALUES (0, ?, ?, ?)";        // log.debug

        PreparedStatement statement1  =  connection.prepareStatement(theQuery);
        statement1.setString(1, "Montes");
        statement1.setString(2, "456");
        statement1.setString(3, "montes@gmail.com");

        // a = b / 0  - null.method();

        /// NULLPOINTER ??
        statement1.execute();
        /// NULLPOINTER ??

        connection.close();

    }

    public static void findAll() throws Exception {
        Connection connection = DBUtils.getConnection();
        Statement statement = connection.createStatement();

        // Hacemos una consulta para leer toda tu tabla 'usuario'
        ResultSet rs = statement.executeQuery("SELECT * FROM usuario");

        System.out.println("\n--- LISTA DE USUARIOS EN LA BBDD ---");

        // Recorremos fila por fila lo que nos devuelve MariaDB
        while (rs.next()) {
            String nombre = rs.getString("nombre");
            String password = rs.getString("password");
            String correo = rs.getString("correo");

            System.out.println("Nombre: " + nombre + " | Pass: " + password + " | Correo: " + correo);
        }

        connection.close();
    }

    public static void main(String[] args) throws Exception {
        insert();
        //findAll();

        // ORM (Object Relation Mapping) --> DAO (Data Access Object)

/*
        User u = new User("Toni");
        s.save(u); =====================> "INSERT INTO USER ...."
        u.setName("Juan");
        s.update(u); ====> "UPDATE xxx"

        s.save(new Object("Escudo")):; //"INSERT NTO Object
        s.save(new Mapa("Escudo")):;   // INSERT INTO Mapa
*/


    }

}
