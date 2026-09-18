package com.bancopreguntas.service;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.domain.Usuario;
import com.bancopreguntas.notificacion.SujetoAsignacion;
import com.bancopreguntas.notificacion.SujetoPreguntas;
import com.bancopreguntas.repository.PreguntaRepository;
import com.bancopreguntas.repository.UsuarioRepository;
import com.bancopreguntas.validacion.ResultadoValidacion;
import com.bancopreguntas.validacion.ValidadorPregunta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementacion de {@link PreguntaService}. Todas sus dependencias se
 * reciben por constructor (inyeccion de dependencias manual), lo que permite
 * sustituirlas facilmente en pruebas unitarias y respeta el principio de
 * Inversion de Dependencias (SOLID).
 */
public class PreguntaServiceImpl implements PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ValidadorPregunta validador;
    private final SujetoAsignacion sujetoAsignacion;
    private final SujetoPreguntas sujetoPreguntas;

    public PreguntaServiceImpl(PreguntaRepository preguntaRepository,
                                UsuarioRepository usuarioRepository,
                                ValidadorPregunta validador,
                                SujetoAsignacion sujetoAsignacion) {
        this(preguntaRepository, usuarioRepository, validador, sujetoAsignacion, new SujetoPreguntas());
    }

    public PreguntaServiceImpl(PreguntaRepository preguntaRepository,
                               UsuarioRepository usuarioRepository,
                               ValidadorPregunta validador,
                               SujetoAsignacion sujetoAsignacion,
                               SujetoPreguntas sujetoPreguntas) {
        this.preguntaRepository = Objects.requireNonNull(preguntaRepository);
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
        this.validador = Objects.requireNonNull(validador);
        this.sujetoAsignacion = Objects.requireNonNull(sujetoAsignacion);
        this.sujetoPreguntas = Objects.requireNonNull(sujetoPreguntas);
    }

    @Override
    public Pregunta crearPregunta(Pregunta pregunta) {
        ResultadoValidacion resultado = validador.validar(pregunta);
        if (!resultado.esValido()) {
            throw new ValidacionException(resultado.getErrores());
        }
        Pregunta guardada = preguntaRepository.guardar(pregunta);
        sujetoPreguntas.notificarActualizacion();
        return guardada;
    }

    @Override
    public Pregunta cambiarEstado(String preguntaId, EstadoPregunta nuevoEstado) {
        Pregunta pregunta = obtenerOFallar(preguntaId);

        if (!pregunta.getEstado().puedeTransicionarA(nuevoEstado)) {
            throw new IllegalStateException("No es posible cambiar la pregunta de estado '"
                    + pregunta.getEstado().getEtiqueta() + "' a '" + nuevoEstado.getEtiqueta() + "'.");
        }

        pregunta.cambiarEstado(nuevoEstado);
        Pregunta actualizada = preguntaRepository.guardar(pregunta);
        sujetoPreguntas.notificarActualizacion();
        return actualizada;
    }

    @Override
    public ResultadoPaginado<Pregunta> listarPreguntasPorAutor(String autorId, FiltroPregunta filtroParam,
                                                                 int pagina, int tamanoPagina) {
        FiltroPregunta filtro = filtroParam == null ? FiltroPregunta.vacio() : filtroParam;

        List<Pregunta> filtradas = preguntaRepository.listarPorAutor(autorId).stream()
                .filter(p -> coincideTexto(p, filtro.getTexto()))
                .filter(p -> coincideCampo(p.getTema(), filtro.getTema()))
                .filter(p -> coincideCampo(p.getSubtema(), filtro.getSubtema()))
                .filter(p -> coincideCampo(p.getCompetencia(), filtro.getCompetencia()))
                .filter(p -> filtro.getNivelDificultad() == null || p.getNivelDificultad() == filtro.getNivelDificultad())
                .filter(p -> filtro.getEstado() == null || p.getEstado() == filtro.getEstado())
                .sorted(Comparator.comparing(Pregunta::getFechaCreacion).reversed())
                .collect(Collectors.toList());

        int totalElementos = filtradas.size();
        int paginaSegura = Math.max(1, pagina);
        int tamanoSeguro = Math.max(1, tamanoPagina);

        int desde = Math.min((paginaSegura - 1) * tamanoSeguro, totalElementos);
        int hasta = Math.min(desde + tamanoSeguro, totalElementos);

        List<Pregunta> pagina_ = new ArrayList<>(filtradas.subList(desde, hasta));

        return new ResultadoPaginado<>(pagina_, paginaSegura, tamanoSeguro, totalElementos);
    }

    @Override
    public Pregunta asignarRevisores(String preguntaId, List<String> revisoresIds) {
        Pregunta pregunta = obtenerOFallar(preguntaId);

        if (pregunta.getEstado() != EstadoPregunta.PENDIENTE_REVISION) {
            throw new IllegalStateException(
                    "Solo se pueden asignar revisores a preguntas en estado 'Pendiente de revision'.");
        }
        if (revisoresIds == null || revisoresIds.isEmpty()) {
            throw new IllegalArgumentException("Debe asignar al menos un revisor.");
        }

        List<Usuario> revisores = revisoresIds.stream()
                .map(id -> usuarioRepository.buscarPorId(id)
                        .orElseThrow(() -> new IllegalArgumentException("Revisor no encontrado: " + id)))
                .collect(Collectors.toList());

        pregunta.asignarRevisores(revisoresIds);
        pregunta.cambiarEstado(EstadoPregunta.EN_REVISION);
        preguntaRepository.guardar(pregunta);
        sujetoPreguntas.notificarActualizacion();

        // Patron Observer: se notifica a todos los interesados (p. ej. envio de email).
        sujetoAsignacion.notificarAsignacion(pregunta, revisores);

        return pregunta;
    }

    @Override
    public List<Pregunta> listarPendientesDeRevision() {
        return preguntaRepository.listarPorEstado(EstadoPregunta.PENDIENTE_REVISION);
    }

    @Override
    public Map<String, Integer> contarEstadosVisiblesPorAutor(String autorId) {
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        cantidades.put("Borrador", 0);
        cantidades.put("En revisión", 0);
        cantidades.put("Eliminada", 0);

        for (Pregunta pregunta : preguntaRepository.listarPorAutor(autorId)) {
            if (pregunta.getEstado() == EstadoPregunta.BORRADOR) {
                cantidades.computeIfPresent("Borrador", (k, v) -> v + 1);
            } else if (pregunta.getEstado() == EstadoPregunta.PENDIENTE_REVISION
                    || pregunta.getEstado() == EstadoPregunta.EN_REVISION) {
                cantidades.computeIfPresent("En revisión", (k, v) -> v + 1);
            } else if (pregunta.getEstado() == EstadoPregunta.ELIMINADA) {
                cantidades.computeIfPresent("Eliminada", (k, v) -> v + 1);
            }
        }
        return cantidades;
    }

    private Pregunta obtenerOFallar(String preguntaId) {
        return preguntaRepository.buscarPorId(preguntaId)
                .orElseThrow(() -> new IllegalArgumentException("Pregunta no encontrada: " + preguntaId));
    }

    private boolean coincideTexto(Pregunta pregunta, String texto) {
        if (texto == null || texto.isBlank()) return true;
        String t = texto.toLowerCase();
        return pregunta.getPreguntaDirecta().toLowerCase().contains(t)
                || pregunta.getContexto().toLowerCase().contains(t);
    }

    private boolean coincideCampo(String valorCampo, String filtro) {
        if (filtro == null || filtro.isBlank()) return true;
        return valorCampo != null && valorCampo.toLowerCase().contains(filtro.toLowerCase());
    }
}
