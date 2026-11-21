package cl.rinconsur.perfiles.exception;

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

    @Test
    void erroresDeNegocioDevuelvenSuCodigoHttp() {
        verificar(manejador.noEncontrado(new RecursoNoEncontradoException("no existe")), HttpStatus.NOT_FOUND, "no existe");
        verificar(manejador.regla(new ReglaNegocioException("regla")), HttpStatus.BAD_REQUEST, "regla");
        verificar(manejador.conflicto(new ConflictoException("repetido")), HttpStatus.CONFLICT, "repetido");
    }

    @Test
    void validacionDevuelveElPrimerMensajeDeCampo() {
        BindingResult resultado = new BeanPropertyBindingResult(new Object(), "perfil");
        resultado.addError(new FieldError("perfil", "nombre", "El nombre del perfil es obligatorio"));
        MethodArgumentNotValidException error = mock(MethodArgumentNotValidException.class);
        when(error.getBindingResult()).thenReturn(resultado);

        verificar(manejador.validacion(error), HttpStatus.BAD_REQUEST, "El nombre del perfil es obligatorio");
    }

    @Test
    void validacionSinCamposDevuelveMensajeGenerico() {
        BindingResult resultado = new BeanPropertyBindingResult(new Object(), "perfil");
        MethodArgumentNotValidException error = mock(MethodArgumentNotValidException.class);
        when(error.getBindingResult()).thenReturn(resultado);

        verificar(manejador.validacion(error), HttpStatus.BAD_REQUEST, "Datos no validos");
    }

    @Test
    void peticionMalFormadaYErrorInternoTienenSuMensaje() {
        verificar(manejador.peticionMala(new RuntimeException("x")), HttpStatus.BAD_REQUEST,
                "Peticion mal formada o con datos faltantes");
        verificar(manejador.general(new RuntimeException("boom")), HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno: boom");
    }
}
