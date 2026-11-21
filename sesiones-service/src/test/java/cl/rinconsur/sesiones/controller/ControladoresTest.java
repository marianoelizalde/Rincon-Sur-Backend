package cl.rinconsur.sesiones.controller;

import cl.rinconsur.sesiones.dto.RechazoRequest;
import cl.rinconsur.sesiones.dto.SesionRequest;
import cl.rinconsur.sesiones.dto.SesionResponse;
import cl.rinconsur.sesiones.model.Acceso;
import cl.rinconsur.sesiones.model.EstadoSesion;
import cl.rinconsur.sesiones.repository.EstadoSesionRepository;
import cl.rinconsur.sesiones.service.AccesoService;
import cl.rinconsur.sesiones.service.SesionService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControladoresTest {

    private final SesionResponse respuesta = new SesionResponse(5L, 1L, "A1", "Principal", "2026-12-01", "10:00",
            "10:30", "MANANA", 2L, "Luis Rojas", "PROGRAMADA", "Programada", null, null, null);

    @Test
    void sesionControllerDelegaConsultasEnElServicio() {
        SesionService servicio = mock(SesionService.class);
        SesionController controlador = new SesionController(servicio);
        when(servicio.listar("2026-12-01", null, null, "TARDE", "EN_CURSO", 2L, 1L)).thenReturn(List.of(respuesta));
        when(servicio.obtener(5L, true)).thenReturn(respuesta);

        assertEquals(1, controlador.listar("2026-12-01", null, null, "TARDE", "EN_CURSO", 2L, 1L).size());
        assertSame(respuesta, controlador.obtener(5L, true));
    }

    @Test
    void sesionControllerDelegaCambiosDeEstadoEnElServicio() {
        SesionService servicio = mock(SesionService.class);
        SesionController controlador = new SesionController(servicio);
        SesionRequest pedido = new SesionRequest(1L, 2L, "2026-12-01", "10:00", "10:30");
        RechazoRequest rechazo = new RechazoRequest(1L, "Datos incompletos");
        when(servicio.programar(pedido)).thenReturn(respuesta);
        when(servicio.iniciar(5L)).thenReturn(respuesta);
        when(servicio.finalizar(5L)).thenReturn(respuesta);
        when(servicio.cancelar(5L)).thenReturn(respuesta);
        when(servicio.validar(5L, 1L)).thenReturn(respuesta);
        when(servicio.rechazar(5L, rechazo)).thenReturn(respuesta);

        assertSame(respuesta, controlador.programar(pedido));
        assertSame(respuesta, controlador.iniciar(5L));
        assertSame(respuesta, controlador.finalizar(5L));
        assertSame(respuesta, controlador.cancelar(5L));
        assertSame(respuesta, controlador.validar(5L, 1L));
        assertSame(respuesta, controlador.rechazar(5L, rechazo));
    }

    @Test
    void accesoControllerDelegaEnElServicio() {
        AccesoService servicio = mock(AccesoService.class);
        AccesoController controlador = new AccesoController(servicio);
        Acceso acceso = Acceso.builder().id(1L).codigo("A1").nombre("Principal").build();
        when(servicio.listar()).thenReturn(List.of(acceso));
        when(servicio.obtener(1L)).thenReturn(acceso);
        when(servicio.crear(acceso)).thenReturn(acceso);
        when(servicio.actualizar(1L, acceso)).thenReturn(acceso);

        assertEquals(1, controlador.listar().size());
        assertSame(acceso, controlador.obtener(1L));
        assertSame(acceso, controlador.crear(acceso));
        assertSame(acceso, controlador.actualizar(1L, acceso));
    }

    @Test
    void estadoSesionControllerListaLosEstadosDeLaBaseDeDatos() {
        EstadoSesionRepository repositorio = mock(EstadoSesionRepository.class);
        when(repositorio.findAll()).thenReturn(List.of(EstadoSesion.builder().id(1L).codigo("PROGRAMADA").etiqueta("Programada").build()));

        assertEquals("PROGRAMADA", new EstadoSesionController(repositorio).listar().get(0).getCodigo());
    }
}
