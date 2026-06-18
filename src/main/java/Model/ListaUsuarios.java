package Model;

import java.util.List;

public class ListaUsuarios {
    private List<User> usuarios;

    public ListaUsuarios() {} // Constructor vacío obligatorio
    public ListaUsuarios(List<User> usuarios) { this.usuarios = usuarios; }

    public List<User> getUsuarios() { return usuarios; }
    public void setUsuarios(List<User> usuarios) { this.usuarios = usuarios; }
}