package cl.rinconsur.usuarios.client;

import cl.rinconsur.usuarios.dto.PerfilDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "perfiles-service")
public interface PerfilClient {

    @GetMapping("/api/perfiles/{id}")
    PerfilDto obtener(@PathVariable("id") Long id);

    @GetMapping("/api/perfiles")
    List<PerfilDto> listar();
}
