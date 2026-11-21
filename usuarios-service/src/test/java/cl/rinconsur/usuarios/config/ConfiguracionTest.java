package cl.rinconsur.usuarios.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfiguracionTest {

    @Test
    void laDocumentacionTieneTituloYVersion() {
        OpenAPI api = new SwaggerConfig().customOpenAPI();

        assertEquals("Rincon Sur - Usuarios", api.getInfo().getTitle());
        assertEquals("1.0", api.getInfo().getVersion());
    }

    @Test
    void elCodificadorDeContrasenasUsaBcrypt() {
        PasswordEncoder codificador = new SeguridadConfig().passwordEncoder();
        String hash = codificador.encode("Clave123!");

        assertTrue(codificador.matches("Clave123!", hash));
        assertFalse(codificador.matches("otra", hash));
    }
}
