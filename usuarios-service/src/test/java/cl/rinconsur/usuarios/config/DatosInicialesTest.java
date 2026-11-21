package cl.rinconsur.usuarios.config;

import cl.rinconsur.usuarios.client.PerfilClient;
import cl.rinconsur.usuarios.dto.PerfilDto;
import cl.rinconsur.usuarios.dto.RegistroRequest;
import cl.rinconsur.usuarios.repository.UsuarioRepository;
import cl.rinconsur.usuarios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatosInicialesTest {

    private UsuarioRepository repositorio;
    private UsuarioService servicio;
    private PerfilClient perfiles;
    private CommandLineRunner ejecutor;

    @BeforeEach
    void preparar() {
        repositorio = mock(UsuarioRepository.class);
        servicio = mock(UsuarioService.class);
        perfiles = mock(PerfilClient.class);
        ejecutor = new DatosIniciales().cargarUsuarios(repositorio, servicio, perfiles);
    }

    @Test
    void sinUsuariosYConAmbosPerfilesCreaSupervisorYObservador() throws Exception {
        when(repositorio.count()).thenReturn(0L);
        when(perfiles.listar()).thenReturn(List.of(
                new PerfilDto(1L, "Supervisor", "", true),
                new PerfilDto(2L, "Observador", "", true)));

        ejecutor.run();

        verify(servicio, timeout(5000).times(2)).registrar(any(RegistroRequest.class));
    }

    @Test
    void conUsuariosExistentesNoCreaNada() throws Exception {
        when(repositorio.count()).thenReturn(3L);

        ejecutor.run();

        verify(repositorio, timeout(5000)).count();
        verify(perfiles, never()).listar();
    }

    @Test
    void sinLosPerfilesNecesariosNoCreaUsuarios() throws Exception {
        when(repositorio.count()).thenReturn(0L);
        when(perfiles.listar()).thenReturn(List.of(new PerfilDto(1L, "Supervisor", "", true)));

        ejecutor.run();

        verify(perfiles, timeout(5000)).listar();
        verify(servicio, never()).registrar(any(RegistroRequest.class));
    }
}
