package cl.rinconsur.sesiones.service;

import cl.rinconsur.sesiones.client.RegistroClient;
import cl.rinconsur.sesiones.client.UsuarioClient;
import cl.rinconsur.sesiones.dto.RechazoRequest;
import cl.rinconsur.sesiones.dto.SesionRequest;
import cl.rinconsur.sesiones.dto.SesionResponse;
import cl.rinconsur.sesiones.dto.UsuarioDto;
import cl.rinconsur.sesiones.model.Acceso;
import cl.rinconsur.sesiones.model.EstadoSesion;
import cl.rinconsur.sesiones.model.Sesion;
import cl.rinconsur.sesiones.model.Turno;
import cl.rinconsur.sesiones.exception.ConflictoException;
import cl.rinconsur.sesiones.exception.RecursoNoEncontradoException;
import cl.rinconsur.sesiones.exception.ReglaNegocioException;
import cl.rinconsur.sesiones.repository.AccesoRepository;
import cl.rinconsur.sesiones.repository.EstadoSesionRepository;
import cl.rinconsur.sesiones.repository.SesionRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SesionService {

    static final LocalTime APERTURA = LocalTime.of(10, 0);
    static final LocalTime CIERRE = LocalTime.of(21, 0);
    static final long MINIMO_REGISTROS_VALIDOS = 30;

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private final SesionRepository sesiones;
    private final AccesoRepository accesos;
    private final EstadoSesionRepository estados;
    private final UsuarioClient usuarioClient;
    private final RegistroClient registroClient;
    private final ZoneId zona;

    public SesionService(SesionRepository sesiones, AccesoRepository accesos, EstadoSesionRepository estados,
                         UsuarioClient usuarioClient, RegistroClient registroClient,
                         @Value("${app.zona:America/Santiago}") String zona) {
        this.sesiones = sesiones;
        this.accesos = accesos;
        this.estados = estados;
        this.usuarioClient = usuarioClient;
        this.registroClient = registroClient;
        this.zona = ZoneId.of(zona);
    }

    public List<SesionResponse> listar(String fecha, String desde, String hasta, String turno, String estado,
                                       Long observadorId, Long accesoId) {
        Turno t = parseTurno(turno);
        LocalDate f = fecha == null || fecha.isBlank() ? null : parseFecha(fecha);
        LocalDate d = desde == null || desde.isBlank() ? null : parseFecha(desde);
        LocalDate h = hasta == null || hasta.isBlank() ? null : parseFecha(hasta);
        Map<Long, String> nombres = new HashMap<>();
        return sesiones.findAll().stream()
                .filter(s -> f == null || s.getFecha().equals(f))
                .filter(s -> d == null || !s.getFecha().isBefore(d))
                .filter(s -> h == null || !s.getFecha().isAfter(h))
                .filter(s -> t == null || Turno.de(s.getHoraInicio()) == t)
                .filter(s -> estado == null || estado.isBlank() || s.getEstado().getCodigo().equalsIgnoreCase(estado))
                .filter(s -> observadorId == null || observadorId.equals(s.getObservadorId()))
                .filter(s -> accesoId == null || accesoId.equals(s.getAcceso().getId()))
                .sorted(Comparator.comparing(Sesion::getFecha).thenComparing(Sesion::getHoraInicio).reversed())
                .map(s -> SesionResponse.de(s, nombres.computeIfAbsent(s.getObservadorId(), this::nombreUsuario), null))
                .toList();
    }

    public SesionResponse obtener(Long id, boolean conConteo) {
        Sesion s = buscar(id);
        Long conteo = conConteo ? contarValidos(id) : null;
        return SesionResponse.de(s, nombreUsuario(s.getObservadorId()), conteo);
    }

    public SesionResponse programar(SesionRequest r) {
        LocalDate fecha = parseFecha(r.fecha());
        LocalTime inicio = parseHora(r.horaInicio());
        LocalTime fin = parseHora(r.horaFin());

        if (fecha.isBefore(LocalDate.now(zona))) {
            throw new ReglaNegocioException("La fecha no puede ser anterior a hoy");
        }
        if (inicio.isBefore(APERTURA)) throw new ReglaNegocioException("La tienda abre a las 10:00");
        if (fin.isAfter(CIERRE)) throw new ReglaNegocioException("La tienda cierra a las 21:00");
        long minutos = Duration.between(inicio, fin).toMinutes();
        if (minutos < 30 || minutos > 60) {
            throw new ReglaNegocioException("La sesion debe durar entre 30 y 60 minutos");
        }

        Acceso acceso = accesos.findById(r.accesoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el acceso " + r.accesoId()));

        UsuarioDto observador = traerUsuario(r.observadorId(), "El observador");
        if (!observador.activo()) throw new ReglaNegocioException("El observador esta desactivado");
        if (observador.perfilNombre() == null || !observador.perfilNombre().equalsIgnoreCase("Observador")) {
            throw new ReglaNegocioException("El usuario elegido no tiene perfil Observador");
        }

        boolean solapa = sesiones.findByAccesoIdAndFecha(acceso.getId(), fecha).stream()
                .filter(o -> !o.getEstado().getCodigo().equals("CANCELADA"))
                .anyMatch(o -> inicio.isBefore(o.getHoraFin()) && o.getHoraInicio().isBefore(fin));
        if (solapa) throw new ConflictoException("Ya hay una sesion en ese acceso y horario");

        Sesion s = new Sesion();
        s.setAcceso(acceso);
        s.setFecha(fecha);
        s.setHoraInicio(inicio);
        s.setHoraFin(fin);
        s.setObservadorId(observador.id());
        s.setEstado(estado("PROGRAMADA"));
        return SesionResponse.de(sesiones.save(s), observador.nombre(), null);
    }

    public SesionResponse iniciar(Long id) {
        Sesion s = buscar(id);
        exigirEstado(s, "PROGRAMADA", "Solo se puede iniciar una sesion programada");
        LocalDateTime ahora = LocalDateTime.now(zona);
        LocalTime hora = ahora.toLocalTime().withSecond(0).withNano(0);
        if (!ahora.toLocalDate().equals(s.getFecha())) {
            throw new ReglaNegocioException("Esta sesion es para el " + s.getFecha());
        }
        if (hora.isBefore(s.getHoraInicio())) {
            throw new ReglaNegocioException("Aun no es la hora: parte a las " + s.getHoraInicio());
        }
        if (!hora.isBefore(s.getHoraFin())) {
            throw new ReglaNegocioException("La sesion ya termino a las " + s.getHoraFin());
        }
        return guardarEstado(s, "EN_CURSO");
    }

    public SesionResponse finalizar(Long id) {
        Sesion s = buscar(id);
        exigirEstado(s, "EN_CURSO", "Solo se puede finalizar una sesion en curso");
        return guardarEstado(s, "FINALIZADA");
    }

    public SesionResponse cancelar(Long id) {
        Sesion s = buscar(id);
        exigirEstado(s, "PROGRAMADA", "Solo se puede cancelar una sesion programada");
        return guardarEstado(s, "CANCELADA");
    }

    public SesionResponse validar(Long id, Long idValidador) {
        Sesion s = buscar(id);
        exigirEstado(s, "FINALIZADA", "Solo se puede validar una sesion finalizada");
        verificarValidador(s, idValidador);
        long validos = contarValidos(id);
        s.setValidadorId(idValidador);
        if (validos < MINIMO_REGISTROS_VALIDOS) {
            s.setMotivoRechazo("Muestra insuficiente");
            return guardarEstadoConConteo(s, "RECHAZADA", validos);
        }
        s.setMotivoRechazo(null);
        return guardarEstadoConConteo(s, "VALIDADA", validos);
    }

    public SesionResponse rechazar(Long id, RechazoRequest r) {
        Sesion s = buscar(id);
        exigirEstado(s, "FINALIZADA", "Solo se puede rechazar una sesion finalizada");
        String motivo = r.motivo() == null ? "" : r.motivo().trim();
        if (motivo.isEmpty()) throw new ReglaNegocioException("Escribe el motivo del rechazo");
        if (motivo.length() < 5) throw new ReglaNegocioException("El motivo debe tener minimo 5 caracteres");
        verificarValidador(s, r.idValidador());
        s.setValidadorId(r.idValidador());
        s.setMotivoRechazo(motivo);
        return guardarEstado(s, "RECHAZADA");
    }

    private void verificarValidador(Sesion s, Long idValidador) {
        if (idValidador == null) throw new ReglaNegocioException("Falta el validador");
        if (idValidador.equals(s.getObservadorId())) {
            throw new ReglaNegocioException("El validador no puede ser quien observo la sesion");
        }
        UsuarioDto v = traerUsuario(idValidador, "El validador");
        if (!v.activo()) throw new ReglaNegocioException("El validador esta desactivado");
        if (v.perfilNombre() == null || !v.perfilNombre().equalsIgnoreCase("Supervisor")) {
            throw new ReglaNegocioException("Solo un supervisor puede validar o rechazar");
        }
    }

    private SesionResponse guardarEstado(Sesion s, String codigo) {
        return guardarEstadoConConteo(s, codigo, null);
    }

    private SesionResponse guardarEstadoConConteo(Sesion s, String codigo, Long conteo) {
        s.setEstado(estado(codigo));
        Sesion guardada = sesiones.save(s);
        return SesionResponse.de(guardada, nombreUsuario(guardada.getObservadorId()), conteo);
    }

    private void exigirEstado(Sesion s, String codigo, String mensaje) {
        if (!s.getEstado().getCodigo().equals(codigo)) throw new ReglaNegocioException(mensaje);
    }

    private EstadoSesion estado(String codigo) {
        return estados.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Falta el estado " + codigo + " en la tabla estados_sesion"));
    }

    private Sesion buscar(Long id) {
        return sesiones.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la sesion " + id));
    }

    private UsuarioDto traerUsuario(Long id, String quien) {
        try {
            return usuarioClient.obtener(id);
        } catch (FeignException.NotFound e) {
            throw new ReglaNegocioException(quien + " no existe");
        }
    }

    private String nombreUsuario(Long id) {
        try {
            return usuarioClient.obtener(id).nombre();
        } catch (FeignException e) {
            return null;
        }
    }

    private long contarValidos(Long idSesion) {
        Long n = registroClient.contarValidos(idSesion);
        return n == null ? 0 : n;
    }

    private LocalDate parseFecha(String texto) {
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("Fecha no valida, usa el formato AAAA-MM-DD");
        }
    }

    private Turno parseTurno(String texto) {
        if (texto == null || texto.isBlank()) return null;
        try {
            return Turno.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("Turno no valido, usa MANANA, TARDE o NOCHE");
        }
    }

    private LocalTime parseHora(String texto) {
        try {
            return LocalTime.parse(texto.trim(), FORMATO_HORA);
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("Hora no valida, usa el formato HH:MM (24 horas)");
        }
    }
}
