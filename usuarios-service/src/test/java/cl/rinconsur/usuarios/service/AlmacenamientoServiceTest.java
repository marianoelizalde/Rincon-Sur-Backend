package cl.rinconsur.usuarios.service;

import cl.rinconsur.usuarios.exception.RecursoNoEncontradoException;
import cl.rinconsur.usuarios.exception.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlmacenamientoServiceTest {

    @TempDir
    Path temporal;

    private AlmacenamientoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AlmacenamientoService(temporal.resolve("avatares").toString());
    }

    @Test
    void guardarCopiaLaImagenYLeerDevuelveLosMismosBytes() {
        byte[] contenido = {1, 2, 3, 4};
        MockMultipartFile imagen = new MockMultipartFile("archivo", "foto.png", "image/png", contenido);

        String nombre = servicio.guardar(imagen);

        assertTrue(nombre.endsWith(".jpg"));
        assertTrue(Files.exists(temporal.resolve("avatares").resolve(nombre)));
        assertArrayEquals(contenido, servicio.leer(nombre));
    }

    @Test
    void guardarRechazaArchivosVaciosYQueNoSonImagenes() {
        MockMultipartFile vacio = new MockMultipartFile("archivo", "f.png", "image/png", new byte[0]);
        MockMultipartFile texto = new MockMultipartFile("archivo", "f.txt", "text/plain", new byte[]{1});
        MockMultipartFile sinTipo = new MockMultipartFile("archivo", "f", null, new byte[]{1});

        assertThrows(ReglaNegocioException.class, () -> servicio.guardar(vacio));
        assertThrows(ReglaNegocioException.class, () -> servicio.guardar(texto));
        assertThrows(ReglaNegocioException.class, () -> servicio.guardar(sinTipo));
    }

    @Test
    void guardarConErrorDeLecturaLanzaIllegalState() throws IOException {
        MultipartFile roto = mock(MultipartFile.class);
        when(roto.isEmpty()).thenReturn(false);
        when(roto.getContentType()).thenReturn("image/png");
        when(roto.getInputStream()).thenThrow(new IOException("fallo"));

        assertThrows(IllegalStateException.class, () -> servicio.guardar(roto));
    }

    @Test
    void leerUnaImagenQueNoExisteLanzaRecursoNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.leer("no-existe.jpg"));
    }

    @Test
    void eliminarBorraElArchivoYToleraNulosYErrores() throws IOException {
        MockMultipartFile imagen = new MockMultipartFile("archivo", "foto.png", "image/png", new byte[]{1});
        String nombre = servicio.guardar(imagen);

        servicio.eliminar(nombre);
        assertFalse(Files.exists(temporal.resolve("avatares").resolve(nombre)));

        assertDoesNotThrow(() -> servicio.eliminar(null));

        Path carpetaConContenido = temporal.resolve("avatares").resolve("dir");
        Files.createDirectories(carpetaConContenido);
        Files.writeString(carpetaConContenido.resolve("x.txt"), "x");
        assertDoesNotThrow(() -> servicio.eliminar("dir"));
    }

    @Test
    void siLaCarpetaNoSePuedeCrearLanzaIllegalState() throws IOException {
        Path archivo = temporal.resolve("archivo.txt");
        Files.writeString(archivo, "x");

        assertThrows(IllegalStateException.class,
                () -> new AlmacenamientoService(archivo.resolve("sub").toString()));
    }
}
