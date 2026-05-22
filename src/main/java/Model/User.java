package Model;

import java.util.ArrayList;
import java.util.List;

public class User {
    //DATOS DE REGISTRO (WEB)
    private int id;
    private String nombre;
    private String password;
    private String mail;

    private int monedas = 500;  // Les damos monedas iniciales
    //private List<String> inventario = new ArrayList<>(); // Creamos la mochila vacía;

    //DATOS DEL JUEGO (temple run)
    private int nivel=1;
    private int ataque=10;
    private int defensa=5;
    private int resistencia=5;

    public User(){}

    // CONSTRUCTOR: Se ejecuta cuando alguien se registra
    public User(String nombre, String password, String correo) {
        // Guardamos los datos del formulario
        this.nombre = nombre;
        this.password = password;
        this.mail = correo;
    }

    // GETTERS

    public int getId() {return id;}
    public String getNombre() {
        return nombre;
    }
    public String getPassword() {
        return password;
    }
    public String getMail() {
        return mail;
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
    public int getMonedas() { return monedas; }
    //public List<String> getInventario() { return inventario; }

    //SETTERS

    public void setId(int id) {this.id = id;}
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setMail(String correo) {
        this.mail = correo;
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
    public void setMonedas(int monedas) { this.monedas = monedas; }
    //public void setInventario(List<String> inventario) { this.inventario = inventario; }

    public void subirNivel() {
        this.nivel++;
        this.ataque += 2; // Al subir nivel, mejora el ataque
        this.defensa += 1;
    }

    /*public void añadirAlInventario(String nombreObjeto) {
        this.inventario.add(nombreObjeto);
    }*/


    @Override
    public String toString() {
        return "Jugador: " + nombre + " | Nivel: " + nivel;
    }

}
