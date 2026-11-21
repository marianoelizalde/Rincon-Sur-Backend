package cl.rinconsur.registros.service;

import cl.rinconsur.registros.client.SesionClient;
import cl.rinconsur.registros.dto.EstadisticasResponse;
import cl.rinconsur.registros.dto.RegistroRequest;
import cl.rinconsur.registros.dto.SesionDto;
import cl.rinconsur.registros.exception.RecursoNoEncontradoException;
import cl.rinconsur.registros.exception.ReglaNegocioException;
import cl.rinconsur.registros.model.Clasificacion;
import cl.rinconsur.registros.model.RangoEtario;
import cl.rinconsur.registros.model.Registro;
import cl.rinconsur.registros.model.Sexo;
import cl.rinconsur.registros.model.TipoCompania;
import cl.rinconsur.registros.repository.RegistroRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegistroServiceTest {

    @Mock
    private RegistroRepository repositorio;

    @Mock
    private SesionClient sesionClient;

    private RegistroService servicio;

    @BeforeEach
    void preparar() {
        servicio = new RegistroService(repositorio, sesionClient, "America/Santiago");
        when(sesionClient.obtener(7L)).thenReturn(new SesionDto(7L, "EN_CURSO", 3L));
        when(sesionClient.obtener(8L)).thenReturn(new SesionDto(8L, "FINALIZADA", 3L));
        when(sesionClient.obtener(99L)).thenThrow(mock(FeignException.NotFound.class));
        when(repositorio.save(any(Registro.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Registro registro(Long id, Clasificacion clasificacion, boolean anulado) {
        return Registro.builder().id(id).sesionId(7L).hora("10:15")
                .clasificacion(clasificacion).anulado(anulado).build();
    }

    private RegistroRequest pedido(Long sesionId, String hora) {
        return new RegistroRequest(sesionId, Clasificacion.TRANSITO, hora, RangoEtario.DE_18_A_25,
                TipoCompania.SOLO, true, Sexo.FEMENINO);
    }

    @Test
    void registrarGuardaElRegistroConTodosSusDatos() {
        Registro guardado = servicio.registrar(pedido(7L, "09:05"), 3L);

        assertEquals(7L, guardado.getSesionId());
        assertEquals("09:05", guardado.getHora());
        assertEquals(Clasificacion.TRANSITO, guardado.getClasificacion());
        assertEquals(RangoEtario.DE_18_A_25, guardado.getRangoEtario());
        assertEquals(TipoCompania.SOLO, guardado.getTipoCompania());
        assertEquals(Sexo.FEMENINO, guardado.getSexo());
        assertTrue(guardado.isCompraOtraTienda());
        assertFalse(guardado.isAnulado());
    }

    @Test
    void registrarSinHoraUsaLaHoraActualYSinCompraLaDejaEnFalso() {
        RegistroRequest sinHora = new RegistroRequest(7L, Clasificacion.VISITA, null, null, null, null, null);

        Registro guardado = servicio.registrar(sinHora, null);

        assertTrue(guardado.getHora().matches("\\d{2}:\\d{2}"));
        assertFalse(guardado.isCompraOtraTienda());
    }

    @Test
    void registrarConHoraInvalidaLanzaReglaDeNegocio() {
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(pedido(7L, "25:99"), 3L));
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(pedido(7L, "mediodia"), 3L));
        verify(repositorio, never()).save(any(Registro.class));
    }

    @Test
    void registrarEnSesionInexistenteLanzaRecursoNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.registrar(pedido(99L, "09:05"), 3L));
    }

    @Test
    void registrarEnUnaSesionQueNoEstaEnCursoLanzaReglaDeNegocio() {
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(pedido(8L, "09:05"), null));
        verify(repositorio, never()).save(any(Registro.class));
    }

    @Test
    void soloElObservadorDeLaSesionPuedeRegistrar() {
        ReglaNegocioException error =
                assertThrows(ReglaNegocioException.class, () -> servicio.registrar(pedido(7L, "09:05"), 4L));

        assertEquals("Solo el observador de la sesion puede registrar personas", error.getMessage());
    }

    @Test
    void anularMarcaElRegistroComoAnulado() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(registro(1L, Clasificacion.TRANSITO, false)));

        assertTrue(servicio.anular(1L, 3L).isAnulado());
    }

    @Test
    void anularUnRegistroYaAnuladoOInexistenteLanzaError() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(registro(1L, Clasificacion.TRANSITO, true)));

        assertThrows(ReglaNegocioException.class, () -> servicio.anular(1L, 3L));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.anular(50L, 3L));
    }

    @Test
    void anularEnUnaSesionFinalizadaLanzaReglaDeNegocio() {
        Registro deOtraSesion = registro(2L, Clasificacion.TRANSITO, false);
        deOtraSesion.setSesionId(8L);
        when(repositorio.findById(2L)).thenReturn(Optional.of(deOtraSesion));

        assertThrows(ReglaNegocioException.class, () -> servicio.anular(2L, 3L));
    }

    @Test
    void deshacerUltimoAnulaElUltimoRegistroValidoDeLaSesion() {
        Registro primero = registro(1L, Clasificacion.TRANSITO, false);
        Registro segundo = registro(2L, Clasificacion.VISITA, false);
        Registro tercero = registro(3L, Clasificacion.VISITA, true);
        when(repositorio.findBySesionIdOrderByIdAsc(7L)).thenReturn(List.of(primero, segundo, tercero));

        Registro anulado = servicio.deshacerUltimo(7L, 3L);

        assertEquals(2L, anulado.getId());
        assertTrue(anulado.isAnulado());
        assertFalse(primero.isAnulado());
    }

    @Test
    void deshacerUltimoSinRegistrosValidosLanzaReglaDeNegocio() {
        when(repositorio.findBySesionIdOrderByIdAsc(7L)).thenReturn(List.of(registro(1L, Clasificacion.TRANSITO, true)));

        assertThrows(ReglaNegocioException.class, () -> servicio.deshacerUltimo(7L, 3L));
    }

    @Test
    void listarPorSesionPuedeDevolverTodosOSoloLosValidos() {
        when(repositorio.findBySesionIdOrderByIdAsc(7L)).thenReturn(List.of(
                registro(1L, Clasificacion.TRANSITO, false),
                registro(2L, Clasificacion.VISITA, true)));

        assertEquals(2, servicio.listarPorSesion(7L, null).size());
        assertEquals(2, servicio.listarPorSesion(7L, false).size());
        assertEquals(1, servicio.listarPorSesion(7L, true).size());
    }

    @Test
    void contarValidosDelegaEnElRepositorio() {
        when(repositorio.countBySesionIdAndAnuladoFalse(7L)).thenReturn(12L);

        assertEquals(12L, servicio.contarValidos(7L));
    }

    @Test
    void estadisticasCalculanElPorcentajeDeTransitoSoloConRegistrosValidos() {
        when(repositorio.findBySesionIdOrderByIdAsc(7L)).thenReturn(List.of(
                registro(1L, Clasificacion.TRANSITO, false),
                registro(2L, Clasificacion.TRANSITO, false),
                registro(3L, Clasificacion.VISITA, false),
                registro(4L, Clasificacion.VISITA, true)));

        EstadisticasResponse resultado = servicio.estadisticas(7L);

        assertEquals(7L, resultado.sesionId());
        assertEquals(4, resultado.total());
        assertEquals(3, resultado.validos());
        assertEquals(1, resultado.anulados());
        assertEquals(2, resultado.transito());
        assertEquals(1, resultado.visita());
        assertEquals(200.0 / 3, resultado.porcentajeTransito(), 0.001);
        assertEquals(30, resultado.minimoRequerido());
        assertFalse(resultado.cumpleMinimo());
    }

    @Test
    void estadisticasSinRegistrosNoTienenPorcentaje() {
        when(repositorio.findBySesionIdOrderByIdAsc(7L)).thenReturn(List.of());

        EstadisticasResponse resultado = servicio.estadisticas(7L);

        assertNull(resultado.porcentajeTransito());
        assertEquals(0, resultado.total());
        assertFalse(resultado.cumpleMinimo());
    }

    @Test
    void conTreintaRegistrosValidosSeCumpleElMinimo() {
        List<Registro> treinta = new ArrayList<>();
        for (long i = 1; i <= 30; i++) {
            treinta.add(registro(i, Clasificacion.VISITA, false));
        }
        when(repositorio.findBySesionIdOrderByIdAsc(7L)).thenReturn(treinta);

        EstadisticasResponse resultado = servicio.estadisticas(7L);

        assertTrue(resultado.cumpleMinimo());
        assertEquals(0.0, resultado.porcentajeTransito(), 0.001);
    }
}
