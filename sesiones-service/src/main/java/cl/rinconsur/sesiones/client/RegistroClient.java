package cl.rinconsur.sesiones.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "registros-service")
public interface RegistroClient {

    @GetMapping("/api/registros/sesion/{idSesion}/validos/count")
    Long contarValidos(@PathVariable("idSesion") Long idSesion);
}
