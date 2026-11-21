package cl.rinconsur.usuarios.controller;

import cl.rinconsur.usuarios.dto.ActualizarUsuarioRequest;
import cl.rinconsur.usuarios.dto.CambiarContrasenaRequest;
import cl.rinconsur.usuarios.dto.LoginRequest;
import cl.rinconsur.usuarios.dto.RecuperarRequest;
import cl.rinconsur.usuarios.dto.RegistroRequest;
import cl.rinconsur.usuarios.dto.UsuarioResponse;
import cl.rinconsur.usuarios.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioControllerTest {

    @Mock
    private UsuarioService servicio;

    @InjectMocks
    private UsuarioController controlador;

    private final UsuarioResponse respuesta = new UsuarioResponse(2L, "Luis Rojas", "luis@correo.cl", "12345678-5",
            "Observador", "912345678", "Calle 1", "Maipu", true, 2L, "Observador", null);

    @Test
    void consultasDelegaEnElServicio() {
        when(servicio.listar("luis", 2L, true)).thenReturn(List.of(respuesta));
        when(servicio.obtener(2L)).thenReturn(respuesta);

        assertEquals(1, controlador.listar("luis", 2L, true).size());
        assertSame(respuesta, controlador.obtener(2L));
    }

    @Test
    void registrarYLoginDelegaEnElServicio() {
        RegistroRequest registro = new RegistroRequest("Luis Rojas", "luis@correo.cl", "Clave123!", "12345678-5",
                "Observador", "912345678", "Calle 1", "Maipu", 2L);
        LoginRequest login = new LoginRequest("luis@correo.cl", "Clave123!");
        when(servicio.registrar(registro, 1L)).thenReturn(respuesta);
        when(servicio.iniciarSesion(login)).thenReturn(respuesta);

        assertSame(respuesta, controlador.registrar(registro, 1L));
        assertSame(respuesta, controlador.iniciarSesion(login));
    }

    @Test
    void recuperarYCambiarContrasenaLlamanAlServicio() {
        RecuperarRequest recuperar = new RecuperarRequest("luis@correo.cl", "12345678-5", "Nueva123!");
        CambiarContrasenaRequest cambio = new CambiarContrasenaRequest("Clave123!", "Nueva123!");

        controlador.recuperar(recuperar);
        controlador.cambiarContrasena(2L, cambio);

        verify(servicio).recuperar(recuperar);
        verify(servicio).cambiarContrasena(2L, cambio);
    }

    @Test
    void actualizacionesDelegaEnElServicio() {
        ActualizarUsuarioRequest datos = new ActualizarUsuarioRequest("Luis Rojas", "12345678-5", "Jefe",
                "912345678", "Calle 1", "Maipu");
        MockMultipartFile foto = new MockMultipartFile("archivo", "f.png", "image/png", new byte[]{1});
        when(servicio.actualizar(2L, datos, 1L)).thenReturn(respuesta);
        when(servicio.cambiarActivo(2L, false, 1L)).thenReturn(respuesta);
        when(servicio.cambiarPerfil(2L, 1L, 1L)).thenReturn(respuesta);
        when(servicio.subirAvatar(2L, foto, 2L)).thenReturn(respuesta);

        assertSame(respuesta, controlador.actualizar(2L, datos, 1L));
        assertSame(respuesta, controlador.cambiarActivo(2L, false, 1L));
        assertSame(respuesta, controlador.cambiarPerfil(2L, 1L, 1L));
        assertSame(respuesta, controlador.subirAvatar(2L, foto, 2L));
    }

    @Test
    void obtenerAvatarDevuelveLaImagenComoJpeg() {
        when(servicio.obtenerAvatar(2L)).thenReturn(new byte[]{1, 2});

        ResponseEntity<byte[]> resultado = controlador.obtenerAvatar(2L);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(MediaType.IMAGE_JPEG, resultado.getHeaders().getContentType());
        assertEquals(2, resultado.getBody().length);
    }

    @Test
    void eliminarLlamaAlServicioConElActor() {
        controlador.eliminar(2L, 1L);

        verify(servicio).eliminar(2L, 1L);
    }
}
