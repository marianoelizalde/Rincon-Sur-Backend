package cl.rinconsur.perfiles.controller;

import cl.rinconsur.perfiles.model.Perfil;
import cl.rinconsur.perfiles.service.PerfilService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PerfilControllerTest {

    @Mock
    private PerfilService servicio;

    @InjectMocks
    private PerfilController controlador;

    private final Perfil perfil = Perfil.builder().id(1L).nombre("Supervisor").build();

    @Test
    void listarYObtenerDelegaEnElServicio() {
        when(servicio.listar()).thenReturn(List.of(perfil));
        when(servicio.obtener(1L)).thenReturn(perfil);

        assertEquals(1, controlador.listar().size());
        assertSame(perfil, controlador.obtener(1L));
    }

    @Test
    void crearYActualizarDelegaEnElServicio() {
        when(servicio.crear(perfil)).thenReturn(perfil);
        when(servicio.actualizar(1L, perfil)).thenReturn(perfil);

        assertSame(perfil, controlador.crear(perfil));
        assertSame(perfil, controlador.actualizar(1L, perfil));
    }
}
