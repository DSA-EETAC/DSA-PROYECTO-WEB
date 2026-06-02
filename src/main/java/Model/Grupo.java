package Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Grupo {
    private String id;
    private String nombre;

    // Constructor vacío obligatorio
    public Grupo() { }

    // Le añadimos estas etiquetas al constructor con datos para que Jackson sepa mapearlo
    @JsonCreator
    public Grupo(@JsonProperty("id") String id, @JsonProperty("nombre") String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}