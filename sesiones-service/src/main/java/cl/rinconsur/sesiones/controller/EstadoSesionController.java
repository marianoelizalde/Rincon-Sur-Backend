package cl.rinconsur.sesiones.controller;

import cl.rinconsur.sesiones.model.EstadoSesion;
import cl.rinconsur.sesiones.repository.EstadoSesionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/estados-sesion")
@RequiredArgsConstructor
public class EstadoSesionController {

    private final EstadoSesionRepository repositorio;

    @GetMapping
    public List<EstadoSesion> listar() {
        return repositorio.findAll();
    }
}
