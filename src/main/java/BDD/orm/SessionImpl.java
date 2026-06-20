package BDD.orm;

import BDD.orm.util.*;
import java.util.ArrayList; // <--- ESTO ARREGLA EL ERROR DE ARRAYLIST
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import Model.User;


public class SessionImpl implements Session {
    private final Connection conn;

    public SessionImpl(Connection conn) {
        this.conn = conn;
    }

    public void save(Object entity) {

        if (entity instanceof Model.InscripcionEvento) {
            Model.InscripcionEvento inscripcion = (Model.InscripcionEvento) entity;
            String sql = "INSERT INTO InscripcionEvento (user_id, evento_id, puntuacion) VALUES (?, ?, ?)";

            try (PreparedStatement ptsm = conn.prepareStatement(sql)) {
                ptsm.setInt(1, inscripcion.getUser_id());
                ptsm.setString(2, inscripcion.getEvento_id());
                ptsm.setInt(3, inscripcion.getPuntuacion());

                ptsm.executeUpdate();
                System.out.println("¡ÉXITO! Inscripción guardada en la Base de Datos usando SQL manual.");
            } catch (SQLException e) {
                System.err.println("Error al insertar (¿El usuario ya estaba apuntado a este evento?): " + e.getMessage());
            }
            return; // ¡Importante! Salimos para que no intente ejecutar el QueryHelper de abajo
        }

        if (entity instanceof Model.User) {
            Model.User u = (Model.User) entity;
            String sql = "INSERT INTO User (nombre, password, mail, monedas) VALUES (?, ?, ?, ?)";

            try (PreparedStatement ptsm = conn.prepareStatement(sql)) {
                ptsm.setString(1, u.getNombre());
                ptsm.setString(2, u.getPassword());
                ptsm.setString(3, u.getMail());
                ptsm.setInt(4, 0); // Les damos 0 monedas al empezar

                ptsm.executeUpdate();
                System.out.println("¡ÉXITO! Usuario registrado en BD usando SQL manual.");
            } catch (SQLException e) {
                System.err.println("Error al insertar User (¿El nombre o correo ya existe?): " + e.getMessage());
            }
            return;
        }

        // PARCHE PARA HISTORIAL DE PARTIDAS
        if (entity instanceof Model.Partida) {
            Model.Partida p = (Model.Partida) entity;
            String sql = "INSERT INTO HistorialPartidas (user_id, puntuacion) VALUES (?, ?)";

            try (PreparedStatement ptsm = conn.prepareStatement(sql)) {
                ptsm.setInt(1, p.getUser_id());
                ptsm.setInt(2, p.getPuntuacion());
                ptsm.executeUpdate();
                System.out.println("¡Historial de partida guardado con éxito!");
            } catch (SQLException e) {
                System.err.println("Error al guardar historial: " + e.getMessage());
            }
            return;
        }




        String insertQuery = QueryHelper.createQueryINSERT(entity);
        PreparedStatement pstm = null;

        try {
            pstm = conn.prepareStatement(insertQuery);
            int i = 1;

            for (String field : ObjectHelper.getFields(entity)) {
                pstm.setObject(i++, ObjectHelper.getter(entity, field));
            }

            pstm.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error del QueryHelper en el ORM:");
            e.printStackTrace();
        }
    }



    public void close() {

    }

    @Override
//    public Object get(Class theClass, int ID) {
//        return null;
//    }

    public Object get(Class theClass, Object val) {
        return get(theClass, "id", val);
    }

    public Object get(Class theClass, String key, Object val) {

        String sql = "SELECT * FROM " + theClass.getSimpleName() + " WHERE " + key + " = ?";

        try {
            PreparedStatement pstm = conn.prepareStatement(sql);            // 2. Metemos el ID (que en tu caso es el texto "IndianaJones") en el interrogante
            pstm.setObject(1, val);

            // 3. Ejecutamos la consulta en MariaDB
            ResultSet rs = pstm.executeQuery();

            // 4. Si la base de datos nos devuelve al usuario...
            if (rs.next()) {
                // Creamos un objeto Usuario vacío usando Java Reflection
                Object o = theClass.getDeclaredConstructor().newInstance();

                // Analizamos cuántas columnas nos ha devuelto MariaDB
                ResultSetMetaData rsmd = rs.getMetaData();
                int numColumns = rsmd.getColumnCount();

                // Bucle para ir columna por columna (nombre, password, correo)
                for (int i = 1; i <= numColumns; i++) {
                    String columnName = rsmd.getColumnName(i); // ej: "nombre"
                    Object value = rs.getObject(i);            // ej: "IndianaJones"

                    // Usamos el ObjectHelper del profe para inyectar el valor en el objeto

                    if (value != null) ObjectHelper.setter(o, columnName, value);
                }

                // Devolvemos el objeto lleno con los datos de la base de datos
                return o;
            }

        } catch (Exception e) {
            System.err.println("Error interno al intentar leer de la base de datos:");
            e.printStackTrace();
        }

        // Si falla o no encuentra al usuario, devuelve null
        return null;
    }

