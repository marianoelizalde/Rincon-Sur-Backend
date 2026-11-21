package cl.rinconsur.sesiones.controller;

import cl.rinconsur.sesiones.dto.RechazoRequest;
import cl.rinconsur.sesiones.dto.SesionRequest;
import cl.rinconsur.sesiones.dto.SesionResponse;
import cl.rinconsur.sesiones.service.SesionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
public class SesionController {

    private final SesionService servicio;

    @GetMapping
    public List<SesionResponse> listar(@RequestParam(required = false) String fecha,
                                       @RequestParam(required = false) String desde,
                                       @RequestParam(required = false) String hasta,
                                       @RequestParam(required = false) String turno,
                                       @RequestParam(required = false) String estado,
                                       @RequestParam(required = false) Long observadorId,
                                       @RequestParam(required = false) Long accesoId) {
        return servicio.listar(fecha, desde, hasta, turno, estado, observadorId, accesoId);
    }

    @GetMapping("/{id}")
    public SesionResponse obtener(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean conteo) {
        return servicio.obtener(id, conteo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SesionResponse programar(@Valid @RequestBody SesionRequest request) {
        return servicio.programar(request);
    }

    @PatchMapping("/{id}/iniciar")
    public SesionResponse iniciar(@PathVariable Long id) {
        return servicio.iniciar(id);
    }

    @PatchMapping("/{id}/finalizar")
    public SesionResponse finalizar(@PathVariable Long id) {
        return servicio.finalizar(id);
    }

    @PatchMapping("/{id}/cancelar")
    public SesionResponse cancelar(@PathVariable Long id) {
        return servicio.cancelar(id);
    }

    @PatchMapping("/{id}/validar")
    public SesionResponse validar(@PathVariable Long id, @RequestParam Long idValidador) {
        return servicio.validar(id, idValidador);
    }

    @PatchMapping("/{id}/rechazar")
    public SesionResponse rechazar(@PathVariable Long id, @Valid @RequestBody RechazoRequest request) {
        return servicio.rechazar(id, request);
    }
}
