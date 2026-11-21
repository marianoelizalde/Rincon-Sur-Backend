package cl.rinconsur.perfiles.repository;

import cl.rinconsur.perfiles.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByNombreIgnoreCase(String nombre);
}
