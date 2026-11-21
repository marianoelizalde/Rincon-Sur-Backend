package cl.rinconsur.sesiones.model;

import java.time.LocalTime;

public enum Turno {
    MANANA, TARDE, NOCHE;

    public static Turno de(LocalTime horaInicio) {
        if (horaInicio.isBefore(LocalTime.of(14, 0))) return MANANA;
        if (horaInicio.isBefore(LocalTime.of(18, 0))) return TARDE;
        return NOCHE;
    }
}
