package cl.rinconsur.perfiles;

import cl.rinconsur.perfiles.model.Perfil;
import cl.rinconsur.perfiles.repository.PerfilRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatosInicialesTest {

    @Test
    void conTablaVaciaCreaLosPerfilesSupervisorYObservador() throws Exception {
        PerfilRepository repositorio = mock(PerfilRepository.class);
        when(repositorio.count()).thenReturn(0L);

        CommandLineRunner ejecutor = new DatosIniciales().cargarPerfiles(repositorio);
        ejecutor.run();

        verify(repositorio, times(2)).save(any(Perfil.class));
    }

    @Test
    void conPerfilesExistentesNoCreaNada() throws Exception {
        PerfilRepository repositorio = mock(PerfilRepository.class);
        when(repositorio.count()).thenReturn(2L);

        new DatosIniciales().cargarPerfiles(repositorio).run();

        verify(repositorio, never()).save(any(Perfil.class));
    }
}
