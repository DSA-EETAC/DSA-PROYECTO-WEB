package Model;

public class Evento {

    private String id;
    private String nombre;
    private String descripcion;
    private String fecha_inicio;
    private String fecha_fin;
    private String imagen_URL;

    public Evento(){
    }

    public Evento(String id, String nombre, String descripcion, String fecha_inicio, String fecha_fin, String imagen_URL) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha_inicio = fecha_inicio;
        this.fecha_fin = fecha_fin;
        this.imagen_URL = imagen_URL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha_inicio() {
        return fecha_inicio;
    }

    public void setFecha_inicio(String fecha_inicio) {
        this.fecha_inicio = fecha_inicio;
    }

    public String getFecha_fin() {
        return fecha_fin;
    }

    public void setFecha_fin(String fecha_fin) {
        this.fecha_fin = fecha_fin;
    }

    public String getImagen_URL() {
        return imagen_URL;
    }

    public void setImagen_URL(String imagen_URL) {
        this.imagen_URL = imagen_URL;
    }
}

