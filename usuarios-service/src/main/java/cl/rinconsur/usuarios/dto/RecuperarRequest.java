package cl.rinconsur.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record RecuperarRequest(
        @NotBlank(message = "El correo es obligatorio")
        String correo,

        @NotBlank(message = "El RUT es obligatorio")
        String rut,

        @NotBlank(message = "La contraseña nueva es obligatoria")
        String nuevaContrasena
) {
}
