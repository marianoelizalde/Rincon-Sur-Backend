package cl.rinconsur.sesiones.controller;

import cl.rinconsur.sesiones.model.Acceso;
import cl.rinconsur.sesiones.service.AccesoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/accesos")
@RequiredArgsConstructor
public class AccesoController {

    private final AccesoService servicio;

    @GetMapping
    public List<Acceso> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Acceso obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Acceso crear(@Valid @RequestBody Acceso acceso) {
        return servicio.crear(acceso);
    }

    @PutMapping("/{id}")
    public Acceso actualizar(@PathVariable Long id, @Valid @RequestBody Acceso acceso) {
        return servicio.actualizar(id, acceso);
    }
}
