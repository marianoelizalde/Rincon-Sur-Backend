package cl.rinconsur.sesiones.repository;

import cl.rinconsur.sesiones.model.EstadoSesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoSesionRepository extends JpaRepository<EstadoSesion, Long> {
    Optional<EstadoSesion> findByCodigo(String codigo);
}
