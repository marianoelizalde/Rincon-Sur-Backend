package cl.rinconsur.sesiones.client;

import cl.rinconsur.sesiones.dto.UsuarioDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "usuarios-service")
public interface UsuarioClient {

    @GetMapping("/api/usuarios/{id}")
    UsuarioDto obtener(@PathVariable("id") Long id);
}
