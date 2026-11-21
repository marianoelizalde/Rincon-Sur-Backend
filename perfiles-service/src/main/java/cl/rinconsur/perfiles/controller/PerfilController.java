package cl.rinconsur.perfiles.controller;

import cl.rinconsur.perfiles.model.Perfil;
import cl.rinconsur.perfiles.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/perfiles")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService servicio;

    @GetMapping
    public List<Perfil> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Perfil obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Perfil crear(@Valid @RequestBody Perfil perfil) {
        return servicio.crear(perfil);
    }

    @PutMapping("/{id}")
    public Perfil actualizar(@PathVariable Long id, @Valid @RequestBody Perfil perfil) {
        return servicio.actualizar(id, perfil);
    }
}
