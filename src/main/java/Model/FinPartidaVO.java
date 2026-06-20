package Model;
import java.util.List;

public class FinPartidaVO {
    private String username;
    private int monedasGanadas; // Esto actúa también como puntuación
    private List<Integer> objetosConsumidos; // Lista de IDs de los objetos a eliminar

    public FinPartidaVO() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getMonedasGanadas() { return monedasGanadas; }
    public void setMonedasGanadas(int monedasGanadas) { this.monedasGanadas = monedasGanadas; }
    public List<Integer> getObjetosConsumidos() { return objetosConsumidos; }
    public void setObjetosConsumidos(List<Integer> objetosConsumidos) { this.objetosConsumidos = objetosConsumidos; }
}