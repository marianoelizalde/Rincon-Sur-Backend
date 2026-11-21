package cl.rinconsur.sesiones.dto;

import cl.rinconsur.sesiones.model.Sesion;
import cl.rinconsur.sesiones.model.Turno;

import java.time.format.DateTimeFormatter;

public record SesionResponse(
        Long id,
        Long accesoId,
        String accesoCodigo,
        String accesoNombre,
        String fecha,
        String horaInicio,
        String horaFin,
        String turno,
        Long observadorId,
        String observador,
        String estado,
        String estadoEtiqueta,
        Long validadorId,
        String motivoRechazo,
        Long registrosValidos) {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public static SesionResponse de(Sesion s, String nombreObservador, Long registrosValidos) {
        return new SesionResponse(
                s.getId(),
                s.getAcceso().getId(),
                s.getAcceso().getCodigo(),
                s.getAcceso().getNombre(),
                s.getFecha().toString(),
                s.getHoraInicio().format(HORA),
                s.getHoraFin().format(HORA),
                Turno.de(s.getHoraInicio()).name(),
                s.getObservadorId(),
                nombreObservador,
                s.getEstado().getCodigo(),
                s.getEstado().getEtiqueta(),
                s.getValidadorId(),
                s.getMotivoRechazo(),
                registrosValidos);
    }
}
