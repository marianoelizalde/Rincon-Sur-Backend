package cl.rinconsur.sesiones.config;

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
                        .title("Rincon Sur - Sesiones")
                        .version("1.0")
                        .description("API de sesiones del sistema Rincon Sur")
                        .contact(new Contact().name("Equipo Rincon Sur")));
    }
}
