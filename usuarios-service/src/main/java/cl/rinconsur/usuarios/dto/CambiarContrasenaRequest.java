package cl.rinconsur.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record CambiarContrasenaRequest(
        @NotBlank(message = "La contraseña actual es obligatoria")
        String actual,

        @NotBlank(message = "La contraseña nueva es obligatoria")
        String nueva
) {
}
