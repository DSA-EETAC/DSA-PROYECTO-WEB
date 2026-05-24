package Model;

public class Inventario {
    private int id;       // ID autoincremental
    private int user_id;  // Relación con tabla user
    private int item_id;  // Relación con tabla item
    private int quantity;

    public Inventario() {}

    public Inventario(int user_id, int item_id, int cantidad) {
        this.user_id = user_id;
        this.item_id = item_id;
        this.quantity = cantidad;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }
    public int getItem_id() { return item_id; }
    public void setItem_id(int item_id) { this.item_id = item_id; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int cantidad) { this.quantity = cantidad; }
}