package cl.rinconsur.sesiones.config;

import cl.rinconsur.sesiones.model.Acceso;
import cl.rinconsur.sesiones.model.EstadoSesion;
import cl.rinconsur.sesiones.repository.AccesoRepository;
import cl.rinconsur.sesiones.repository.EstadoSesionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner cargarCatalogos(EstadoSesionRepository estados, AccesoRepository accesos) {
        return args -> {
            if (estados.count() == 0) {
                estados.save(new EstadoSesion(null, "PROGRAMADA", "Programada"));
                estados.save(new EstadoSesion(null, "EN_CURSO", "En curso"));
                estados.save(new EstadoSesion(null, "FINALIZADA", "Finalizada"));
                estados.save(new EstadoSesion(null, "VALIDADA", "Validada"));
                estados.save(new EstadoSesion(null, "RECHAZADA", "Rechazada"));
                estados.save(new EstadoSesion(null, "CANCELADA", "Cancelada"));
            }
            if (accesos.count() == 0) {
                accesos.save(new Acceso(null, "A1", "Principal", "Frente al pasillo central del mall"));
                accesos.save(new Acceso(null, "A2", "Lateral", "Patio de comidas"));
                accesos.save(new Acceso(null, "A3", "Estacionamiento", "Senal demasiado debil"));
                accesos.save(new Acceso(null, "A4", "Conexion", "Hacia la tienda vecina"));
            }
        };
    }
}
