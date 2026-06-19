package Model;

public class InscripcionEvento {
    private int user_id;
    private String evento_id;

    public InscripcionEvento() { }

    public InscripcionEvento(int user_id, String evento_id) {
        this.user_id = user_id;
        this.evento_id = evento_id;
    }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public String getEvento_id() { return evento_id; }
    public void setEvento_id(String evento_id) { this.evento_id = evento_id; }
}