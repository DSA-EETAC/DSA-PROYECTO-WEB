package Service;

import org.glassfish.jersey.server.ResourceConfig;
import io.swagger.jaxrs.config.BeanConfig;

public class AplicacionWeb extends ResourceConfig {

    public AplicacionWeb() {
        // Jersey busca etiquetas web dentro de paquete "Service"
        packages("Service");

        // Registramos las clases internas de Swagger para que pueda leer nuestro código
        register(io.swagger.jaxrs.listing.ApiListingResource.class);
        register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        // configuramos detalles visuales y conexión de Swagger
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/api");
        beanConfig.setResourcePackage("Service");
        beanConfig.setScan(true);
    }
}
