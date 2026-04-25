package Model;

public class Usuario {
    //DATOS DE REGISTRO (WEB)
    private String nombre;
    private String password;
    private String correo;
    private String telefono;

    //DATOS DEL JUEGO (temple run)
    private int nivel=1;
    private int ataque=10;
    private int defensa=5;
    private int resistencia=5;

    public Usuario(){}

    // CONSTRUCTOR: Se ejecuta cuando alguien se registra
    public Usuario(String nombre, String password, String correo, String telefono) {
        // Guardamos los datos del formulario
        this.nombre = nombre;
        this.password = password;
        this.correo = correo;
        this.telefono = telefono;
    }

    // GETTERS

    public String getNombre() {
        return nombre;
    }
    public String getPassword() {
        return password;
    }
    public String getCorreo() {
        return correo;
    }
    public String getTelefono() {
        return telefono;
    }
    public int getNivel() {
        return nivel;
    }
    public int getAtaque(){
        return ataque;
    }
    public int getDefensa(){
        return defensa;
    }
    public int getResistencia(){
        return resistencia;
    }

    //SETTERS

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public void setNivel(int nivel) {
        this.nivel = nivel;
    }
    public void setAtaque(int ataque) {
        this.ataque = ataque;
    }
    public void setDefensa(int defensa) {
        this.defensa = defensa;
    }
    public void setResistencia(int resistencia) {
        this.resistencia = resistencia;
    }
    public void subirNivel() {
        this.nivel++;
        this.ataque += 2; // Al subir nivel, mejora el ataque
        this.defensa += 1;
    }

    @Override
    public String toString() {
        return "Jugador: " + nombre + " | Nivel: " + nivel;
    }

}
