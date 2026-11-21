package cl.rinconsur.sesiones.service;

import cl.rinconsur.sesiones.model.Acceso;
import cl.rinconsur.sesiones.exception.ConflictoException;
import cl.rinconsur.sesiones.exception.RecursoNoEncontradoException;
import cl.rinconsur.sesiones.repository.AccesoRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccesoService {

    private final AccesoRepository repositorio;

    public List<Acceso> listar() {
        return repositorio.findAll();
    }

    public Acceso obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el acceso " + id));
    }

    public Acceso crear(Acceso acceso) {
        acceso.setId(null);
        acceso.setCodigo(acceso.getCodigo().trim().toUpperCase());
        if (repositorio.findByCodigoIgnoreCase(acceso.getCodigo()).isPresent()) {
            throw new ConflictoException("Ya existe el acceso " + acceso.getCodigo());
        }
        return repositorio.save(acceso);
    }

    public Acceso actualizar(Long id, Acceso datos) {
        Acceso actual = obtener(id);
        String codigo = datos.getCodigo().trim().toUpperCase();
        repositorio.findByCodigoIgnoreCase(codigo).ifPresent(otro -> {
            if (!otro.getId().equals(id)) throw new ConflictoException("Ya existe el acceso " + codigo);
        });
        actual.setCodigo(codigo);
        actual.setNombre(datos.getNombre());
        actual.setObservacion(datos.getObservacion());
        return repositorio.save(actual);
    }
}
