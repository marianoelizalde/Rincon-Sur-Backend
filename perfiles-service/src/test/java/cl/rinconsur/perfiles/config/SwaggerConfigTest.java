package cl.rinconsur.perfiles.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SwaggerConfigTest {

    @Test
    void laDocumentacionTieneTituloYVersion() {
        OpenAPI api = new SwaggerConfig().customOpenAPI();

        assertEquals("Rincon Sur - Perfiles", api.getInfo().getTitle());
        assertEquals("1.0", api.getInfo().getVersion());
    }
}
