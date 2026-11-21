package cl.rinconsur.usuarios.controller;

import cl.rinconsur.usuarios.dto.ActualizarUsuarioRequest;
import cl.rinconsur.usuarios.dto.CambiarContrasenaRequest;
import cl.rinconsur.usuarios.dto.LoginRequest;
import cl.rinconsur.usuarios.dto.RecuperarRequest;
import cl.rinconsur.usuarios.dto.RegistroRequest;
import cl.rinconsur.usuarios.dto.UsuarioResponse;
import cl.rinconsur.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) String buscar,
                                        @RequestParam(required = false) Long perfilId,
                                        @RequestParam(required = false) Boolean activo) {
        return usuarioService.listar(buscar, perfilId, activo);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest datos,
                                     @RequestHeader(value = "X-Usuario-Id", required = false) Long idActor) {
        return usuarioService.registrar(datos, idActor);
    }

    @PostMapping("/login")
    public UsuarioResponse iniciarSesion(@Valid @RequestBody LoginRequest datos) {
        return usuarioService.iniciarSesion(datos);
    }

    @PostMapping("/recuperar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recuperar(@Valid @RequestBody RecuperarRequest datos) {
        usuarioService.recuperar(datos);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ActualizarUsuarioRequest datos,
                                      @RequestHeader(value = "X-Usuario-Id", required = false) Long idActor) {
        return usuarioService.actualizar(id, datos, idActor);
    }

    @PutMapping("/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(@PathVariable Long id, @Valid @RequestBody CambiarContrasenaRequest datos) {
        usuarioService.cambiarContrasena(id, datos);
    }

    @PatchMapping("/{id}/activo")
    public UsuarioResponse cambiarActivo(@PathVariable Long id,
                                         @RequestParam boolean valor,
                                         @RequestHeader(value = "X-Usuario-Id", required = false) Long idActor) {
        return usuarioService.cambiarActivo(id, valor, idActor);
    }

    @PatchMapping("/{id}/perfil")
    public UsuarioResponse cambiarPerfil(@PathVariable Long id,
                                         @RequestParam Long perfilId,
                                         @RequestHeader(value = "X-Usuario-Id", required = false) Long idActor) {
        return usuarioService.cambiarPerfil(id, perfilId, idActor);
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UsuarioResponse subirAvatar(@PathVariable Long id,
                                       @RequestParam("archivo") MultipartFile archivo,
                                       @RequestHeader(value = "X-Usuario-Id", required = false) Long idActor) {
        return usuarioService.subirAvatar(id, archivo, idActor);
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> obtenerAvatar(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(usuarioService.obtenerAvatar(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id,
                         @RequestHeader(value = "X-Usuario-Id", required = false) Long idActor) {
        usuarioService.eliminar(id, idActor);
    }
}
