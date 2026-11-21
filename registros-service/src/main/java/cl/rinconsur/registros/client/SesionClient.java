package cl.rinconsur.registros.client;

import cl.rinconsur.registros.dto.SesionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "sesiones-service")
public interface SesionClient {

    @GetMapping("/api/sesiones/{id}")
    SesionDto obtener(@PathVariable("id") Long id);
}
