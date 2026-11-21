package cl.rinconsur.sesiones.exception;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler manejador = new GlobalExceptionHandler();

    private void verificar(ResponseEntity<Map<String, String>> respuesta, HttpStatus estado, String mensaje) {
        assertEquals(estado, respuesta.getStatusCode());
        assertEquals(mensaje, respuesta.getBody().get("message"));
    }

    private FeignException feign(int estado) {
        FeignException error = mock(FeignException.class);
        when(error.status()).thenReturn(estado);
        return error;
    }

    @Test
    void erroresDeNegocioDevuelvenSuCodigoHttp() {
        verificar(manejador.noEncontrado(new RecursoNoEncontradoException("no existe")), HttpStatus.NOT_FOUND, "no existe");
        verificar(manejador.regla(new ReglaNegocioException("regla")), HttpStatus.BAD_REQUEST, "regla");
        verificar(manejador.conflicto(new ConflictoException("repetido")), HttpStatus.CONFLICT, "repetido");
    }

    @Test
    void validacionDevuelveElPrimerMensajeODatosNoValidos() {
        BindingResult conError = new BeanPropertyBindingResult(new Object(), "sesion");
        conError.addError(new FieldError("sesion", "fecha", "Ingresa la fecha"));
        MethodArgumentNotValidException primero = mock(MethodArgumentNotValidException.class);
        when(primero.getBindingResult()).thenReturn(conError);
        verificar(manejador.validacion(primero), HttpStatus.BAD_REQUEST, "Ingresa la fecha");

        MethodArgumentNotValidException segundo = mock(MethodArgumentNotValidException.class);
        when(segundo.getBindingResult()).thenReturn(new BeanPropertyBindingResult(new Object(), "sesion"));
        verificar(manejador.validacion(segundo), HttpStatus.BAD_REQUEST, "Datos no validos");
    }

    @Test
    void peticionMalFormadaYErrorInternoTienenSuMensaje() {
        verificar(manejador.peticionMala(new RuntimeException("x")), HttpStatus.BAD_REQUEST,
                "Peticion mal formada o con datos faltantes");
        verificar(manejador.general(new RuntimeException("boom")), HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno: boom");
    }

    @Test
    void erroresDeOtrosServiciosSeTraducenSegunSuEstado() {
        verificar(manejador.feign(feign(404)), HttpStatus.NOT_FOUND, "Recurso de otro servicio no encontrado");
        verificar(manejador.feign(feign(400)), HttpStatus.BAD_REQUEST, "Otro servicio rechazo la peticion");
        verificar(manejador.feign(feign(409)), HttpStatus.BAD_REQUEST, "Otro servicio rechazo la peticion");
        verificar(manejador.feign(feign(500)), HttpStatus.SERVICE_UNAVAILABLE, "Un servicio necesario no esta disponible");
    }
}
