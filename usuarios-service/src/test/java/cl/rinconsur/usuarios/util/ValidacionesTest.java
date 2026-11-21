package cl.rinconsur.usuarios.util;

import cl.rinconsur.usuarios.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidacionesTest {

    private void falla(Runnable accion) {
        assertThrows(ReglaNegocioException.class, accion::run);
    }

    @Test
    void rutValidoSeNormalizaYRutConDigitoIncorrectoLanzaError() {
        assertEquals("12345678-5", Validaciones.rut("12.345.678-5"));
        assertThrows(ReglaNegocioException.class, () -> Validaciones.rut("12.345.678-9"));
    }

    @Test
    void rutAceptaDigitosVerificadoresKYCeroYCuerposDeSieteDigitos() {
        assertEquals("10000013-K", Validaciones.rut("10000013-k"));
        assertEquals("10000004-0", Validaciones.rut("10000004-0"));
        assertEquals("1234567-4", Validaciones.rut("1234567-4"));
    }

    @Test
    void rutNuloCortoOConLetrasLanzaError() {
        falla(() -> Validaciones.rut(null));
        falla(() -> Validaciones.rut("5"));
        falla(() -> Validaciones.rut("123456-7"));
        falla(() -> Validaciones.rut("123456789-0"));
        falla(() -> Validaciones.rut("ABCDEFGH-1"));
    }

    @Test
    void nombreValidaLargoYCaracteres() {
        assertEquals("Ana Perez", Validaciones.nombre("  Ana Perez "));
        falla(() -> Validaciones.nombre(null));
        falla(() -> Validaciones.nombre("Al"));
        falla(() -> Validaciones.nombre("A".repeat(81)));
        falla(() -> Validaciones.nombre("Ana 123"));
    }

    @Test
    void correoDebeTenerArrobaYPunto() {
        assertEquals("ana@correo.cl", Validaciones.correo("  Ana@Correo.cl "));
        falla(() -> Validaciones.correo(null));
        falla(() -> Validaciones.correo("anacorreo.cl"));
        falla(() -> Validaciones.correo("ana@correo"));
        falla(() -> Validaciones.correo("a@" + "b".repeat(120) + ".cl"));
    }

    @Test
    void cargoValidaLargo() {
        assertEquals("Jefe", Validaciones.cargo(" Jefe "));
        falla(() -> Validaciones.cargo(null));
        falla(() -> Validaciones.cargo("Jf"));
        falla(() -> Validaciones.cargo("J".repeat(61)));
    }

    @Test
    void telefonoPuedeIrVacioPeroSiVieneDebeTenerNueveDigitos() {
        assertEquals("", Validaciones.telefono(null));
        assertEquals("", Validaciones.telefono("  "));
        assertEquals("912345678", Validaciones.telefono(" 912345678 "));
        falla(() -> Validaciones.telefono("12345"));
        falla(() -> Validaciones.telefono("91234567a"));
    }

    @Test
    void direccionPuedeIrVaciaPeroSiVieneDebeTenerLargoValido() {
        assertEquals("", Validaciones.direccion(null));
        assertEquals("Calle 123", Validaciones.direccion(" Calle 123 "));
        falla(() -> Validaciones.direccion("Abc"));
        falla(() -> Validaciones.direccion("C".repeat(81)));
    }

    @Test
    void comunaPuedeIrVaciaPeroSiVieneDebeSerValida() {
        assertEquals("", Validaciones.comuna(null));
        assertEquals("Santiago", Validaciones.comuna(" Santiago "));
        falla(() -> Validaciones.comuna("Ab"));
        falla(() -> Validaciones.comuna("Santiago 1"));
    }

    @Test
    void contrasenaDebilLanzaErrorYContrasenaSeguraSeAcepta() {
        assertDoesNotThrow(() -> Validaciones.contrasena("Clave123!"));
        falla(() -> Validaciones.contrasena(null));
        falla(() -> Validaciones.contrasena("   "));
        falla(() -> Validaciones.contrasena("Cl1!"));
        falla(() -> Validaciones.contrasena("clave123!"));
        falla(() -> Validaciones.contrasena("CLAVE123!"));
        falla(() -> Validaciones.contrasena("Clave!!!!"));
        falla(() -> Validaciones.contrasena("Clave1234"));
    }
}
