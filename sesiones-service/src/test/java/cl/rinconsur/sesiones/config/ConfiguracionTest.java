package cl.rinconsur.sesiones.config;

import cl.rinconsur.sesiones.model.Acceso;
import cl.rinconsur.sesiones.model.EstadoSesion;
import cl.rinconsur.sesiones.repository.AccesoRepository;
import cl.rinconsur.sesiones.repository.EstadoSesionRepository;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfiguracionTest {

    @Test
    void laDocumentacionTieneTituloYVersion() {
        OpenAPI api = new SwaggerConfig().customOpenAPI();

        assertEquals("Rincon Sur - Sesiones", api.getInfo().getTitle());
        assertEquals("1.0", api.getInfo().getVersion());
    }

    @Test
    void conTablasVaciasCreaLosSeisEstadosYLosCuatroAccesos() throws Exception {
        EstadoSesionRepository estados = mock(EstadoSesionRepository.class);
        AccesoRepository accesos = mock(AccesoRepository.class);
        when(estados.count()).thenReturn(0L);
        when(accesos.count()).thenReturn(0L);

        new DatosIniciales().cargarCatalogos(estados, accesos).run();

        verify(estados, times(6)).save(any(EstadoSesion.class));
        verify(accesos, times(4)).save(any(Acceso.class));
    }

    @Test
    void conTablasLlenasNoCreaNada() throws Exception {
        EstadoSesionRepository estados = mock(EstadoSesionRepository.class);
        AccesoRepository accesos = mock(AccesoRepository.class);
        when(estados.count()).thenReturn(6L);
        when(accesos.count()).thenReturn(4L);

        new DatosIniciales().cargarCatalogos(estados, accesos).run();

        verify(estados, never()).save(any(EstadoSesion.class));
        verify(accesos, never()).save(any(Acceso.class));
    }
}
