package cl.rinconsur.registros.controller;

import cl.rinconsur.registros.dto.EstadisticasResponse;
import cl.rinconsur.registros.dto.RegistroRequest;
import cl.rinconsur.registros.model.Clasificacion;
import cl.rinconsur.registros.model.Registro;
import cl.rinconsur.registros.service.RegistroService;
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
class RegistroControllerTest {

    @Mock
    private RegistroService servicio;

    @InjectMocks
    private RegistroController controlador;

    private final Registro registro = Registro.builder().id(1L).sesionId(7L).hora("10:00")
            .clasificacion(Clasificacion.TRANSITO).build();

    @Test
    void registrarAnularYDeshacerDelegaEnElServicio() {
        RegistroRequest pedido = new RegistroRequest(7L, Clasificacion.TRANSITO, "10:00", null, null, false, null);
        when(servicio.registrar(pedido, 3L)).thenReturn(registro);
        when(servicio.anular(1L, 3L)).thenReturn(registro);
        when(servicio.deshacerUltimo(7L, 3L)).thenReturn(registro);

        assertSame(registro, controlador.registrar(pedido, 3L));
        assertSame(registro, controlador.anular(1L, 3L));
        assertSame(registro, controlador.deshacerUltimo(7L, 3L));
    }

    @Test
    void consultasDelegaEnElServicio() {
        EstadisticasResponse estadisticas = new EstadisticasResponse(7L, 1, 1, 0, 1, 0, 100.0, 30, false);
        when(servicio.listarPorSesion(7L, true)).thenReturn(List.of(registro));
        when(servicio.contarValidos(7L)).thenReturn(1L);
        when(servicio.estadisticas(7L)).thenReturn(estadisticas);

        assertEquals(1, controlador.listarPorSesion(7L, true).size());
        assertEquals(1L, controlador.contarValidos(7L));
        assertSame(estadisticas, controlador.estadisticas(7L));
    }
}
