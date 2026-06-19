package Model;

public class InscripcionRequest {
    private String username;
    private String idEvento;
    private int puntuacion;

    public InscripcionRequest(){}

    public InscripcionRequest (String username, String idEvento){

        this.username = username;
        this.idEvento = idEvento;
        this.puntuacion = 0;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(String idEvento) {
        this.idEvento = idEvento;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }
}
