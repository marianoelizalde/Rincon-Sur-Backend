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
import cl.rinconsur.usuarios.util.Validaciones;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilClient perfilClient;
    private final AlmacenamientoService almacenamientoService;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> listar(String buscar, Long perfilId, Boolean activo) {
        Map<Long, String> perfiles = mapaDePerfiles();
        String q = buscar == null ? "" : buscar.trim().toLowerCase();
        return usuarioRepository.findAll().stream()
                .filter(u -> perfilId == null || perfilId.equals(u.getPerfilId()))
                .filter(u -> activo == null || activo == u.isActivo())
                .filter(u -> q.isEmpty()
                        || u.getNombre().toLowerCase().contains(q)
                        || u.getCorreo().toLowerCase().contains(q)
                        || u.getRut().toLowerCase().contains(q)
                        || (u.getCargo() != null && u.getCargo().toLowerCase().contains(q)))
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(u -> aRespuesta(u, perfiles.getOrDefault(u.getPerfilId(), "")))
                .toList();
    }

    public UsuarioResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    public UsuarioResponse registrar(RegistroRequest datos, Long idActor) {
        exigirSupervisor(idActor);
        return registrar(datos);
    }

    public UsuarioResponse registrar(RegistroRequest datos) {
        String nombre = Validaciones.nombre(datos.nombre());
        String correo = Validaciones.correo(datos.correo());
        String rut = Validaciones.rut(datos.rut());
        String cargo = Validaciones.cargo(datos.cargo());
        String telefono = Validaciones.telefono(datos.telefono());
        String direccion = Validaciones.direccion(datos.direccion());
        String comuna = Validaciones.comuna(datos.comuna());
        Validaciones.contrasena(datos.contrasena());
        if (datos.perfilId() == null) {
            throw new ReglaNegocioException("Elige un perfil");
        }

        if (usuarioRepository.findByCorreoIgnoreCase(correo).isPresent()) {
            throw new ConflictoException("Ese correo ya tiene una cuenta");
        }
        if (usuarioRepository.findByRut(rut).isPresent()) {
            throw new ConflictoException("Ese RUT ya está registrado");
        }
        validarPerfil(datos.perfilId());

        Usuario nuevo = Usuario.builder()
                .nombre(nombre)
                .correo(correo)
                .contrasena(passwordEncoder.encode(datos.contrasena()))
                .rut(rut)
                .cargo(cargo)
                .telefono(telefono)
                .direccion(direccion)
                .comuna(comuna)
                .perfilId(datos.perfilId())
                .activo(true)
                .build();
        return aRespuesta(usuarioRepository.save(nuevo));
    }

    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest datos, Long idActor) {
        exigirSupervisorOSiMismo(id, idActor);
        Usuario usuario = buscar(id);
        String rut = Validaciones.rut(datos.rut());
        usuarioRepository.findByRut(rut).ifPresent(otro -> {
            if (!otro.getId().equals(id)) throw new ConflictoException("Ese RUT ya está registrado");
        });

        usuario.setNombre(Validaciones.nombre(datos.nombre()));
        usuario.setRut(rut);
        usuario.setCargo(Validaciones.cargo(datos.cargo()));
        usuario.setTelefono(Validaciones.telefono(datos.telefono()));
        usuario.setDireccion(Validaciones.direccion(datos.direccion()));
        usuario.setComuna(Validaciones.comuna(datos.comuna()));
        return aRespuesta(usuarioRepository.save(usuario));
    }

    public void eliminar(Long id, Long idActor) {
        exigirSupervisor(idActor);
        Usuario usuario = buscar(id);
        if (id.equals(idActor)) {
            throw new ReglaNegocioException("No puedes eliminar tu propia cuenta");
        }
        usuarioRepository.delete(usuario);
        almacenamientoService.eliminar(usuario.getAvatar());
    }

    public UsuarioResponse cambiarActivo(Long id, boolean activo, Long idActor) {
        exigirSupervisor(idActor);
        Usuario usuario = buscar(id);
        if (!activo && id.equals(idActor)) {
            throw new ReglaNegocioException("No puedes desactivar tu propia cuenta");
        }
        usuario.setActivo(activo);
        return aRespuesta(usuarioRepository.save(usuario));
    }

    public UsuarioResponse cambiarPerfil(Long id, Long perfilId, Long idActor) {
        exigirSupervisor(idActor);
        Usuario usuario = buscar(id);
        if (id.equals(idActor)) {
            throw new ReglaNegocioException("No puedes cambiar tu propio rol");
        }
        validarPerfil(perfilId);
        usuario.setPerfilId(perfilId);
        return aRespuesta(usuarioRepository.save(usuario));
    }

    public UsuarioResponse iniciarSesion(LoginRequest datos) {
        Usuario usuario = datos.correo() == null || datos.contrasena() == null ? null
                : usuarioRepository.findByCorreoIgnoreCase(datos.correo().trim()).orElse(null);
        if (usuario == null || !passwordEncoder.matches(datos.contrasena(), usuario.getContrasena())) {
            throw new NoAutorizadoException("Correo o contraseña incorrectos");
        }
        if (!usuario.isActivo()) {
            throw new NoAutorizadoException("Tu cuenta está desactivada. Habla con un supervisor");
        }
        return aRespuesta(usuario);
    }

    public void recuperar(RecuperarRequest datos) {
        String rut = Validaciones.rut(datos.rut());
        Usuario usuario = datos.correo() == null ? null
                : usuarioRepository.findByCorreoIgnoreCase(datos.correo().trim()).orElse(null);
        if (usuario == null || !usuario.getRut().equals(rut)) {
            throw new ReglaNegocioException("El correo y el RUT no coinciden con ninguna cuenta");
        }
        if (!usuario.isActivo()) {
            throw new NoAutorizadoException("Tu cuenta está desactivada. Habla con un supervisor");
        }
        Validaciones.contrasena(datos.nuevaContrasena());
        usuario.setContrasena(passwordEncoder.encode(datos.nuevaContrasena()));
        usuarioRepository.save(usuario);
    }

    public void cambiarContrasena(Long id, CambiarContrasenaRequest datos) {
        Usuario usuario = buscar(id);
        if (datos.actual() == null || !passwordEncoder.matches(datos.actual(), usuario.getContrasena())) {
            throw new NoAutorizadoException("La contraseña actual es incorrecta");
        }
        Validaciones.contrasena(datos.nueva());
        usuario.setContrasena(passwordEncoder.encode(datos.nueva()));
        usuarioRepository.save(usuario);
    }

    public UsuarioResponse subirAvatar(Long id, MultipartFile archivo, Long idActor) {
        exigirSupervisorOSiMismo(id, idActor);
        Usuario usuario = buscar(id);
        String anterior = usuario.getAvatar();

        usuario.setAvatar(almacenamientoService.guardar(archivo));
        Usuario guardado = usuarioRepository.save(usuario);

        almacenamientoService.eliminar(anterior);
        return aRespuesta(guardado);
    }

    public byte[] obtenerAvatar(Long id) {
        Usuario usuario = buscar(id);
        if (usuario.getAvatar() == null) {
            throw new RecursoNoEncontradoException("El usuario no tiene foto");
        }
        return almacenamientoService.leer(usuario.getAvatar());
    }

    private void exigirSupervisor(Long idActor) {
        if (!esSupervisorActivo(idActor)) {
            throw new AccesoDenegadoException("Solo un supervisor puede hacer esta acción");
        }
    }

    private void exigirSupervisorOSiMismo(Long id, Long idActor) {
        if (idActor != null && idActor.equals(id)) {
            return;
        }
        exigirSupervisor(idActor);
    }

    private boolean esSupervisorActivo(Long idActor) {
        if (idActor == null) {
            throw new NoAutorizadoException("Falta indicar quién hace la acción (encabezado X-Usuario-Id)");
        }
        Usuario actor = usuarioRepository.findById(idActor)
                .orElseThrow(() -> new NoAutorizadoException("El usuario que hace la acción no existe"));
        if (!actor.isActivo()) {
            return false;
        }
        try {
            return "Supervisor".equalsIgnoreCase(perfilClient.obtener(actor.getPerfilId()).nombre());
        } catch (FeignException e) {
            return false;
        }
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private void validarPerfil(Long perfilId) {
        try {
            PerfilDto perfil = perfilClient.obtener(perfilId);
            if (!perfil.activo()) throw new ReglaNegocioException("El perfil está desactivado");
        } catch (FeignException.NotFound e) {
            throw new ReglaNegocioException("El perfil indicado no existe");
        }
    }

    private Map<Long, String> mapaDePerfiles() {
        Map<Long, String> mapa = new HashMap<>();
        try {
            perfilClient.listar().forEach(p -> mapa.put(p.id(), p.nombre()));
        } catch (FeignException e) {

        }
        return mapa;
    }

    private UsuarioResponse aRespuesta(Usuario usuario) {
        String nombrePerfil = "";
        try {
            nombrePerfil = perfilClient.obtener(usuario.getPerfilId()).nombre();
        } catch (FeignException e) {

        }
        return aRespuesta(usuario, nombrePerfil);
    }

    private UsuarioResponse aRespuesta(Usuario usuario, String perfilNombre) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRut(),
                usuario.getCargo(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.getComuna(),
                usuario.isActivo(),
                usuario.getPerfilId(),
                perfilNombre,
                urlAvatar(usuario)
        );
    }

    private String urlAvatar(Usuario usuario) {
        if (usuario.getAvatar() == null) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/usuarios/" + usuario.getId() + "/avatar")
                .queryParam("v", usuario.getAvatar())
                .toUriString();
    }
}
