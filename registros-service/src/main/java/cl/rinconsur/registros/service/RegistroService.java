package cl.rinconsur.registros.service;

import cl.rinconsur.registros.client.SesionClient;
import cl.rinconsur.registros.dto.EstadisticasResponse;
import cl.rinconsur.registros.dto.RegistroRequest;
import cl.rinconsur.registros.dto.SesionDto;
import cl.rinconsur.registros.model.Clasificacion;
import cl.rinconsur.registros.model.Registro;
import cl.rinconsur.registros.exception.RecursoNoEncontradoException;
import cl.rinconsur.registros.exception.ReglaNegocioException;
import cl.rinconsur.registros.repository.RegistroRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RegistroService {

    static final long MINIMO_REGISTROS_VALIDOS = 30;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final RegistroRepository repositorio;
    private final SesionClient sesionClient;
    private final ZoneId zona;

    public RegistroService(RegistroRepository repositorio, SesionClient sesionClient,
                           @Value("${app.zona:America/Santiago}") String zona) {
        this.repositorio = repositorio;
        this.sesionClient = sesionClient;
        this.zona = ZoneId.of(zona);
    }

    public Registro registrar(RegistroRequest r, Long idActor) {
        exigirSesionEnCurso(r.sesionId(), idActor);

        Registro nuevo = new Registro();
        nuevo.setSesionId(r.sesionId());
        nuevo.setHora(horaDe(r.hora()));
        nuevo.setClasificacion(r.clasificacion());
        nuevo.setRangoEtario(r.rangoEtario());
        nuevo.setTipoCompania(r.tipoCompania());
        nuevo.setCompraOtraTienda(Boolean.TRUE.equals(r.compraOtraTienda()));
        nuevo.setSexo(r.sexo());
        nuevo.setAnulado(false);
        return repositorio.save(nuevo);
    }

    public Registro anular(Long id, Long idActor) {
        Registro registro = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el registro " + id));
        exigirSesionEnCurso(registro.getSesionId(), idActor);
        if (registro.isAnulado()) throw new ReglaNegocioException("Ese registro ya estaba anulado");
        registro.setAnulado(true);
        return repositorio.save(registro);
    }

    public Registro deshacerUltimo(Long sesionId, Long idActor) {
        exigirSesionEnCurso(sesionId, idActor);
        Registro ultimo = repositorio.findBySesionIdOrderByIdAsc(sesionId).stream()
                .filter(x -> !x.isAnulado())
                .reduce((primero, segundo) -> segundo)
                .orElseThrow(() -> new ReglaNegocioException("No hay registros para deshacer"));
        ultimo.setAnulado(true);
        return repositorio.save(ultimo);
    }

    public List<Registro> listarPorSesion(Long sesionId, Boolean soloValidos) {
        List<Registro> todos = repositorio.findBySesionIdOrderByIdAsc(sesionId);
        if (Boolean.TRUE.equals(soloValidos)) return todos.stream().filter(x -> !x.isAnulado()).toList();
        return todos;
    }

    public long contarValidos(Long sesionId) {
        return repositorio.countBySesionIdAndAnuladoFalse(sesionId);
    }

    public EstadisticasResponse estadisticas(Long sesionId) {
        List<Registro> todos = repositorio.findBySesionIdOrderByIdAsc(sesionId);
        List<Registro> validos = todos.stream().filter(x -> !x.isAnulado()).toList();
        long transito = validos.stream().filter(x -> x.getClasificacion() == Clasificacion.TRANSITO).count();
        long visita = validos.size() - transito;
        Double porcentaje = validos.isEmpty() ? null : transito * 100.0 / validos.size();
        return new EstadisticasResponse(sesionId, todos.size(), validos.size(), todos.size() - validos.size(),
                transito, visita, porcentaje, MINIMO_REGISTROS_VALIDOS, validos.size() >= MINIMO_REGISTROS_VALIDOS);
    }

    private void exigirSesionEnCurso(Long sesionId, Long idActor) {
        SesionDto sesion;
        try {
            sesion = sesionClient.obtener(sesionId);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("No existe la sesion " + sesionId);
        }
        if (!"EN_CURSO".equals(sesion.estado())) {
            throw new ReglaNegocioException("Solo se puede registrar o anular en una sesion en curso");
        }
        if (idActor != null && !idActor.equals(sesion.observadorId())) {
            throw new ReglaNegocioException("Solo el observador de la sesion puede registrar personas");
        }
    }

    private String horaDe(String texto) {
        if (texto == null || texto.isBlank()) return LocalTime.now(zona).format(HORA);
        try {
            return LocalTime.parse(texto.trim(), HORA).format(HORA);
        } catch (Exception e) {
            throw new ReglaNegocioException("Hora no valida, usa el formato HH:MM (24 horas)");
        }
    }
}
