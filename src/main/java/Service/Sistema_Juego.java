package Service;

import Model.Usuario;
import Manager.JuegoManagerImpl;
import Manager.JuegoManager;
import Model.PeticionCompra;
import org.apache.log4j.Logger;
import org.apache.commons.validator.routines.EmailValidator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.regex.Pattern;

@Api(value = "/juego", description = "Web para Temple Run")
@Path("/juego") // Esta es la ruta base: http://localhost:8080/api/juego
public class Sistema_Juego {

    private final static Logger log = Logger.getLogger(Sistema_Juego.class.getName());

    private JuegoManager manager = JuegoManagerImpl.getInstance();

    private boolean esNuloOVacio(String text){
        return text == null || text.trim().isEmpty();
    }
 
    @POST
    @ApiOperation(value = "Registrar un nuevo usuario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Usuario registrado exitosamente"),
            @ApiResponse(code = 409, message = "El nombre de usuario ya existe"),
            @ApiResponse(code = 400, message = "Faltan campos o el formato es incorrecto")
    })
    @Path("/registro")
    @Consumes(MediaType.APPLICATION_JSON) // Esperara con JSON
    @Produces(MediaType.APPLICATION_JSON) // Respondera cn JSON
    public Response registrarUsuario(Usuario nuevoUsuario) {

        log.info("API REST - Petición de registro para: " + nuevoUsuario.getNombre());
        // Validamos que esten tdos los campos rellenos
        if (esNuloOVacio(nuevoUsuario.getNombre()) ||
                esNuloOVacio(nuevoUsuario.getPassword()) ||
                esNuloOVacio(nuevoUsuario.getCorreo())) {
            log.warn("Registro fallido: Campos en blanco.");
            return Response.status(400).entity("Error: Todos los campos (nombre, password, correo) son obligatorios y no pueden estar vacíos.").build();
        }

        // Validamos formato de correo electrónico
        if (!EmailValidator.getInstance().isValid(nuevoUsuario.getCorreo())) {
            log.warn("Registro fallido: Formato de correo inválido (" + nuevoUsuario.getCorreo() + ").");
            return Response.status(400).entity("Error: El formato del correo electrónico no es válido.").build();
        }

        // Procesar el registro en el Manager
        if (manager.registrarUsuario(nuevoUsuario)) {
            return Response.status(201).entity(nuevoUsuario).build();
        } else {
            return Response.status(409).entity("Error: El nombre de usuario ya está en uso.").build();
        }
    }

    @POST
    @ApiOperation(value = "Iniciar sesión en el juego")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Login exitoso", response = Usuario.class),
            @ApiResponse(code = 400, message = "Campos de login vacíos"),
            @ApiResponse(code = 401, message = "Credenciales incorrectas")
    })
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Usuario credenciales) {
        log.info("API REST - Petición de login para: " + credenciales.getNombre());

        // Validamos q no queden campos en blanco en el login
        if (esNuloOVacio(credenciales.getNombre()) || esNuloOVacio(credenciales.getPassword())) {
            log.warn("Login fallido: Campos en blanco.");
            return Response.status(400).entity("Error: Debes introducir tu nombre de usuario y contraseña.").build();
        }

        Usuario userValidado = manager.procesarLogin(credenciales.getNombre(), credenciales.getPassword());

        if (userValidado != null) {
            return Response.status(200).entity(userValidado).build();
        } else {
            return Response.status(401).entity("Error: Usuario o contraseña incorrectos.").build();
        }
    }

    @POST
    @Path("/comprar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response procesarCompra(PeticionCompra peticion) {

        // 1. Buscamos al usuario en el sistema.
        Usuario jugador = manager.consultarUsuario(peticion.getNombreJugador());

        if (jugador != null) {
            // 2. Comprobamos si tiene suficiente dinero
            if (jugador.getMonedas() >= peticion.getPrecio()) {

                // 3. Le cobramos y le damos el objeto
                jugador.setMonedas(jugador.getMonedas() - peticion.getPrecio());
                jugador.añadirAlInventario(peticion.getNombreObjeto());

                System.out.println("El jugador " + jugador.getNombre() + " ha comprado " + peticion.getNombreObjeto());

                // 200 = Compra exitosa
                return Response.status(200).build();
            } else {
                // 402 = Payment Required (No tiene dinero suficiente)
                return Response.status(402).build();
            }
        }

        // 404 = Usuario no encontrado
        return Response.status(404).build();
    }
}