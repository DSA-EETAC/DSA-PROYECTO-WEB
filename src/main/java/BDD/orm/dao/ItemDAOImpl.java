package BDD.orm.dao;

import BDD.orm.FactorySession;
import BDD.orm.Session;
import Model.*;

import java.util.HashMap;
import java.util.List;

public class ItemDAOImpl implements IItemDAO {

    @Override
    public void addItem(int id, String tipo, String nombre, double precio) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            Item item = new Item(id, tipo, nombre, precio);
            session.save(item);
        }
        catch (Exception e) {
            // LOG
        }
        finally {
            if (session != null) session.close();
        }
    }

    // Búsqueda por por ID
    @Override
    public Item getItem(int id) {
        return getItem("id", id);
    }

    @Override
    public Item getItem(String key, Object val) {
        Session session = null;
        Item item = null;
        try {
            session = FactorySession.openSession();
            // Asume que session.get() acepta Object para el valor
            item = (Item)session.get(Item.class, key, val);
        }
        catch (Exception e) {
            // LOG
        }
        finally {
            if (session != null) session.close();
        }

        return item;
    }

    @Override
    public void updateItem(int id, String nuevoTipo, String nuevoNombre, double nuevoPrecio) {
        Item item = this.getItem(id);

        if (item != null) {
            item.setTipo(nuevoTipo);
            item.setNombre(nuevoNombre);
            item.setPrecio(nuevoPrecio);

            Session session = null;
            try {
                session = FactorySession.openSession();
                session.update(item);
            }
            catch (Exception e) {
                // LOG
            }
            finally {
                if (session != null) session.close();
            }
        }
    }

    @Override
    public void deleteItem(int id) {
        Item item = this.getItem(id);

        if (item != null) {
            Session session = null;
            try {
                session = FactorySession.openSession();
                session.delete(item);
            }
            catch (Exception e) {
                // LOG
            }
            finally {
                if (session != null) session.close();
            }
        }
    }

    @Override
    public List<Item> getItems() {
        Session session = null;
        List<Item> itemList = null;
        try {
            session = FactorySession.openSession();
            itemList = session.findAll(Item.class);
        }
        catch (Exception e) {
            // LOG
        }
        finally {
            if (session != null) session.close();
        }
        return itemList;
    }

    @Override
    public List<Item> getItemsByType(String tipo) {
        Session session = null;
        List<Item> itemList = null;
        try {
            session = FactorySession.openSession();

            HashMap<String, Object> params = new HashMap<>();
            params.put("tipo", tipo);

            itemList = session.findAll(Item.class, params);
        }
        catch (Exception e) {
            // LOG
        }
        finally {
            if (session != null) session.close();
        }
        return itemList;
    }
}