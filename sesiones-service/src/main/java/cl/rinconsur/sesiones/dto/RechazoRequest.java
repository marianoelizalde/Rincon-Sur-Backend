package cl.rinconsur.sesiones.dto;

import jakarta.validation.constraints.NotNull;

public record RechazoRequest(
        @NotNull(message = "Falta el validador") Long idValidador,
        String motivo) {
}
