package Model;

import java.util.List;
import java.util.ArrayList;

public class ListaGrupos {
    private List<Grupo> grupos;

    public ListaGrupos() {
    }

    public ListaGrupos(List<Grupo> grupos) {
        this.grupos = grupos;
    }

    public List<Grupo> getGrupos() {
        return grupos;
    }

    public void setGrupos(List<Grupo> grupos) {
        this.grupos = grupos;
    }
}