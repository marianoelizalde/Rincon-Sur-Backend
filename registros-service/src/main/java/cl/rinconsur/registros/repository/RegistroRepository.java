package cl.rinconsur.registros.repository;

import cl.rinconsur.registros.model.Registro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroRepository extends JpaRepository<Registro, Long> {
    List<Registro> findBySesionIdOrderByIdAsc(Long sesionId);

    long countBySesionIdAndAnuladoFalse(Long sesionId);
}
