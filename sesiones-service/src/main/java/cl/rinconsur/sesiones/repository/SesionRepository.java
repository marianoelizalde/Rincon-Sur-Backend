package cl.rinconsur.sesiones.repository;

import cl.rinconsur.sesiones.model.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SesionRepository extends JpaRepository<Sesion, Long> {
    List<Sesion> findByAccesoIdAndFecha(Long accesoId, LocalDate fecha);
}
