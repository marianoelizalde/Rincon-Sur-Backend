package cl.rinconsur.perfiles.service;

import cl.rinconsur.perfiles.model.Perfil;
import cl.rinconsur.perfiles.exception.ConflictoException;
import cl.rinconsur.perfiles.exception.RecursoNoEncontradoException;
import cl.rinconsur.perfiles.repository.PerfilRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final PerfilRepository repositorio;

    public List<Perfil> listar() {
        return repositorio.findAll();
    }

    public Perfil obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el perfil " + id));
    }

    public Perfil crear(Perfil perfil) {
        perfil.setId(null);
        perfil.setNombre(perfil.getNombre().trim());
        if (repositorio.findByNombreIgnoreCase(perfil.getNombre()).isPresent()) {
            throw new ConflictoException("Ya existe un perfil llamado " + perfil.getNombre());
        }
        return repositorio.save(perfil);
    }

    public Perfil actualizar(Long id, Perfil datos) {
        Perfil actual = obtener(id);
        String nombre = datos.getNombre().trim();
        repositorio.findByNombreIgnoreCase(nombre).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw new ConflictoException("Ya existe un perfil llamado " + nombre);
            }
        });
        actual.setNombre(nombre);
        actual.setDescripcion(datos.getDescripcion());
        actual.setActivo(datos.isActivo());
        return repositorio.save(actual);
    }
}
