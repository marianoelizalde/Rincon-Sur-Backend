package cl.rinconsur.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo no válido (ej: nombre@correo.cl)")
        @Size(max = 120, message = "El correo no puede pasar de 120 caracteres")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        String contrasena,

        @NotBlank(message = "El RUT es obligatorio")
        String rut,

        @NotBlank(message = "El cargo es obligatorio")
        @Size(min = 3, max = 60, message = "El cargo debe tener entre 3 y 60 caracteres")
        String cargo,

        @Pattern(regexp = "^$|^\\d{9}$", message = "El teléfono debe tener 9 dígitos")
        String telefono,

        @Size(max = 80, message = "La dirección no puede pasar de 80 caracteres")
        String direccion,

        @Size(max = 60, message = "La comuna no puede pasar de 60 caracteres")
        String comuna,

        @NotNull(message = "El perfil es obligatorio")
        Long perfilId
) {
}
