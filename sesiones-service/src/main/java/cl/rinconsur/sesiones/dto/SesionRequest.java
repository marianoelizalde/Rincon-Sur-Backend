package cl.rinconsur.sesiones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SesionRequest(
        @NotNull(message = "Elige un acceso") Long accesoId,
        @NotNull(message = "Elige un observador") Long observadorId,
        @NotBlank(message = "Ingresa la fecha") String fecha,
        @NotBlank(message = "Ingresa la hora de inicio") String horaInicio,
        @NotBlank(message = "Ingresa la hora de fin") String horaFin) {
}
