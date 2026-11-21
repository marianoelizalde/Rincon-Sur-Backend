package cl.rinconsur.perfiles.service;

import cl.rinconsur.perfiles.exception.ConflictoException;
import cl.rinconsur.perfiles.exception.RecursoNoEncontradoException;
import cl.rinconsur.perfiles.model.Perfil;
import cl.rinconsur.perfiles.repository.PerfilRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PerfilServiceTest {

    @Mock
    private PerfilRepository repositorio;

    @InjectMocks
    private PerfilService servicio;

    @Test
    void listarDevuelveTodosLosPerfiles() {
        when(repositorio.findAll()).thenReturn(List.of(
                Perfil.builder().id(1L).nombre("Supervisor").build(),
                Perfil.builder().id(2L).nombre("Observador").build()));

        assertEquals(2, servicio.listar().size());
    }

    @Test
    void obtenerDevuelveElPerfilExistente() {
        Perfil perfil = Perfil.builder().id(1L).nombre("Supervisor").build();
        when(repositorio.findById(1L)).thenReturn(Optional.of(perfil));

        assertEquals("Supervisor", servicio.obtener(1L).getNombre());
    }

    @Test
    void obtenerPerfilInexistenteLanzaRecursoNoEncontrado() {
        when(repositorio.findById(50L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException error =
                assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(50L));

        assertEquals("No existe el perfil 50", error.getMessage());
    }

    @Test
    void crearPerfilNuevoLimpiaElNombreYLoGuarda() {
        Perfil entrada = Perfil.builder().id(99L).nombre("  Auditor  ").descripcion("Revisa datos").build();
        when(repositorio.findByNombreIgnoreCase("Auditor")).thenReturn(Optional.empty());
        when(repositorio.save(any(Perfil.class))).thenAnswer(i -> i.getArgument(0));

        Perfil guardado = servicio.crear(entrada);

        assertEquals("Auditor", guardado.getNombre());
        assertNull(guardado.getId());
    }

    @Test
    void crearPerfilConNombreRepetidoLanzaConflicto() {
        Perfil existente = Perfil.builder().id(1L).nombre("Supervisor").build();
        when(repositorio.findByNombreIgnoreCase("Supervisor")).thenReturn(Optional.of(existente));

        Perfil nuevo = Perfil.builder().nombre("Supervisor").build();

        assertThrows(ConflictoException.class, () -> servicio.crear(nuevo));
        verify(repositorio, never()).save(any(Perfil.class));
    }

    @Test
    void actualizarCambiaNombreDescripcionYEstado() {
        Perfil actual = Perfil.builder().id(3L).nombre("Viejo").descripcion("a").activo(true).build();
        when(repositorio.findById(3L)).thenReturn(Optional.of(actual));
        when(repositorio.findByNombreIgnoreCase("Nuevo")).thenReturn(Optional.empty());
        when(repositorio.save(any(Perfil.class))).thenAnswer(i -> i.getArgument(0));

        Perfil datos = Perfil.builder().nombre(" Nuevo ").descripcion("b").activo(false).build();
        Perfil resultado = servicio.actualizar(3L, datos);

        assertEquals("Nuevo", resultado.getNombre());
        assertEquals("b", resultado.getDescripcion());
        assertFalse(resultado.isActivo());
    }

    @Test
    void actualizarConElNombreDeOtroPerfilLanzaConflicto() {
        Perfil actual = Perfil.builder().id(3L).nombre("Viejo").build();
        Perfil otro = Perfil.builder().id(4L).nombre("Nuevo").build();
        when(repositorio.findById(3L)).thenReturn(Optional.of(actual));
        when(repositorio.findByNombreIgnoreCase("Nuevo")).thenReturn(Optional.of(otro));

        Perfil datos = Perfil.builder().nombre("Nuevo").build();

        assertThrows(ConflictoException.class, () -> servicio.actualizar(3L, datos));
    }

    @Test
    void actualizarConSuPropioNombreNoLanzaConflicto() {
        Perfil actual = Perfil.builder().id(3L).nombre("Viejo").build();
        when(repositorio.findById(3L)).thenReturn(Optional.of(actual));
        when(repositorio.findByNombreIgnoreCase("Viejo")).thenReturn(Optional.of(actual));
        when(repositorio.save(any(Perfil.class))).thenAnswer(i -> i.getArgument(0));

        Perfil datos = Perfil.builder().nombre("Viejo").descripcion("c").build();

        assertEquals("c", servicio.actualizar(3L, datos).getDescripcion());
    }
}
