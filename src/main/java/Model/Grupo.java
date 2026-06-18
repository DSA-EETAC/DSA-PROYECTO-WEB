package Model;

public class Grupo {
    private int id;
    private String nombre;
    private int miembros;

    // Constructor vacío obligatorio
    public Grupo() { }


    public Grupo( int id, String nombre, int miembros) {
        this.id = id;
        this.nombre = nombre;
        this.miembros = miembros;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getMiembros() { return miembros; }
    public void setMiembros(int miembros) { this.miembros = miembros; }
}