package Main;

import Service.Sistema_Juego;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import java.net.URI;

public class Main {
    // La URI base donde escuchará la API
    public static final String BASE_URI = "http://localhost:8080/api/";

    public static void main(String[] args) throws Exception {

        final ResourceConfig rc = new ResourceConfig()
                .register(Sistema_Juego.class) // Tu servicio
                .register(JacksonFeature.class) // <--- ESTA LÍNEA ARREGLA EL ERROR 415
                .register(CORSFilter.class) // Filtro para conectar la web con el servidor
                .register(io.swagger.jaxrs.listing.ApiListingResource.class) // Swagger
                .register(io.swagger.jaxrs.listing.SwaggerSerializers.class); // Swagger

        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        StaticHttpHandler staticHttpHandler = new StaticHttpHandler("./public/");
        server.getServerConfiguration().addHttpHandler(staticHttpHandler, "/");

        System.out.println("----------------------------------------------");
        System.out.println("Templo de Temple Run abierto en: " + BASE_URI);
        System.out.println("Accede a la web en: http://localhost:8080/index.html");
        System.out.println("Presiona ENTER para detener el servidor...");
        System.out.println("----------------------------------------------");

        System.in.read();
        server.shutdownNow();
    }
}