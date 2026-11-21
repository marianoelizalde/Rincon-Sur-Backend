package cl.rinconsur.perfiles;

import cl.rinconsur.perfiles.model.Perfil;
import cl.rinconsur.perfiles.repository.PerfilRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner cargarPerfiles(PerfilRepository repositorio) {
        return args -> {
            if (repositorio.count() == 0) {
                repositorio.save(new Perfil(null, "Supervisor", "Programa sesiones, valida y administra usuarios", true));
                repositorio.save(new Perfil(null, "Observador", "Registra personas durante una sesion", true));
            }
        };
    }
}
