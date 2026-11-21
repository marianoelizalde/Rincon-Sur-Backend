package cl.rinconsur.usuarios.dto;

public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        String rut,
        String cargo,
        String telefono,
        String direccion,
        String comuna,
        boolean activo,
        Long perfilId,
        String perfilNombre,
        String avatarUrl
) {
}
