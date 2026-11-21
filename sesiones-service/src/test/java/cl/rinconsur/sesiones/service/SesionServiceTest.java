package cl.rinconsur.sesiones.service;

import cl.rinconsur.sesiones.client.RegistroClient;
import cl.rinconsur.sesiones.client.UsuarioClient;
import cl.rinconsur.sesiones.dto.RechazoRequest;
import cl.rinconsur.sesiones.dto.SesionRequest;
import cl.rinconsur.sesiones.dto.SesionResponse;
import cl.rinconsur.sesiones.dto.UsuarioDto;
import cl.rinconsur.sesiones.exception.ConflictoException;
import cl.rinconsur.sesiones.exception.RecursoNoEncontradoException;
import cl.rinconsur.sesiones.exception.ReglaNegocioException;
import cl.rinconsur.sesiones.model.Acceso;
import cl.rinconsur.sesiones.model.EstadoSesion;
import cl.rinconsur.sesiones.model.Sesion;
import cl.rinconsur.sesiones.repository.AccesoRepository;
import cl.rinconsur.sesiones.repository.EstadoSesionRepository;
import cl.rinconsur.sesiones.repository.SesionRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SesionServiceTest {

    private static final String ZONA = "America/Santiago";

    @Mock
    private SesionRepository sesiones;

    @Mock
    private AccesoRepository accesos;

    @Mock
    private EstadoSesionRepository estados;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private RegistroClient registroClient;

    private SesionService servicio;

    private final Acceso acceso = Acceso.builder().id(1L).codigo("A1").nombre("Principal").build();
    private final Acceso otroAcceso = Acceso.builder().id(2L).codigo("A2").nombre("Lateral").build();
    private final LocalDate hoy = LocalDate.now(ZoneId.of(ZONA));
    private final LocalDate manana = hoy.plusDays(1);

    @BeforeEach
    void preparar() {
        servicio = new SesionService(sesiones, accesos, estados, usuarioClient, registroClient, ZONA);
        when(accesos.findById(1L)).thenReturn(Optional.of(acceso));
        when(estados.findByCodigo(anyString())).thenAnswer(i -> Optional.of(estado(i.getArgument(0))));
        when(sesiones.save(any(Sesion.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioClient.obtener(1L)).thenReturn(new UsuarioDto(1L, "Sofia Diaz", true, 1L, "Supervisor"));
        when(usuarioClient.obtener(2L)).thenReturn(new UsuarioDto(2L, "Luis Rojas", true, 2L, "Observador"));
        when(usuarioClient.obtener(3L)).thenReturn(new UsuarioDto(3L, "Inactivo", false, 2L, "Observador"));
        when(usuarioClient.obtener(4L)).thenReturn(new UsuarioDto(4L, "Sup inactivo", false, 1L, "Supervisor"));
        when(usuarioClient.obtener(6L)).thenReturn(new UsuarioDto(6L, "Sin perfil", true, null, null));
        when(usuarioClient.obtener(7L)).thenThrow(mock(FeignException.NotFound.class));
        when(usuarioClient.obtener(9L)).thenReturn(new UsuarioDto(9L, "Otro Obs", true, 2L, "Observador"));
    }

    private EstadoSesion estado(String codigo) {
        return EstadoSesion.builder().codigo(codigo).etiqueta(codigo).build();
    }

    private Sesion sesion(Long id, LocalDate fecha, String inicio, String fin, String codigoEstado) {
        return Sesion.builder().id(id).acceso(acceso).fecha(fecha)
                .horaInicio(LocalTime.parse(inicio)).horaFin(LocalTime.parse(fin))
                .observadorId(2L).estado(estado(codigoEstado)).build();
    }

    private SesionRequest pedido(Long accesoId, Long observadorId, LocalDate fecha, String inicio, String fin) {
        return new SesionRequest(accesoId, observadorId, fecha.toString(), inicio, fin);
    }

    private void reglaDeNegocio(SesionRequest pedido) {
        assertThrows(ReglaNegocioException.class, () -> servicio.programar(pedido));
    }

    @Test
    void listarSinFiltrosOrdenaDeLaMasNuevaALaMasAntigua() {
        LocalDate d1 = LocalDate.of(2026, 12, 1);
        LocalDate d2 = LocalDate.of(2026, 12, 2);
        Sesion s3 = sesion(3L, d2, "19:00", "19:30", "FINALIZADA");
        s3.setAcceso(otroAcceso);
        s3.setObservadorId(9L);
        when(sesiones.findAll()).thenReturn(List.of(
                sesion(1L, d1, "10:00", "10:30", "PROGRAMADA"),
                sesion(2L, d1, "15:00", "15:30", "EN_CURSO"),
                s3));

        List<SesionResponse> lista = servicio.listar(null, null, null, null, null, null, null);

        assertEquals(List.of(3L, 2L, 1L), lista.stream().map(SesionResponse::id).toList());
        assertEquals("Otro Obs", lista.get(0).observador());
        assertEquals("A2", lista.get(0).accesoCodigo());
        assertEquals("Lateral", lista.get(0).accesoNombre());
        assertEquals("NOCHE", lista.get(0).turno());
        assertEquals("2026-12-02", lista.get(0).fecha());
        assertEquals("19:00", lista.get(0).horaInicio());
        assertEquals("19:30", lista.get(0).horaFin());
        assertEquals("FINALIZADA", lista.get(0).estadoEtiqueta());
    }

    @Test
    void listarAplicaCadaFiltro() {
        LocalDate d1 = LocalDate.of(2026, 12, 1);
        LocalDate d2 = LocalDate.of(2026, 12, 2);
        Sesion s3 = sesion(3L, d2, "19:00", "19:30", "FINALIZADA");
        s3.setAcceso(otroAcceso);
        s3.setObservadorId(9L);
        when(sesiones.findAll()).thenReturn(List.of(
                sesion(1L, d1, "10:00", "10:30", "PROGRAMADA"),
                sesion(2L, d1, "15:00", "15:30", "EN_CURSO"),
                s3));

        assertEquals(2, servicio.listar("2026-12-01", null, null, null, null, null, null).size());
        assertEquals(1, servicio.listar(null, "2026-12-02", null, null, null, null, null).size());
        assertEquals(2, servicio.listar(null, null, "2026-12-01", null, null, null, null).size());
        assertEquals(2L, servicio.listar(null, null, null, "tarde", null, null, null).get(0).id());
        assertEquals(3L, servicio.listar(null, null, null, "NOCHE", null, null, null).get(0).id());
        assertEquals(2L, servicio.listar(null, null, null, null, "en_curso", null, null).get(0).id());
        assertEquals(3L, servicio.listar(null, null, null, null, null, 9L, null).get(0).id());
        assertEquals(3L, servicio.listar(null, null, null, null, null, null, 2L).get(0).id());
        assertEquals(3, servicio.listar(" ", " ", " ", " ", " ", null, null).size());
    }

    @Test
    void listarConFechaOTurnoInvalidoLanzaReglaDeNegocio() {
        assertThrows(ReglaNegocioException.class, () -> servicio.listar("2026-13-45", null, null, null, null, null, null));
        assertThrows(ReglaNegocioException.class, () -> servicio.listar(null, null, null, "MEDIODIA", null, null, null));
    }

    @Test
    void listarSinNombreDeObservadorSiUsuariosNoResponde() {
        when(sesiones.findAll()).thenReturn(List.of(sesion(1L, hoy, "10:00", "10:30", "PROGRAMADA")));
        when(usuarioClient.obtener(2L)).thenThrow(mock(FeignException.class));

        assertNull(servicio.listar(null, null, null, null, null, null, null).get(0).observador());
    }

    @Test
    void obtenerPuedeIncluirElConteoDeRegistrosValidos() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(5L, hoy, "10:00", "10:30", "FINALIZADA")));
        when(sesiones.findById(6L)).thenReturn(Optional.of(sesion(6L, hoy, "11:00", "11:30", "FINALIZADA")));
        when(registroClient.contarValidos(5L)).thenReturn(12L);
        when(registroClient.contarValidos(6L)).thenReturn(null);

        assertNull(servicio.obtener(5L, false).registrosValidos());
        assertEquals(12L, servicio.obtener(5L, true).registrosValidos());
        assertEquals(0L, servicio.obtener(6L, true).registrosValidos());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(99L, false));
    }

    @Test
    void programarGuardaLaSesionComoProgramada() {
        SesionResponse respuesta = servicio.programar(pedido(1L, 2L, manana, "10:00", "10:30"));

        assertEquals("PROGRAMADA", respuesta.estado());
        assertEquals("Luis Rojas", respuesta.observador());
        assertEquals("MANANA", respuesta.turno());
        assertEquals(1L, respuesta.accesoId());
        assertEquals(2L, respuesta.observadorId());
        assertNull(respuesta.validadorId());
        assertNull(respuesta.motivoRechazo());
    }

    @Test
    void programarIgnoraSesionesCanceladasYPermiteHorariosContiguos() {
        when(sesiones.findByAccesoIdAndFecha(1L, manana)).thenReturn(List.of(
                sesion(8L, manana, "10:00", "11:00", "CANCELADA"),
                sesion(9L, manana, "11:30", "12:00", "PROGRAMADA")));

        assertEquals("PROGRAMADA", servicio.programar(pedido(1L, 2L, manana, "11:00", "11:30")).estado());
    }

    @Test
    void programarEnUnHorarioOcupadoDelMismoAccesoLanzaConflicto() {
        when(sesiones.findByAccesoIdAndFecha(1L, manana)).thenReturn(List.of(
                sesion(8L, manana, "10:00", "11:00", "PROGRAMADA")));

        assertThrows(ConflictoException.class, () -> servicio.programar(pedido(1L, 2L, manana, "10:30", "11:00")));
    }

    @Test
    void programarConFechaOHorasInvalidasLanzaReglaDeNegocio() {
        reglaDeNegocio(pedido(1L, 2L, hoy.minusDays(1), "10:00", "10:30"));
        reglaDeNegocio(pedido(1L, 2L, manana, "09:30", "10:30"));
        reglaDeNegocio(pedido(1L, 2L, manana, "20:30", "21:30"));
        reglaDeNegocio(pedido(1L, 2L, manana, "10:00", "10:20"));
        reglaDeNegocio(pedido(1L, 2L, manana, "10:00", "11:30"));
        reglaDeNegocio(new SesionRequest(1L, 2L, "ayer", "10:00", "10:30"));
        reglaDeNegocio(new SesionRequest(1L, 2L, manana.toString(), "25:00", "10:30"));
    }

    @Test
    void programarConAccesoInexistenteLanzaRecursoNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.programar(pedido(99L, 2L, manana, "10:00", "10:30")));
    }

    @Test
    void programarConObservadorNoValidoLanzaReglaDeNegocio() {
        reglaDeNegocio(pedido(1L, 7L, manana, "10:00", "10:30"));
        reglaDeNegocio(pedido(1L, 3L, manana, "10:00", "10:30"));
        reglaDeNegocio(pedido(1L, 1L, manana, "10:00", "10:30"));
        reglaDeNegocio(pedido(1L, 6L, manana, "10:00", "10:30"));
    }

    @Test
    void iniciarUnaSesionDeHoyDentroDeSuHorarioLaPoneEnCurso() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(5L, hoy, "00:00", "23:59", "PROGRAMADA")));

        assertEquals("EN_CURSO", servicio.iniciar(5L).estado());
    }

    @Test
    void iniciarUnaSesionDeOtroDiaLanzaReglaDeNegocio() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(5L, manana, "10:00", "11:00", "PROGRAMADA")));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class, () -> servicio.iniciar(5L));

        assertEquals("Esta sesion es para el " + manana, error.getMessage());
    }

    @Test
    void iniciarAntesDeLaHoraOLuegoDeQueTermineLanzaReglaDeNegocio() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(5L, hoy, "23:59", "23:59", "PROGRAMADA")));
        when(sesiones.findById(6L)).thenReturn(Optional.of(sesion(6L, hoy, "00:00", "00:01", "PROGRAMADA")));

        assertThrows(ReglaNegocioException.class, () -> servicio.iniciar(5L));
        assertThrows(ReglaNegocioException.class, () -> servicio.iniciar(6L));
    }

    @Test
    void iniciarUnaSesionQueNoEstaProgramadaLanzaReglaDeNegocio() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(5L, hoy, "00:00", "23:59", "EN_CURSO")));

        assertThrows(ReglaNegocioException.class, () -> servicio.iniciar(5L));
    }

    @Test
    void finalizarSoloFuncionaConUnaSesionEnCurso() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(5L, hoy, "10:00", "10:30", "EN_CURSO")));
        when(sesiones.findById(6L)).thenReturn(Optional.of(sesion(6L, hoy, "10:00", "10:30", "PROGRAMADA")));

        assertEquals("FINALIZADA", servicio.finalizar(5L).estado());
        assertThrows(ReglaNegocioException.class, () -> servicio.finalizar(6L));
    }

    @Test
    void cancelarSoloFuncionaConUnaSesionProgramada() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(5L, hoy, "10:00", "10:30", "PROGRAMADA")));
        when(sesiones.findById(6L)).thenReturn(Optional.of(sesion(6L, hoy, "10:00", "10:30", "EN_CURSO")));

        assertEquals("CANCELADA", servicio.cancelar(5L).estado());
        assertThrows(ReglaNegocioException.class, () -> servicio.cancelar(6L));
    }

    @Test
    void siFaltaElEstadoEnLaBaseDeDatosLanzaRecursoNoEncontrado() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(5L, hoy, "10:00", "10:30", "EN_CURSO")));
        when(estados.findByCodigo("FINALIZADA")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.finalizar(5L));
    }

    @Test
    void validarConMenosDe30RegistrosValidosDejaLaSesionRechazada() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(5L, hoy, "10:00", "11:00", "FINALIZADA")));
        when(registroClient.contarValidos(5L)).thenReturn(10L);

        SesionResponse respuesta = servicio.validar(5L, 1L);

        assertEquals("RECHAZADA", respuesta.estado());
        assertEquals("Muestra insuficiente", respuesta.motivoRechazo());
        assertEquals(10L, respuesta.registrosValidos());
        assertEquals(1L, respuesta.validadorId());
    }

    @Test
    void validarConAlMenos30RegistrosValidosDejaLaSesionValidada() {
        Sesion finalizada = sesion(5L, hoy, "10:00", "11:00", "FINALIZADA");
        finalizada.setMotivoRechazo("antiguo");
        when(sesiones.findById(5L)).thenReturn(Optional.of(finalizada));
        when(registroClient.contarValidos(5L)).thenReturn(30L);

        SesionResponse respuesta = servicio.validar(5L, 1L);

        assertEquals("VALIDADA", respuesta.estado());
        assertNull(respuesta.motivoRechazo());
        assertEquals(30L, respuesta.registrosValidos());
    }

    @Test
    void validarConUnValidadorNoPermitidoLanzaReglaDeNegocio() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(5L, hoy, "10:00", "11:00", "FINALIZADA")));
        when(sesiones.findById(6L)).thenReturn(Optional.of(sesion(6L, hoy, "10:00", "11:00", "EN_CURSO")));

        assertThrows(ReglaNegocioException.class, () -> servicio.validar(6L, 1L));
        assertThrows(ReglaNegocioException.class, () -> servicio.validar(5L, null));
        assertThrows(ReglaNegocioException.class, () -> servicio.validar(5L, 2L));
        assertThrows(ReglaNegocioException.class, () -> servicio.validar(5L, 7L));
        assertThrows(ReglaNegocioException.class, () -> servicio.validar(5L, 4L));
        assertThrows(ReglaNegocioException.class, () -> servicio.validar(5L, 9L));
        assertThrows(ReglaNegocioException.class, () -> servicio.validar(5L, 6L));
    }

    @Test
    void rechazarGuardaElMotivoYElValidador() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(5L, hoy, "10:00", "11:00", "FINALIZADA")));

        SesionResponse respuesta = servicio.rechazar(5L, new RechazoRequest(1L, "  Datos incompletos  "));

        assertEquals("RECHAZADA", respuesta.estado());
        assertEquals("Datos incompletos", respuesta.motivoRechazo());
        assertEquals(1L, respuesta.validadorId());
    }

    @Test
    void rechazarConMotivoVacioOCortoLanzaReglaDeNegocio() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(5L, hoy, "10:00", "11:00", "FINALIZADA")));

        assertThrows(ReglaNegocioException.class, () -> servicio.rechazar(5L, new RechazoRequest(1L, null)));
        assertThrows(ReglaNegocioException.class, () -> servicio.rechazar(5L, new RechazoRequest(1L, "   ")));
        assertThrows(ReglaNegocioException.class, () -> servicio.rechazar(5L, new RechazoRequest(1L, "abc")));
    }

    @Test
    void rechazarUnaSesionNoFinalizadaOConValidadorInvalidoLanzaReglaDeNegocio() {
        when(sesiones.findById(5L)).thenReturn(Optional.of(sesion(5L, hoy, "10:00", "11:00", "FINALIZADA")));
        when(sesiones.findById(6L)).thenReturn(Optional.of(sesion(6L, hoy, "10:00", "11:00", "EN_CURSO")));

        assertThrows(ReglaNegocioException.class, () -> servicio.rechazar(6L, new RechazoRequest(1L, "Motivo largo")));
        assertThrows(ReglaNegocioException.class, () -> servicio.rechazar(5L, new RechazoRequest(2L, "Motivo largo")));
    }
}
