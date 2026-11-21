package cl.rinconsur.usuarios.config;

import cl.rinconsur.usuarios.client.PerfilClient;
import cl.rinconsur.usuarios.dto.RegistroRequest;
import cl.rinconsur.usuarios.repository.UsuarioRepository;
import cl.rinconsur.usuarios.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner cargarUsuarios(UsuarioRepository repositorio, UsuarioService servicio, PerfilClient perfiles) {
        return args -> new Thread(() -> {
            for (int intento = 1; intento <= 12; intento++) {
                try {
                    if (repositorio.count() > 0) return;
                    Long idSupervisor = null;
                    Long idObservador = null;
                    for (var p : perfiles.listar()) {
                        if (p.nombre().equalsIgnoreCase("Supervisor")) idSupervisor = p.id();
                        if (p.nombre().equalsIgnoreCase("Observador")) idObservador = p.id();
                    }
                    if (idSupervisor == null || idObservador == null) return;
                    servicio.registrar(new RegistroRequest("Ana Supervisora", "supervisor@rinconsur.cl", "Admin123!",
                            "11111111-1", "Jefa de turno", "912345678", "Av. Siempre Viva 123", "Santiago", idSupervisor));
                    servicio.registrar(new RegistroRequest("Beto Observador", "observador@rinconsur.cl", "Obser123!",
                            "22222222-2", "Observador de campo", "987654321", "Calle Sur 45", "Maipu", idObservador));
                    System.out.println(">>> Usuarios de prueba creados (supervisor@rinconsur.cl / Admin123!)");
                    return;
                } catch (Exception e) {
                    System.out.println(">>> Usuarios de prueba: intento " + intento + " fallo (" + e.getMessage() + "), reintento en 5 s");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        return;
                    }
                }
            }
        }).start();
    }
}
