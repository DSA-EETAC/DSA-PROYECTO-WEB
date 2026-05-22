package BDD.orm.dao;

import java.util.List;
import Model.*;

public interface IItemDAO {

    // Crear
    void addItem(int id, String tipo, String nombre, double precio);

    // Leer
    Item getItem(int id);
    Item getItem(String key, Object val); // Cambiado val a Object para soportar int o String

    // Actualizar (asumiendo que el ID no cambia)
    void updateItem(int id, String nuevoTipo, String nuevoNombre, double nuevoPrecio);

    // Borrar
    void deleteItem(int id);

    // Listar todos
    List<Item> getItems();

    // FIltrar
    List<Item> getItemsByType(String tipo);
}