package Model;

import java.util.ArrayList;
import java.util.List;

public class DetalleGrupo {
    private boolean tieneGrupo;
    private String nombreGrupo;
    private List<String> miembros;

    // Constructor para cuando NO tiene grupo
    public DetalleGrupo() {
        this.tieneGrupo = false;
        this.miembros = new ArrayList<>();
    }

    // Constructor para cuando SÍ tiene grupo
    public DetalleGrupo(String nombreGrupo, List<String> miembros) {
        this.tieneGrupo = true;
        this.nombreGrupo = nombreGrupo;
        this.miembros = miembros;
    }

    // Getters y Setters necesarios para que Jersey lo convierta a JSON automáticamente
    public boolean isTieneGrupo() { return tieneGrupo; }
    public void setTieneGrupo(boolean tieneGrupo) { this.tieneGrupo = tieneGrupo; }
    public String getNombreGrupo() { return nombreGrupo; }
    public void setNombreGrupo(String nombreGrupo) { this.nombreGrupo = nombreGrupo; }
    public List<String> getMiembros() { return miembros; }
    public void setMiembros(List<String> miembros) { this.miembros = miembros; }
}