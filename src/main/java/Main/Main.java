package Main;

import Service.Sistema_Juego;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
//import org.glassfish.jersey.jackson.JacksonFeature;
import java.net.URI;


import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jersey.listing.ApiListingResourceJSON;

public class Main {
    // La URI base donde escuchará la API
    public static final String BASE_URI = "https://0.0.0.0:8080/api/"; //"http://192.168.10.133:8080/api/";

    public static void main(String[] args) throws Exception {

        final ResourceConfig rc = new ResourceConfig().packages("Service");

        rc.register(io.swagger.jaxrs.listing.ApiListingResource.class);
        rc.register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        BeanConfig beanConfig = new BeanConfig();

        beanConfig.setHost("dsa2.upc.edu");
        beanConfig.setBasePath("/api");
        beanConfig.setContact("support@example.com");
        beanConfig.setDescription("REST API for Tracks Manager");
        beanConfig.setLicenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html");
        beanConfig.setResourcePackage("Service");
        beanConfig.setTermsOfServiceUrl("http://www.example.com/resources/eula");
        beanConfig.setTitle("REST API");
        beanConfig.setVersion("1.0.0");
        beanConfig.setScan(true);


        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        StaticHttpHandler staticHttpHandler = new StaticHttpHandler("./public/");
        server.getServerConfiguration().addHttpHandler(staticHttpHandler, "/");

        System.out.println("----------------------------------------------");
        System.out.println("Templo de Temple Run abierto en: " + BASE_URI);
        System.out.println("Accede a la web en: http://localhost:8080/api/");
        System.out.println("Presiona ENTER para detener el servidor...");
        System.out.println("----------------------------------------------");

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
