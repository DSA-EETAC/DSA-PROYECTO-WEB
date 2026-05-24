package Model;
import java.util.List;

public class TiendaJuego {
    private List<Item> items;

    // Constructor vacío obligatorio para JSON
    public TiendaJuego() {}

    public TiendaJuego(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}