package Model;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement

public class Item {
    private String tipo;
    private String nombre;
    private double precio;
    private int id;


    public Item() {
    }

    public Item(int id, String tipo, String nombre, double precio) {
        this.id = id;
        this.tipo = tipo;
        this.nombre = nombre;
        this.precio = precio;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }
}