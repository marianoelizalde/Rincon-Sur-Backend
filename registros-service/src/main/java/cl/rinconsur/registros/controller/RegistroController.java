package cl.rinconsur.registros.controller;

import cl.rinconsur.registros.dto.EstadisticasResponse;
import cl.rinconsur.registros.dto.RegistroRequest;
import cl.rinconsur.registros.model.Registro;
import cl.rinconsur.registros.service.RegistroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/registros")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService servicio;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Registro registrar(@Valid @RequestBody RegistroRequest request,
                              @RequestHeader(value = "X-Usuario-Id", required = false) Long idActor) {
        return servicio.registrar(request, idActor);
    }

    @PatchMapping("/{id}/anular")
    public Registro anular(@PathVariable Long id,
                           @RequestHeader(value = "X-Usuario-Id", required = false) Long idActor) {
        return servicio.anular(id, idActor);
    }

    @GetMapping("/sesion/{sesionId}")
    public List<Registro> listarPorSesion(@PathVariable Long sesionId,
                                          @RequestParam(required = false) Boolean soloValidos) {
        return servicio.listarPorSesion(sesionId, soloValidos);
    }

    @GetMapping("/sesion/{sesionId}/validos/count")
    public long contarValidos(@PathVariable Long sesionId) {
        return servicio.contarValidos(sesionId);
    }

    @GetMapping("/sesion/{sesionId}/estadisticas")
    public EstadisticasResponse estadisticas(@PathVariable Long sesionId) {
        return servicio.estadisticas(sesionId);
    }

    @PostMapping("/sesion/{sesionId}/deshacer-ultimo")
    public Registro deshacerUltimo(@PathVariable Long sesionId,
                                   @RequestHeader(value = "X-Usuario-Id", required = false) Long idActor) {
        return servicio.deshacerUltimo(sesionId, idActor);
    }
}
