package Model;

public class Partida {
    private int user_id;
    private int puntuacion;

    public Partida() {} // Constructor vacío

    public Partida(int user_id, int puntuacion) {
        this.user_id = user_id;
        this.puntuacion = puntuacion;
    }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }
    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }
}