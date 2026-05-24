package BDD.orm.dao;

import BDD.orm.FactorySession;
import BDD.orm.Session;
import Model.*;

import java.util.ArrayList;
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

    public User getUsuario(String nombre) {return getUsuario("nombre", nombre); }
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


    public List<String> getNombresItemsUsuario(String nombre) {
        Session session = null;
        List<String> listaNombres = new ArrayList<>();

        try {
            session = FactorySession.openSession();

            // 1. Primero buscamos al usuario por su nombre usando tu ORM
            User usuario = (User) session.get(User.class, "nombre", nombre);

            if (usuario != null) {
                int userId = usuario.getId(); // Obtenemos su ID real

                // 2. Pedimos al ORM TODOS los registros de la tabla 'inventario'
                List<Inventario> todoElInventario = session.findAll(Inventario.class);

                // 3. Filtramos en Java buscando solo los que sean de nuestro usuario
                if (todoElInventario != null) {
                    for (Inventario inv : todoElInventario) {
                        if (inv.getUser_id() == userId) {

                            // 4. Buscamos el 'Item' usando el ID encontrado
                            Item itemEncontrado = (Item) session.get(Item.class, "id", inv.getItem_id());

                            if (itemEncontrado != null) {
                                listaNombres.add(itemEncontrado.getNombre());
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return listaNombres;
    }

    public List<String> getNombresItemsUsuario(int userId) {
        Session session = null;
        List<String> listaNombres = new ArrayList<>();

        try {
            // Abrimos sesión igual que en tus otros métodos
            session = FactorySession.openSession();

            // 1. Pedimos al ORM TODOS los registros de la tabla 'inventario'
            // (Asegúrate de haber creado la clase Inventario.java en tu paquete Model)
            List<Inventario> todoElInventario = session.findAll(Inventario.class);

            // 2. Filtramos en Java buscando solo los que sean de nuestro usuario
            if (todoElInventario != null) {
                for (Inventario inv : todoElInventario) {

                    if (inv.getUser_id() == userId) {

                        // 3. Usamos el ORM para buscar el 'Item' usando el ID que hemos encontrado
                        // Le pasamos el nombre de la columna clave ("id") y el valor a buscar
                        Item itemEncontrado = (Item) session.get(Item.class, "id", inv.getItem_id());

                        // 4. Si el ORM lo encuentra, extraemos su nombre y lo añadimos a la lista
                        if (itemEncontrado != null) {
                            listaNombres.add(itemEncontrado.getNombre());
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return listaNombres;
    }

    public void updateUsuario(User usuario) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.update(usuario);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(session != null) session.close();
        }
    }
    public void añadirItemAInventario(int userId, int itemId) {
        Session session = null;
        try {
            session = FactorySession.openSession();

            List<Inventario> inventarioCompleto = session.findAll(Inventario.class);
            Inventario objetoExistente = null;

            // Chivato 1: Ver qué datos entran al método
            System.out.println("---> [DEBUG] Buscando si USER " + userId + " ya tiene el ITEM " + itemId);

            if (inventarioCompleto != null) {
                // Chivato 2: Ver cuántas filas lee el ORM en total
                System.out.println("---> [DEBUG] Filas totales en la tabla Inventario: " + inventarioCompleto.size());

                for (Inventario inv : inventarioCompleto) {
                    // Chivato 3: Ver qué valores tienen las variables de cada fila que ha leído el ORM
                    System.out.println("---> [DEBUG] Fila leída -> ID=" + inv.getId() + " | User_ID=" + inv.getUser_id() + " | Item_ID=" + inv.getItem_id() + " | Cantidad=" + inv.getQuantity());

                    if (inv.getUser_id() == userId && inv.getItem_id() == itemId) {
                        objetoExistente = inv;
                        break;
                    }
                }
            }

            if (objetoExistente != null) {
                System.out.println("---> [DEBUG] ¡Encontrado en mochila! Modificando cantidad existente...");
                int cantidadActual = objetoExistente.getQuantity();
                objetoExistente.setQuantity(cantidadActual + 1);
                session.update(objetoExistente);
            } else {
                System.out.println("---> [DEBUG] No encontrado en mochila. Creando fila nueva...");
                Inventario nuevoRegistro = new Inventario(userId, itemId, 1);
                session.save(nuevoRegistro);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
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
