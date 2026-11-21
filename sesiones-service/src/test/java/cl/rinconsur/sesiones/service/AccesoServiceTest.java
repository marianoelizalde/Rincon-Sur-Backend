package cl.rinconsur.sesiones.service;

import cl.rinconsur.sesiones.exception.ConflictoException;
import cl.rinconsur.sesiones.exception.RecursoNoEncontradoException;
import cl.rinconsur.sesiones.model.Acceso;
import cl.rinconsur.sesiones.repository.AccesoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccesoServiceTest {

    @Mock
    private AccesoRepository repositorio;

    @InjectMocks
    private AccesoService servicio;

    @Test
    void listarYObtenerDevuelvenLosAccesos() {
        Acceso acceso = Acceso.builder().id(1L).codigo("A1").nombre("Principal").build();
        when(repositorio.findAll()).thenReturn(List.of(acceso));
        when(repositorio.findById(1L)).thenReturn(Optional.of(acceso));

        assertEquals(1, servicio.listar().size());
        assertEquals("A1", servicio.obtener(1L).getCodigo());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(99L));
    }

    @Test
    void crearNormalizaElCodigoAMayusculasYLoGuarda() {
        when(repositorio.findByCodigoIgnoreCase("B7")).thenReturn(Optional.empty());
        when(repositorio.save(any(Acceso.class))).thenAnswer(i -> i.getArgument(0));

        Acceso guardado = servicio.crear(Acceso.builder().id(50L).codigo(" b7 ").nombre("Norte").build());

        assertEquals("B7", guardado.getCodigo());
        assertNull(guardado.getId());
    }

    @Test
    void crearConCodigoRepetidoLanzaConflicto() {
        Acceso existente = Acceso.builder().id(1L).codigo("A1").build();
        when(repositorio.findByCodigoIgnoreCase("A1")).thenReturn(Optional.of(existente));

        assertThrows(ConflictoException.class,
                () -> servicio.crear(Acceso.builder().codigo("a1").nombre("Otro").build()));
    }

    @Test
    void actualizarCambiaLosDatosDelAcceso() {
        Acceso actual = Acceso.builder().id(1L).codigo("A1").nombre("Principal").build();
        when(repositorio.findById(1L)).thenReturn(Optional.of(actual));
        when(repositorio.findByCodigoIgnoreCase("A1")).thenReturn(Optional.of(actual));
        when(repositorio.save(any(Acceso.class))).thenAnswer(i -> i.getArgument(0));

        Acceso resultado = servicio.actualizar(1L,
                Acceso.builder().codigo(" a1 ").nombre("Nuevo nombre").observacion("Nota").build());

        assertEquals("A1", resultado.getCodigo());
        assertEquals("Nuevo nombre", resultado.getNombre());
        assertEquals("Nota", resultado.getObservacion());
    }

    @Test
    void actualizarConElCodigoDeOtroAccesoLanzaConflicto() {
        Acceso actual = Acceso.builder().id(1L).codigo("A1").build();
        Acceso otro = Acceso.builder().id(2L).codigo("A2").build();
        when(repositorio.findById(1L)).thenReturn(Optional.of(actual));
        when(repositorio.findByCodigoIgnoreCase("A2")).thenReturn(Optional.of(otro));

        assertThrows(ConflictoException.class,
                () -> servicio.actualizar(1L, Acceso.builder().codigo("a2").nombre("X").build()));
    }
}
