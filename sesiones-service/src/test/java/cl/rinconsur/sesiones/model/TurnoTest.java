package cl.rinconsur.sesiones.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TurnoTest {

    @Test
    void laHoraDeInicioDefineElTurno() {
        assertEquals(Turno.MANANA, Turno.de(LocalTime.of(13, 59)));
        assertEquals(Turno.TARDE, Turno.de(LocalTime.of(14, 0)));
        assertEquals(Turno.NOCHE, Turno.de(LocalTime.of(18, 0)));
    }
}
