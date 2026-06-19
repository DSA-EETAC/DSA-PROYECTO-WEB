package Model;

public class JugadorRanking {
    private String nombreJugador;
    private int puntuacion;

    public JugadorRanking() {}

    public JugadorRanking(String nombreJugador, int puntuacion) {
        this.nombreJugador = nombreJugador;
        this.puntuacion = puntuacion;
    }

    public String getNombreJugador() { return nombreJugador; }
    public void setNombreJugador(String nombreJugador) { this.nombreJugador = nombreJugador; }

    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }
}