package cl.rinconsur.registros.dto;

public record EstadisticasResponse(
        Long sesionId,
        long total,
        long validos,
        long anulados,
        long transito,
        long visita,
        Double porcentajeTransito,
        long minimoRequerido,
        boolean cumpleMinimo) {
}
