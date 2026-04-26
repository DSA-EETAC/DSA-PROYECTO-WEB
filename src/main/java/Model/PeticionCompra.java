package Model;

public class PeticionCompra {
    private String nombreJugador;
    private String nombreObjeto;
    private int precio;

    // Constructor vacío (OBLIGATORIO para que el JSON no explote)
    public PeticionCompra() {}

    // Getters
    public String getNombreJugador() { return nombreJugador; }
    public String getNombreObjeto() { return nombreObjeto; }
    public int getPrecio() { return precio; }

    // Setters
    public void setNombreJugador(String nombreJugador) { this.nombreJugador = nombreJugador; }
    public void setNombreObjeto(String nombreObjeto) { this.nombreObjeto = nombreObjeto; }
    public void setPrecio(int precio) { this.precio = precio; }
}
