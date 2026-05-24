package Service;

import Model.Item;
import Model.User;
import Manager.JuegoManagerImpl;
import Manager.JuegoManager;
import Model.PeticionCompra;
import org.apache.log4j.Logger;
import org.apache.commons.validator.routines.EmailValidator;
import BDD.orm.dao.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;
import java.util.*;

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

    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    private boolean esPasswordSeguro(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

 
    @POST
    @ApiOperation(value = "Registrar un nuevo usuario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Usuario registrado exitosamente"),
            @ApiResponse(code = 409, message = "El nombre de usuario ya existe"),
            @ApiResponse(code = 400, message = "Faltan campos o el formato es incorrecto")
    })
    @Path("/registro")
    @Consumes(MediaType.APPLICATION_JSON) // Esperará con JSON
    @Produces(MediaType.APPLICATION_JSON) // Responderá cn JSON
    public Response registrarUsuario(User nuevoUsuario) {

        log.info("API REST - Petición de registro para: " + nuevoUsuario.getNombre());
        // Validamos que estén todos los campos rellenos
        if (esNuloOVacio(nuevoUsuario.getNombre()) ||
                esNuloOVacio(nuevoUsuario.getPassword()) ||
                esNuloOVacio(nuevoUsuario.getMail())) {
            log.warn("Registro fallido: Campos en blanco.");
            return Response.status(400).entity("Error: Todos los campos (nombre, password, correo) son obligatorios y no pueden estar vacíos.").build();
        }

        // Validamos formato de correo electrónico
        if (!EmailValidator.getInstance().isValid(nuevoUsuario.getMail())) {
            log.warn("Registro fallido: Formato de correo inválido (" + nuevoUsuario.getMail() + ").");
            return Response.status(400).entity("Error: El formato del correo electrónico no es válido.").build();
        }
        // Validamos el formato del correo
        if (!EmailValidator.getInstance().isValid(nuevoUsuario.getMail())) {
            log.warn("Registro fallido: Formato de correo inválido (" + nuevoUsuario.getMail() + ").");
            return Response.status(400).entity("Error: El formato del correo electrónico no es válido.").build();
        }     

        // Seguridad de la contraseña
        if (!esPasswordSeguro(nuevoUsuario.getPassword())) {
            log.warn("Registro fallido: Contraseña débil para el usuario " + nuevoUsuario.getNombre());
            return Response.status(400).entity("Error: La contraseña es demasiado débil. " +
                    "Debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial (@#$%^&+=!).").build();
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
            @ApiResponse(code = 200, message = "Login exitoso", response = User.class),
            @ApiResponse(code = 400, message = "Campos de login vacíos"),
            @ApiResponse(code = 401, message = "Credenciales incorrectas")
    })
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(User credenciales) {
        log.info("API REST - Petición de login para: " + credenciales.getNombre());

        // Validamos q no queden campos en blanco en el login
        if (esNuloOVacio(credenciales.getNombre()) || esNuloOVacio(credenciales.getPassword())) {
            log.warn("Login fallido: Campos en blanco.");
            return Response.status(400).entity("Error: Debes introducir tu nombre de usuario y contraseña.").build();
        }

        User userValidado = manager.procesarLogin(credenciales.getNombre(), credenciales.getPassword());

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
        boolean compraOk = manager.comprarObjeto(peticion.getNombreJugador(), peticion.getNombreObjeto());

        if (compraOk) {
            return Response.status(200).build(); // 200 OK
        } else {
            return Response.status(402).entity("Error: Fondos insuficientes o error de datos.").build();
        }
    }

    @GET
    @Path("/inventario/{nombre}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerInventario(@PathParam("nombre") String nombre) {
        List<String> inventario = manager.obtenerInventarioUsuario(nombre);
        Model.InventarioJugador respuesta = new Model.InventarioJugador(inventario);
        return Response.status(200).entity(respuesta).build();
    }

    @GET
    @Path("/tienda")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerTienda() {
        // Pedimos la lista al manager
        List<Item> items = manager.obtenerItemsTienda();
        // La devolvemos con un código 200 (OK). Java/Jersey la pasará a JSON automáticamente.
        return Response.status(200).entity(items).build();
    }
    @GET
    @Path("usuarios")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerUsuarios() {
        List<User> usuarios = manager.obtenerUsuarios();
        return Response.status(200).entity(usuarios).build();
    }
}