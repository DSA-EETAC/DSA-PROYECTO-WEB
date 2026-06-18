package Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Grupo {
    private int id;
    private String nombre;
    private int miembros;

    // Constructor vacío obligatorio
    public Grupo() { }

    // Le añadimos estas etiquetas al constructor con datos para que Jackson sepa mapearlo
    @JsonCreator
    public Grupo(@JsonProperty("id") int id, @JsonProperty("nombre") String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getMiembros() { return miembros; }
    public void setMiembros(int miembros) { this.miembros = miembros; }
}