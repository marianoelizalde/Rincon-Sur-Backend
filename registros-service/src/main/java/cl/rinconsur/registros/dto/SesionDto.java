package cl.rinconsur.registros.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SesionDto(Long id, String estado, Long observadorId) {
}
