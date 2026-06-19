package Model;

public class InscripcionRequest {
    private String username;
    private String idEvento;

    public InscripcionRequest(){}

    public InscripcionRequest (String username, String idEvento){

        this.username = username;
        this.idEvento = idEvento;
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

}
