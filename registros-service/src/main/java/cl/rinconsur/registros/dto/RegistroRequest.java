package cl.rinconsur.registros.dto;

import cl.rinconsur.registros.model.Clasificacion;
import cl.rinconsur.registros.model.RangoEtario;
import cl.rinconsur.registros.model.Sexo;
import cl.rinconsur.registros.model.TipoCompania;
import jakarta.validation.constraints.NotNull;

public record RegistroRequest(
        @NotNull(message = "Falta la sesion") Long sesionId,
        @NotNull(message = "Elige la clasificacion (TRANSITO o VISITA)") Clasificacion clasificacion,
        String hora,
        RangoEtario rangoEtario,
        TipoCompania tipoCompania,
        Boolean compraOtraTienda,
        Sexo sexo) {
}
