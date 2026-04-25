package Service;

import Model.Usuario;
import Manager.JuegoManagerImpl;
import Manager.JuegoManager;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "/juego", description = "Web para Temple Run")
@Path("/juego") // Esta es la ruta base: http://localhost:8080/api/juego
public class Sistema_Juego {

    private final static Logger log = Logger.getLogger(Sistema_Juego.class.getName());

    private JuegoManager manager = JuegoManagerImpl.getInstance();

    @POST
    @ApiOperation(value = "Registrar un nuevo usuario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Usuario registrado exitosamente"),
            @ApiResponse(code = 409, message = "El nombre de usuario ya existe")
    })
    @Path("/registro")
    @Consumes(MediaType.APPLICATION_JSON) // Esperara con JSON
    @Produces(MediaType.APPLICATION_JSON) // Respondera cn JSON
    public Response registrarUsuario(Usuario nuevoUsuario) {

        log.info("API REST - Petición de registro para: " + nuevoUsuario.getNombre());

        if (manager.registrarUsuario(nuevoUsuario)) {
            // 201 Se guardo correctamente
            return Response.status(201).entity(nuevoUsuario).header("Access-Control-Allow-Origin","*").build();
        } else {
            // 409 ya existe alguien con ese nombre
            return Response.status(409).entity("Error: El usuario ya existe").build();
        }
    }

    @POST
    @ApiOperation(value = "Iniciar sesión en el juego")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Login exitoso", response = Usuario.class),
            @ApiResponse(code = 401, message = "Credenciales incorrectas")
    })
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Usuario credenciales) {
        log.info("API REST - Petición de login para: " + credenciales.getNombre());

        Usuario userValidado = manager.procesarLogin(credenciales.getNombre(), credenciales.getPassword());

        if (userValidado != null) {
            // 200 OK
            return Response.status(200).entity(userValidado).build();
        } else {
            // 401 Contraseña o nombre falsos
            return Response.status(401).entity("Error: Usuario o contraseña incorrectos").build();
        }
    }
}