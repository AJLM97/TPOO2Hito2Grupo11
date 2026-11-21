package edu.unla.epicentro_gourmet.configuration;

import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Epicentro Gourmet API")
                        .version("1.0")
                        .description("Documentación de los endpoints del sistema Epicentro Gourmet")
                        .contact(new Contact()
                                .name("Grupo 11 - Epicentro Gourmet")))
                .addTagsItem(new Tag()
                    .name("Festival")
                    .description("Operaciones sobre festivales"))
                .addTagsItem(new Tag()
                    .name("Unidades de Venta")
                    .description("Operaciones sobre unidades de venta"))
                .addTagsItem(new Tag()
                    .name("Empleado")
                    .description("Operaciones sobre empleados"));
    }
}

