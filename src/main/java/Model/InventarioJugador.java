package Model;
import java.util.List;

public class InventarioJugador {
    private List<String> objetos;

    public InventarioJugador() {}

    public InventarioJugador(List<String> objetos) {
        this.objetos = objetos;
    }

    public List<String> getObjetos() { return objetos; }
    public void setObjetos(List<String> objetos) { this.objetos = objetos; }
}