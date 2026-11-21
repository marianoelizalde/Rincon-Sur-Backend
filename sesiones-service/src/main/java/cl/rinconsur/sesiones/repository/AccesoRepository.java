package cl.rinconsur.sesiones.repository;

import cl.rinconsur.sesiones.model.Acceso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccesoRepository extends JpaRepository<Acceso, Long> {
    Optional<Acceso> findByCodigoIgnoreCase(String codigo);
}
