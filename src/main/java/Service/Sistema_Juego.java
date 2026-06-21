package Service;

import Model.*;
import Manager.JuegoManagerImpl;
import Manager.JuegoManager;
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
    public Response obtenerInventario(@PathParam("nombre") String nombreUsuario) {
        // Le pasamos el ID numérico al manager
        List<String> inventario = manager.obtenerInventarioUsuario(nombreUsuario);
        Model.InventarioJugador respuesta = new Model.InventarioJugador(inventario);
        return Response.status(200).entity(respuesta).build();
    }

    @GET
    @Path("/tienda")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerTienda() {
        // Pedimos la lista al manager
        List<Item> items = manager.obtenerItemsTienda();
        Model.TiendaJuego respuesta = new Model.TiendaJuego(items);
        return Response.status(200).entity(respuesta).build();
    }
    @GET
    @Path("usuarios")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerUsuarios() {
        List<User> usuarios = manager.obtenerUsuarios();
        ListaUsuarios respuesta = new ListaUsuarios(usuarios);
        return Response.status(200).entity(respuesta).build();
    }

    @GET
    @Path("/grupos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListaGrupos() {
        List<Grupo> listaGrupos = manager.obtenerGrupos();
        ListaGrupos respuesta = new ListaGrupos(listaGrupos);
        if (respuesta != null) {
            return Response.status(200).entity(respuesta).build();
        } else {
            return Response.status(500).entity("Error al obtener los grupos de la base de datos").build();
        }
    }

    @POST
    @Path("/grupos/{idGrupo}/unirse")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unirseAlGrupo(@PathParam("idGrupo") int idGrupo, User usuario) {

        // Llamamos al manager pasando el nombre del jugador y el ID del grupo
        boolean exito = manager.unirUsuarioAGrupo(usuario.getNombre(), idGrupo);

        if (exito) {
            return Response.status(200).build();
        } else {
            return Response.status(400).entity("Error: No se ha podido unir al grupo.").build();
        }
    }
    @GET
    @Path("/usuarios/{nombre}/grupo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerGrupoUsuario(@PathParam("nombre") String nombre) {
        Model.DetalleGrupo detalle = manager.obtenerDetalleGrupoUsuario(nombre);

        // Retornamos un código 200 con el objeto JSON estructurado
        return Response.status(200).entity(detalle).build();
    }
    @GET
    @Path("/eventos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventos(){

        log.info("Petición de eventos recibida");
        List<Model.Evento> eventos = manager.obtenerListaEventos();

        for (Model.Evento e : eventos) {
            System.out.println("ID = " + e.getId());
            System.out.println("NOMBRE = " + e.getNombre());
            System.out.println("IMAGEN = " + e.getImagen_URL());
            System.out.println("-------------------");
        }

        GenericEntity<List<Model.Evento>> entity = new GenericEntity<List<Model.Evento>>(eventos) {};

        return Response.status(200).entity(entity).build();
    }

    @POST
    @Path("/eventos/inscripcion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response inscribirUsuario(InscripcionRequest request){
        log.info("API REST - Petición de inscripción: " + request.getUsername() + " -> " + request.getIdEvento());

        boolean exito = manager.registrarInscripcion(request);

        if (exito) {
            return Response.status(201).build();
        } else {
            return Response.status(400).entity("No se pudo realizar la inscripción.").build();
        }
    }

    @PUT
    @Path("/usuarios/{nombre}/recompensa/{cantidad}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ganarMonedas(@PathParam("nombre") String nombre, @PathParam("cantidad") int cantidad) {
        log.info("API REST - Otorgando " + cantidad + " monedas al usuario: " + nombre);

        // Llamamos al manager para que sume las monedas en la base de datos
        boolean exito = manager.sumarMonedas(nombre, cantidad);

        if (exito) {
            return Response.status(200).entity("Monedas añadidas correctamente").build();
        } else {
            return Response.status(404).entity("Error: No se ha podido actualizar el saldo del usuario.").build();
        }
    }
    @GET
    @Path("/eventos/{id}/ranking")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRankingEvento(@PathParam("id") String idEvento) {
        log.info("API REST - Solicitando ranking para el evento: " + idEvento);

        List<Model.JugadorRanking> ranking = manager.obtenerRankingEvento(idEvento);
        GenericEntity<List<Model.JugadorRanking>> entity = new GenericEntity<List<Model.JugadorRanking>>(ranking) {};

        return Response.status(200).entity(entity).build();
    }
    @PUT
    @Path("/eventos/{idEvento}/jugadores/{nombreJugador}/puntos/{puntosNuevos}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sumarPuntosEvento(@PathParam("idEvento") String idEvento,
                                      @PathParam("nombreJugador") String nombreJugador,
                                      @PathParam("puntosNuevos") int puntosNuevos) {

        log.info("API REST - Petición del juego: Sumar " + puntosNuevos + " pts a " + nombreJugador + " en " + idEvento);

        boolean exito = manager.sumarPuntosAInscripcion(idEvento, nombreJugador, puntosNuevos);

        if (exito) {
            return Response.status(200).entity("¡Puntuación actualizada con éxito!").build();
        } else {
            return Response.status(404).entity("Error: Jugador no encontrado o no inscrito en el evento.").build();
        }
    }
    @DELETE
    @ApiOperation(value = "Abandonar una misión", notes = "Borra la inscripción de un usuario a un evento")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Inscripción borrada con éxito"),
            @ApiResponse(code = 404, message = "Usuario o inscripción no encontrada"),
            @ApiResponse(code = 500, message = "Error interno del servidor")
    })
    @Path("/eventos/inscripcion/{username}/{idEvento}")
    public Response abandonarEvento(@PathParam("username") String username, @PathParam("idEvento") String idEvento) {
        log.info("API REST - Petición para abandonar misión: " + username + " del evento " + idEvento);

        try {
            // Llamamos a nuestro manager para que se encargue del trabajo sucio
            boolean borrado = this.manager.abandonarEvento(username, idEvento);

            if (borrado) {
                return Response.status(200).build();
            } else {
                return Response.status(404).entity("No se encontró la inscripción para borrar").build();
            }
        } catch (Exception e) {
            log.error("Error al abandonar el evento: ", e);
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/eventos/{idEvento}/usuarios")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsuariosEvento(@PathParam("idEvento") String idEvento) {
        log.info("API REST - Usuarios inscrits al evento: " + idEvento);

        List<User> usuarios = manager.obtenerUsuariosEvento(idEvento);
        GenericEntity<List<User>> entity = new GenericEntity<List<User>>(usuarios) {};

        return Response.status(200).entity(entity).build();
    }

    @POST
    @ApiOperation(value = "Registrar fin de partida", notes = "Actualiza monedas, historial y consume objetos")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Partida registrada correctamente"),
            @ApiResponse(code = 404, message = "Usuario no encontrado"),
            @ApiResponse(code = 500, message = "Error del servidor")
    })
    @Path("/partida/fin")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response finalizarPartida(FinPartidaVO datosPartida) {
        log.info("API REST - Petición de fin de partida recibida de: " + datosPartida.getUsername());

        boolean exito = this.manager.procesarFinPartida(datosPartida);

        if (exito) {
            return Response.status(200).build();
        } else {
            return Response.status(404).entity("Error al procesar la partida. ¿Existe el usuario?").build();
        }
    }

}
