package cl.rinconsur.usuarios.service;

import cl.rinconsur.usuarios.exception.RecursoNoEncontradoException;
import cl.rinconsur.usuarios.exception.ReglaNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AlmacenamientoService {

    private final Path carpeta;

    public AlmacenamientoService(@Value("${app.avatares.carpeta}") String carpeta) {
        this.carpeta = Path.of(carpeta).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.carpeta);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear la carpeta de avatares: " + this.carpeta, e);
        }
    }

    public String guardar(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new ReglaNegocioException("No se recibió ninguna imagen");
        }
        String tipo = archivo.getContentType();
        if (tipo == null || !tipo.startsWith("image/")) {
            throw new ReglaNegocioException("El archivo debe ser una imagen");
        }

        String nombre = UUID.randomUUID() + ".jpg";
        try {
            Files.copy(archivo.getInputStream(), carpeta.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la imagen");
        }
        return nombre;
    }

    public byte[] leer(String nombre) {
        try {
            return Files.readAllBytes(carpeta.resolve(nombre));
        } catch (IOException e) {
            throw new RecursoNoEncontradoException("La imagen no existe en el servidor");
        }
    }

    public void eliminar(String nombre) {
        if (nombre == null) {
            return;
        }
        try {
            Files.deleteIfExists(carpeta.resolve(nombre));
        } catch (IOException ignorada) {

        }
    }
}