    public void update(Object object) {

        if (object instanceof User) {
            User usuario = (User) object;
            String sql = "UPDATE User SET nombre = ?, password = ?, mail = ?, monedas = ?, id_grupo = ? WHERE id = ?";

            try (PreparedStatement ptsm = conn.prepareStatement(sql)) {
                ptsm.setString(1, usuario.getNombre());
                ptsm.setString(2, usuario.getPassword());
                ptsm.setString(3, usuario.getMail());
                ptsm.setInt(4, usuario.getMonedas());
                if (usuario.getId_grupo() > 0) {
                    ptsm.setInt(5, usuario.getId_grupo());
                } else {
                    ptsm.setNull(5, java.sql.Types.INTEGER);
                }
                ptsm.setInt(6, usuario.getId());

                int filasModificadas = ptsm.executeUpdate();
                if (filasModificadas > 0) {
                    System.out.println("¡Usuario actualizado con éxito en la BD!");
                }
            } catch (SQLException e) {
                System.out.println("Error al actualizar el usuario: " + e.getMessage());
            }
        }

        else if (object instanceof Model.Inventario) {
            Model.Inventario inv = (Model.Inventario) object;
            String sql = "UPDATE Inventario SET quantity = ? WHERE id = ?";

            try (PreparedStatement ptsm = conn.prepareStatement(sql)) {
                ptsm.setInt(1, inv.getQuantity());
                ptsm.setInt(2, inv.getId());

                int filasModificadas = ptsm.executeUpdate();
                if (filasModificadas > 0) {
                    System.out.println("¡Cantidad del inventario actualizada con éxito!");
                }
            } catch (SQLException e) {
                System.out.println("Error al actualizar el inventario: " + e.getMessage());
            }
        }

        else if (object instanceof Model.Item) {
            Model.Item item = (Model.Item) object;
            String sql = "UPDATE Item SET tipo = ?, nombre = ?, precio = ? WHERE id = ?";

            try (PreparedStatement ptsm = conn.prepareStatement(sql)) {
                ptsm.setString(1, item.getTipo());
                ptsm.setString(2, item.getNombre());
                ptsm.setDouble(3, item.getPrecio());
                ptsm.setInt(4, item.getId());
                ptsm.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error al actualizar el item: " + e.getMessage());
            }
        }

        else if (object instanceof Model.InscripcionEvento) {
            Model.InscripcionEvento inscripcion = (Model.InscripcionEvento) object;
            // Actualizamos la puntuación filtrando por el ID del usuario y el ID del evento
            String sql = "UPDATE InscripcionEvento SET puntuacion = ? WHERE user_id = ? AND evento_id = ?";

            try (PreparedStatement ptsm = conn.prepareStatement(sql)) {
                ptsm.setInt(1, inscripcion.getPuntuacion());
                ptsm.setInt(2, inscripcion.getUser_id());
                ptsm.setString(3, inscripcion.getEvento_id());

                int filasModificadas = ptsm.executeUpdate();
                if (filasModificadas > 0) {
                    System.out.println("¡Puntuación de la inscripción actualizada con éxito!");
                } else {
                    System.out.println("Cuidado: No se encontró la inscripción para actualizar.");
                }
            } catch (SQLException e) {
                System.out.println("Error al actualizar la inscripción: " + e.getMessage());
            }
        }

        // CASO DE ERROR
        else {
            System.out.println("Error: El ORM no sabe cómo actualizar el objeto de tipo " + object.getClass().getSimpleName());
        }
    }

    public void delete(Object object) {
        // borrar una inscripción en concreto
        if (object instanceof Model.InscripcionEvento) {
            Model.InscripcionEvento insc = (Model.InscripcionEvento) object;
            String sql = "DELETE FROM InscripcionEvento WHERE user_id = ? AND evento_id = ?";
            try (PreparedStatement ptsm = conn.prepareStatement(sql)) {
                ptsm.setInt(1, insc.getUser_id());
                ptsm.setString(2, insc.getEvento_id());
                ptsm.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error al borrar inscripción: " + e.getMessage());
            }
            return;
        }

        // Borrado genérico para User, Item, Grupo asumiendo la columna id
        String sql = "DELETE FROM " + object.getClass().getSimpleName() + " WHERE id = ?";
        try (PreparedStatement ptsm = conn.prepareStatement(sql)) {
            // Usamos tu ObjectHelper para sacar la ID automáticamente
            ptsm.setObject(1, ObjectHelper.getter(object, "id"));
            int filas = ptsm.executeUpdate();
            if (filas > 0) System.out.println("¡Objeto eliminado correctamente de la BD!");
        } catch (SQLException e) {
            System.err.println("Error al borrar el objeto: " + e.getMessage());
        }
    }

    public List<Object> findAll(Class theClass) {
        List<Object> results = new ArrayList<>();
        String sql = "SELECT * FROM " + theClass.getSimpleName();
        try {
            PreparedStatement pstm = conn.prepareStatement(sql);
            ResultSet rs = pstm.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int numColumns = rsmd.getColumnCount();
            while (rs.next()) {
                Object o = theClass.getDeclaredConstructor().newInstance();
                for (int i = 1; i <= numColumns; i++) {
                    String columnName = rsmd.getColumnName(i);
                    Object value = rs.getObject(i);
                    if (value != null) BDD.orm.util.ObjectHelper.setter(o, columnName, value);
                }
                results.add(o);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return results;
    }
    public List<Object> findAll(Class theClass, HashMap params) {
     /*   String theQuery = QueryHelper.createSelectFindAll(theClass, params);
        PreparedStatement pstm = null;
        pstm = conn.prepareStatement(theQuery);

        int i=1;
        for (Object value : params.values()) {
            pstm.setObject(i++, value );
        }
        //ResultSet rs = pstm.executeQuery();




        return result;
*/
     return null;
    }

    public List<Object> query(String query, Class theClass, HashMap params) {
        return null;
    }
}
