package cl.rinconsur.sesiones.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioDto(Long id, String nombre, boolean activo, Long perfilId, String perfilNombre) {
}
