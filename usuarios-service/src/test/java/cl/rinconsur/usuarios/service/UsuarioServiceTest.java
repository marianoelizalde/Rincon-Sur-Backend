package cl.rinconsur.usuarios.service;

import cl.rinconsur.usuarios.client.PerfilClient;
import cl.rinconsur.usuarios.dto.ActualizarUsuarioRequest;
import cl.rinconsur.usuarios.dto.CambiarContrasenaRequest;
import cl.rinconsur.usuarios.dto.LoginRequest;
import cl.rinconsur.usuarios.dto.PerfilDto;
import cl.rinconsur.usuarios.dto.RecuperarRequest;
import cl.rinconsur.usuarios.dto.RegistroRequest;
import cl.rinconsur.usuarios.dto.UsuarioResponse;
import cl.rinconsur.usuarios.exception.AccesoDenegadoException;
import cl.rinconsur.usuarios.exception.ConflictoException;
import cl.rinconsur.usuarios.exception.NoAutorizadoException;
import cl.rinconsur.usuarios.exception.RecursoNoEncontradoException;
import cl.rinconsur.usuarios.exception.ReglaNegocioException;
import cl.rinconsur.usuarios.model.Usuario;
import cl.rinconsur.usuarios.repository.UsuarioRepository;
import feign.FeignException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilClient perfilClient;

    @Mock
    private AlmacenamientoService almacenamientoService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService servicio;

    private Usuario supervisor;
    private Usuario observador;

    @BeforeEach
    void preparar() {
        supervisor = Usuario.builder().id(1L).nombre("Sofia Diaz").correo("sofia@correo.cl").contrasena("HASH")
                .rut("10000013-K").perfilId(1L).activo(true).build();
        observador = Usuario.builder().id(2L).nombre("Luis Rojas").correo("luis@correo.cl").contrasena("HASH")
                .rut("12345678-5").cargo("Observador de campo").perfilId(2L).activo(true).build();
        PerfilDto perfilSupervisor = new PerfilDto(1L, "Supervisor", "Administra", true);
        PerfilDto perfilObservador = new PerfilDto(2L, "Observador", "Registra", true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(supervisor));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(observador));
        when(usuarioRepository.findAll()).thenReturn(List.of(supervisor, observador));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(perfilClient.obtener(1L)).thenReturn(perfilSupervisor);
        when(perfilClient.obtener(2L)).thenReturn(perfilObservador);
        when(perfilClient.listar()).thenReturn(List.of(perfilSupervisor, perfilObservador));
        when(passwordEncoder.encode(anyString())).thenReturn("NUEVO_HASH");
        when(passwordEncoder.matches("Clave123!", "HASH")).thenReturn(true);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void limpiar() {
        RequestContextHolder.resetRequestAttributes();
    }

    private RegistroRequest datosRegistro(Long perfilId) {
        return new RegistroRequest("Ana Perez", "Ana@Correo.cl", "Clave123!", "12.345.678-5",
                "Observadora", "912345678", "Calle 123", "Santiago", perfilId);
    }

    private ActualizarUsuarioRequest datosActualizacion() {
        return new ActualizarUsuarioRequest("Luis Rojas Soto", "12.345.678-5", "Jefe de campo", "912345678",
                "Calle Sur 45", "Maipu");
    }

    private FeignException feign() {
        return mock(FeignException.class);
    }

    @Test
    void listarSinFiltrosOrdenaPorNombreYTraeElNombreDelPerfil() {
        List<UsuarioResponse> lista = servicio.listar(null, null, null);

        assertEquals(2, lista.size());
        assertEquals("Luis Rojas", lista.get(0).nombre());
        assertEquals("Observador", lista.get(0).perfilNombre());
        assertEquals("Sofia Diaz", lista.get(1).nombre());
    }

    @Test
    void listarFiltraPorTextoPerfilYEstado() {
        assertEquals(1, servicio.listar("sofia", null, null).size());
        assertEquals(1, servicio.listar("CORREO.CL", 2L, null).size());
        assertEquals(1, servicio.listar("campo", null, null).size());
        assertEquals(1, servicio.listar("12345678", null, null).size());
        assertEquals(0, servicio.listar(null, null, false).size());
        assertEquals(2, servicio.listar("  ", null, true).size());
    }

    @Test
    void listarSinPerfilesDisponiblesDejaElNombreDelPerfilVacio() {
        when(perfilClient.listar()).thenThrow(feign());

        List<UsuarioResponse> lista = servicio.listar(null, null, null);

        assertEquals("", lista.get(0).perfilNombre());
    }

    @Test
    void obtenerDevuelveElUsuarioYSiNoExisteLanzaError() {
        assertEquals("luis@correo.cl", servicio.obtener(2L).correo());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(99L));
    }

    @Test
    void obtenerConErrorEnPerfilesDevuelvePerfilVacio() {
        when(perfilClient.obtener(2L)).thenThrow(feign());

        assertEquals("", servicio.obtener(2L).perfilNombre());
    }

    @Test
    void registrarGuardaElUsuarioConLaContrasenaCodificada() {
        when(usuarioRepository.findByCorreoIgnoreCase("ana@correo.cl")).thenReturn(Optional.empty());
        when(usuarioRepository.findByRut("12345678-5")).thenReturn(Optional.empty());

        UsuarioResponse respuesta = servicio.registrar(datosRegistro(2L));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("NUEVO_HASH", captor.getValue().getContrasena());
        assertTrue(captor.getValue().isActivo());
        assertEquals("ana@correo.cl", respuesta.correo());
        assertEquals("12345678-5", respuesta.rut());
        assertEquals("Observadora", respuesta.cargo());
        assertEquals("912345678", respuesta.telefono());
        assertEquals("Calle 123", respuesta.direccion());
        assertEquals("Santiago", respuesta.comuna());
        assertEquals(2L, respuesta.perfilId());
        assertEquals("Observador", respuesta.perfilNombre());
        assertNull(respuesta.avatarUrl());
    }

    @Test
    void registrarSinPerfilLanzaReglaDeNegocio() {
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(datosRegistro(null)));
    }

    @Test
    void registrarConCorreoORutRepetidoLanzaConflicto() {
        when(usuarioRepository.findByCorreoIgnoreCase("ana@correo.cl")).thenReturn(Optional.of(observador));
        assertThrows(ConflictoException.class, () -> servicio.registrar(datosRegistro(2L)));

        when(usuarioRepository.findByCorreoIgnoreCase("ana@correo.cl")).thenReturn(Optional.empty());
        when(usuarioRepository.findByRut("12345678-5")).thenReturn(Optional.of(observador));
        assertThrows(ConflictoException.class, () -> servicio.registrar(datosRegistro(2L)));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void registrarConPerfilDesactivadoOInexistenteLanzaReglaDeNegocio() {
        when(perfilClient.obtener(3L)).thenReturn(new PerfilDto(3L, "Auditor", "", false));
        ReglaNegocioException desactivado =
                assertThrows(ReglaNegocioException.class, () -> servicio.registrar(datosRegistro(3L)));
        assertEquals("El perfil está desactivado", desactivado.getMessage());

        when(perfilClient.obtener(9L)).thenThrow(mock(FeignException.NotFound.class));
        ReglaNegocioException inexistente =
                assertThrows(ReglaNegocioException.class, () -> servicio.registrar(datosRegistro(9L)));
        assertEquals("El perfil indicado no existe", inexistente.getMessage());
    }

    @Test
    void registrarConActorSoloFuncionaSiElActorEsSupervisor() {
        assertEquals("ana@correo.cl", servicio.registrar(datosRegistro(2L), 1L).correo());
        assertThrows(AccesoDenegadoException.class, () -> servicio.registrar(datosRegistro(2L), 2L));
    }

    @Test
    void actualizarPermiteAlPropioUsuarioYAlSupervisor() {
        UsuarioResponse propio = servicio.actualizar(2L, datosActualizacion(), 2L);
        assertEquals("Luis Rojas Soto", propio.nombre());
        assertEquals("Jefe de campo", propio.cargo());
        assertEquals("Maipu", propio.comuna());

        assertEquals("Luis Rojas Soto", servicio.actualizar(2L, datosActualizacion(), 1L).nombre());
    }

    @Test
    void actualizarConRutDeOtroUsuarioLanzaConflicto() {
        when(usuarioRepository.findByRut("12345678-5")).thenReturn(Optional.of(supervisor));

        assertThrows(ConflictoException.class, () -> servicio.actualizar(2L, datosActualizacion(), 2L));
    }

    @Test
    void actualizarOtraCuentaSinSerSupervisorLanzaAccesoDenegado() {
        assertThrows(AccesoDenegadoException.class, () -> servicio.actualizar(1L, datosActualizacion(), 2L));
    }

    @Test
    void eliminarBorraAlUsuarioYSuFoto() {
        observador.setAvatar("foto.jpg");

        servicio.eliminar(2L, 1L);

        verify(usuarioRepository).delete(observador);
        verify(almacenamientoService).eliminar("foto.jpg");
    }

    @Test
    void eliminarLaPropiaCuentaOUnaInexistenteLanzaError() {
        assertThrows(ReglaNegocioException.class, () -> servicio.eliminar(1L, 1L));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.eliminar(99L, 1L));
        assertThrows(AccesoDenegadoException.class, () -> servicio.eliminar(1L, 2L));
    }

    @Test
    void cambiarActivoDesactivaYActivaAOtrosUsuarios() {
        assertFalse(servicio.cambiarActivo(2L, false, 1L).activo());
        assertTrue(servicio.cambiarActivo(2L, true, 1L).activo());
    }

    @Test
    void cambiarActivoNoPermiteDesactivarLaPropiaCuenta() {
        assertThrows(ReglaNegocioException.class, () -> servicio.cambiarActivo(1L, false, 1L));
        assertTrue(servicio.cambiarActivo(1L, true, 1L).activo());
    }

    @Test
    void cambiarActivoSinSerSupervisorLanzaAccesoDenegado() {
        assertThrows(AccesoDenegadoException.class, () -> servicio.cambiarActivo(1L, false, 2L));
    }

    @Test
    void cambiarPerfilActualizaElPerfilDeOtroUsuario() {
        UsuarioResponse respuesta = servicio.cambiarPerfil(2L, 1L, 1L);

        assertEquals(1L, respuesta.perfilId());
        assertEquals("Supervisor", respuesta.perfilNombre());
    }

    @Test
    void cambiarPerfilNoPermiteCambiarElPropioRol() {
        assertThrows(ReglaNegocioException.class, () -> servicio.cambiarPerfil(1L, 2L, 1L));
    }

    @Test
    void iniciarSesionConDatosCorrectosDevuelveElUsuario() {
        when(usuarioRepository.findByCorreoIgnoreCase("LUIS@correo.cl")).thenReturn(Optional.of(observador));

        UsuarioResponse respuesta = servicio.iniciarSesion(new LoginRequest(" LUIS@correo.cl ", "Clave123!"));

        assertEquals(2L, respuesta.id());
    }

    @Test
    void iniciarSesionConDatosInvalidosLanzaNoAutorizado() {
        when(usuarioRepository.findByCorreoIgnoreCase("luis@correo.cl")).thenReturn(Optional.of(observador));

        assertThrows(NoAutorizadoException.class, () -> servicio.iniciarSesion(new LoginRequest("luis@correo.cl", "mala")));
        assertThrows(NoAutorizadoException.class, () -> servicio.iniciarSesion(new LoginRequest("nadie@correo.cl", "Clave123!")));
        assertThrows(NoAutorizadoException.class, () -> servicio.iniciarSesion(new LoginRequest(null, "Clave123!")));
        assertThrows(NoAutorizadoException.class, () -> servicio.iniciarSesion(new LoginRequest("luis@correo.cl", null)));
    }

    @Test
    void iniciarSesionConCuentaDesactivadaLanzaNoAutorizado() {
        observador.setActivo(false);
        when(usuarioRepository.findByCorreoIgnoreCase("luis@correo.cl")).thenReturn(Optional.of(observador));

        NoAutorizadoException error = assertThrows(NoAutorizadoException.class,
                () -> servicio.iniciarSesion(new LoginRequest("luis@correo.cl", "Clave123!")));

        assertEquals("Tu cuenta está desactivada. Habla con un supervisor", error.getMessage());
    }

    @Test
    void recuperarCambiaLaContrasenaCuandoCorreoYRutCoinciden() {
        when(usuarioRepository.findByCorreoIgnoreCase("luis@correo.cl")).thenReturn(Optional.of(observador));

        servicio.recuperar(new RecuperarRequest("luis@correo.cl", "12.345.678-5", "Nueva123!"));

        assertEquals("NUEVO_HASH", observador.getContrasena());
        verify(usuarioRepository).save(observador);
    }

    @Test
    void recuperarConDatosQueNoCoincidenLanzaReglaDeNegocio() {
        when(usuarioRepository.findByCorreoIgnoreCase("luis@correo.cl")).thenReturn(Optional.of(observador));

        assertThrows(ReglaNegocioException.class,
                () -> servicio.recuperar(new RecuperarRequest("luis@correo.cl", "10000013-K", "Nueva123!")));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.recuperar(new RecuperarRequest("nadie@correo.cl", "12345678-5", "Nueva123!")));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.recuperar(new RecuperarRequest(null, "12345678-5", "Nueva123!")));
    }

    @Test
    void recuperarConCuentaDesactivadaOContrasenaDebilLanzaError() {
        when(usuarioRepository.findByCorreoIgnoreCase("luis@correo.cl")).thenReturn(Optional.of(observador));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.recuperar(new RecuperarRequest("luis@correo.cl", "12345678-5", "debil")));

        observador.setActivo(false);
        assertThrows(NoAutorizadoException.class,
                () -> servicio.recuperar(new RecuperarRequest("luis@correo.cl", "12345678-5", "Nueva123!")));
    }

    @Test
    void cambiarContrasenaConLaActualCorrectaGuardaLaNueva() {
        servicio.cambiarContrasena(2L, new CambiarContrasenaRequest("Clave123!", "Nueva123!"));

        assertEquals("NUEVO_HASH", observador.getContrasena());
        verify(usuarioRepository).save(observador);
    }

    @Test
    void cambiarContrasenaConActualIncorrectaOContrasenaNuevaDebilLanzaError() {
        assertThrows(NoAutorizadoException.class,
                () -> servicio.cambiarContrasena(2L, new CambiarContrasenaRequest("otra", "Nueva123!")));
        assertThrows(NoAutorizadoException.class,
                () -> servicio.cambiarContrasena(2L, new CambiarContrasenaRequest(null, "Nueva123!")));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.cambiarContrasena(2L, new CambiarContrasenaRequest("Clave123!", "debil")));
    }

    @Test
    void subirAvatarGuardaLaFotoNuevaYBorraLaAnterior() {
        observador.setAvatar("viejo.jpg");
        MockMultipartFile foto = new MockMultipartFile("archivo", "foto.png", "image/png", new byte[]{1, 2, 3});
        when(almacenamientoService.guardar(foto)).thenReturn("nuevo.jpg");

        UsuarioResponse respuesta = servicio.subirAvatar(2L, foto, 2L);

        assertTrue(respuesta.avatarUrl().contains("/api/usuarios/2/avatar?v=nuevo.jpg"));
        verify(almacenamientoService).eliminar("viejo.jpg");
    }

    @Test
    void obtenerAvatarDevuelveLosBytesOLanzaErrorSiNoHayFoto() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtenerAvatar(2L));

        observador.setAvatar("foto.jpg");
        when(almacenamientoService.leer("foto.jpg")).thenReturn(new byte[]{9});

        assertEquals(1, servicio.obtenerAvatar(2L).length);
    }

    @Test
    void sinIdentificarAlActorLanzaNoAutorizado() {
        assertThrows(NoAutorizadoException.class, () -> servicio.cambiarActivo(2L, true, null));
        assertThrows(NoAutorizadoException.class, () -> servicio.cambiarActivo(2L, true, 99L));
    }

    @Test
    void unSupervisorDesactivadoOSinPerfilDisponibleNoTienePermisos() {
        supervisor.setActivo(false);
        assertThrows(AccesoDenegadoException.class, () -> servicio.cambiarActivo(2L, true, 1L));

        supervisor.setActivo(true);
        when(perfilClient.obtener(1L)).thenThrow(feign());
        assertThrows(AccesoDenegadoException.class, () -> servicio.cambiarActivo(2L, true, 1L));
    }
}
