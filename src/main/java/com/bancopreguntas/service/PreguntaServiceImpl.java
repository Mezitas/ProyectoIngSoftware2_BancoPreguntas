package com.bancopreguntas.service;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.NivelDificultad;
import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.domain.Rol;
import com.bancopreguntas.domain.Usuario;
import com.bancopreguntas.notificacion.SujetoAsignacion;
import com.bancopreguntas.notificacion.SujetoPreguntas;
import com.bancopreguntas.repository.PreguntaRepository;
import com.bancopreguntas.repository.RepositorioAuditoria;
import com.bancopreguntas.repository.RepositorioAuditoriaNoOp;
import com.bancopreguntas.repository.UsuarioRepository;
import com.bancopreguntas.validacion.ResultadoValidacion;
import com.bancopreguntas.validacion.ValidadorPregunta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PreguntaServiceImpl implements PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ValidadorPregunta validador;
    private final SujetoAsignacion sujetoAsignacion;
    private final SujetoPreguntas sujetoPreguntas;
    private final RepositorioAuditoria auditoria;

    public PreguntaServiceImpl(PreguntaRepository preguntaRepository, UsuarioRepository usuarioRepository,
                              ValidadorPregunta validador, SujetoAsignacion sujetoAsignacion) {
        this(preguntaRepository, usuarioRepository, validador, sujetoAsignacion,
                new SujetoPreguntas(), new RepositorioAuditoriaNoOp());
    }

    public PreguntaServiceImpl(PreguntaRepository preguntaRepository, UsuarioRepository usuarioRepository,
                              ValidadorPregunta validador, SujetoAsignacion sujetoAsignacion,
                              SujetoPreguntas sujetoPreguntas) {
        this(preguntaRepository, usuarioRepository, validador, sujetoAsignacion,
                sujetoPreguntas, new RepositorioAuditoriaNoOp());
    }

    public PreguntaServiceImpl(PreguntaRepository preguntaRepository, UsuarioRepository usuarioRepository,
                              ValidadorPregunta validador, SujetoAsignacion sujetoAsignacion,
                              SujetoPreguntas sujetoPreguntas, RepositorioAuditoria auditoria) {
        this.preguntaRepository = Objects.requireNonNull(preguntaRepository);
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
        this.validador = Objects.requireNonNull(validador);
        this.sujetoAsignacion = Objects.requireNonNull(sujetoAsignacion);
        this.sujetoPreguntas = Objects.requireNonNull(sujetoPreguntas);
        this.auditoria = Objects.requireNonNull(auditoria);
    }

    @Override
    public Pregunta crearPregunta(Pregunta pregunta, Usuario actor) {
        exigirRol(actor, Rol.AUTOR);
        if (!actor.getId().equals(pregunta.getAutorId())) {
            throw new SecurityException("Un autor solo puede crear preguntas a su nombre.");
        }
        if (pregunta.getEstado() != EstadoPregunta.BORRADOR || !pregunta.getRevisoresAsignados().isEmpty()) {
            throw new SecurityException("Las preguntas nuevas deben iniciar en borrador y sin revisores.");
        }
        if (preguntaRepository.buscarPorId(pregunta.getId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una pregunta con el identificador " + pregunta.getId());
        }
        validar(pregunta);
        Pregunta guardada = preguntaRepository.guardar(pregunta);
        registrar(actor, guardada, "CREAR", "Pregunta creada en borrador");
        sujetoPreguntas.notificarActualizacion();
        return guardada;
    }

    @Override
    public Pregunta actualizarPregunta(String preguntaId, Pregunta contenido, Usuario actor) {
        exigirRol(actor, Rol.AUTOR);
        Pregunta original = obtenerOFallar(preguntaId);
        exigirPropietario(original, actor);
        if (original.getEstado() != EstadoPregunta.BORRADOR && original.getEstado() != EstadoPregunta.RECHAZADA) {
            throw new IllegalStateException("Solo se pueden editar preguntas en borrador o rechazadas.");
        }

        Pregunta candidato = Pregunta.builder()
                .id(original.getId())
                .contexto(contenido.getContexto())
                .preguntaDirecta(contenido.getPreguntaDirecta())
                .distractores(contenido.getDistractores())
                .respuestaCorrecta(contenido.getRespuestaCorrecta())
                .justificacion(contenido.getJustificacion())
                .bibliografia(contenido.getBibliografia())
                .competencia(contenido.getCompetencia())
                .tema(contenido.getTema())
                .subtema(contenido.getSubtema())
                .nivelDificultad(contenido.getNivelDificultad())
                .autorId(original.getAutorId())
                .build();
        validar(candidato);
        original.actualizarContenido(candidato.getContexto(), candidato.getPreguntaDirecta(),
                candidato.getDistractores(), candidato.getRespuestaCorrecta(), candidato.getJustificacion(),
                candidato.getBibliografia(), candidato.getCompetencia(), candidato.getTema(),
                candidato.getSubtema(), candidato.getNivelDificultad());
        if (original.getEstado() == EstadoPregunta.RECHAZADA) {
            original.asignarRevisores(List.of());
            original.cambiarEstado(EstadoPregunta.BORRADOR);
        }
        Pregunta guardada = preguntaRepository.guardar(original);
        registrar(actor, guardada, "EDITAR", "Contenido de la pregunta actualizado");
        sujetoPreguntas.notificarActualizacion();
        return guardada;
    }

    @Override
    public Pregunta cambiarEstado(String preguntaId, EstadoPregunta nuevoEstado, Usuario actor) {
        exigirRol(actor, Rol.AUTOR);
        Objects.requireNonNull(nuevoEstado, "El nuevo estado es obligatorio.");
        Pregunta pregunta = obtenerOFallar(preguntaId);
        exigirPropietario(pregunta, actor);
        if (!pregunta.getEstado().puedeTransicionarA(nuevoEstado)) {
            throw new IllegalStateException("No es posible cambiar la pregunta de estado '"
                    + pregunta.getEstado().getEtiqueta() + "' a '" + nuevoEstado.getEtiqueta() + "'.");
        }
        if (nuevoEstado != EstadoPregunta.PENDIENTE_REVISION && nuevoEstado != EstadoPregunta.ELIMINADA) {
            throw new SecurityException("El autor solo puede enviar a revisión o eliminar su pregunta.");
        }
        if (nuevoEstado == EstadoPregunta.ELIMINADA
                && pregunta.getEstado() != EstadoPregunta.BORRADOR
                && pregunta.getEstado() != EstadoPregunta.RECHAZADA) {
            throw new IllegalStateException("Solo se pueden eliminar preguntas en borrador o rechazadas.");
        }
        pregunta.cambiarEstado(nuevoEstado);
        Pregunta actualizada = preguntaRepository.guardar(pregunta);
        registrar(actor, actualizada, "CAMBIAR_ESTADO", nuevoEstado.name());
        sujetoPreguntas.notificarActualizacion();
        return actualizada;
    }

    @Override
    public ResultadoPaginado<Pregunta> listarPreguntasPorAutor(Usuario actor, FiltroPregunta filtroParam,
                                                                int pagina, int tamanoPagina) {
        exigirRol(actor, Rol.AUTOR);
        FiltroPregunta filtro = filtroParam == null ? FiltroPregunta.vacio() : filtroParam;
        List<Pregunta> filtradas = preguntaRepository.listarPorAutor(actor.getId()).stream()
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
        int desde = (int) Math.min((long) (paginaSegura - 1) * tamanoSeguro, totalElementos);
        int hasta = Math.min(desde + tamanoSeguro, totalElementos);
        return new ResultadoPaginado<>(new ArrayList<>(filtradas.subList(desde, hasta)),
                paginaSegura, tamanoSeguro, totalElementos);
    }

    @Override
    public Pregunta asignarRevisores(String preguntaId, List<String> revisoresIds, Usuario actor) {
        exigirRol(actor, Rol.ADMINISTRADOR);
        Pregunta pregunta = obtenerOFallar(preguntaId);
        if (pregunta.getEstado() != EstadoPregunta.PENDIENTE_REVISION) {
            throw new IllegalStateException("Solo se pueden asignar revisores a preguntas pendientes.");
        }
        if (revisoresIds == null || revisoresIds.isEmpty()) {
            throw new IllegalArgumentException("Debe asignar al menos un revisor.");
        }
        List<Usuario> revisores = revisoresIds.stream().distinct()
                .map(id -> usuarioRepository.buscarPorId(id)
                        .orElseThrow(() -> new IllegalArgumentException("Revisor no encontrado: " + id)))
                .toList();
        if (revisores.stream().anyMatch(revisor -> revisor.getRol() != Rol.REVISOR)) {
            throw new SecurityException("Solo se pueden asignar usuarios con rol de revisor.");
        }
        List<String> idsUnicos = revisores.stream().map(Usuario::getId).toList();
        pregunta.asignarRevisores(idsUnicos);
        pregunta.cambiarEstado(EstadoPregunta.EN_REVISION);
        preguntaRepository.guardar(pregunta);
        registrar(actor, pregunta, "ASIGNAR_REVISORES", String.join(",", idsUnicos));
        sujetoPreguntas.notificarActualizacion();
        sujetoAsignacion.notificarAsignacion(pregunta, revisores);
        return pregunta;
    }

    @Override
    public List<Pregunta> listarPendientesDeRevision(Usuario actor) {
        exigirRol(actor, Rol.ADMINISTRADOR);
        return preguntaRepository.listarPorEstado(EstadoPregunta.PENDIENTE_REVISION);
    }

    /**
     * Retained for source compatibility with callers compiled against the old
     * contract. Listing pending questions requires an authenticated administrator.
     */
    @Deprecated
    @Override
    public List<Pregunta> listarPendientesDeRevision() {
        throw new SecurityException("Debe proporcionar el administrador autenticado para listar pendientes.");
    }

    @Override
    public List<Pregunta> listarAsignadasARevisor(Usuario actor) {
        exigirRol(actor, Rol.REVISOR);
        return preguntaRepository.listarAsignadasARevisor(actor.getId());
    }

    @Override
    public Pregunta resolverRevision(String preguntaId, boolean aprobada, Usuario actor) {
        exigirRol(actor, Rol.REVISOR);
        Pregunta pregunta = obtenerOFallar(preguntaId);
        if (!pregunta.getRevisoresAsignados().contains(actor.getId())) {
            throw new SecurityException("No está asignado como revisor de esta pregunta.");
        }
        EstadoPregunta destino = aprobada ? EstadoPregunta.APROBADA : EstadoPregunta.RECHAZADA;
        if (pregunta.getEstado() != EstadoPregunta.EN_REVISION
                || !pregunta.getEstado().puedeTransicionarA(destino)) {
            throw new IllegalStateException("La pregunta ya no está disponible para revisión.");
        }
        pregunta.cambiarEstado(destino);
        Pregunta actualizada = preguntaRepository.guardar(pregunta);
        registrar(actor, actualizada, aprobada ? "APROBAR" : "RECHAZAR", "Decisión del revisor");
        sujetoPreguntas.notificarActualizacion();
        return actualizada;
    }

    @Override
    public Map<String, Integer> contarEstadosVisiblesPorAutor(Usuario actor) {
        exigirRol(actor, Rol.AUTOR);
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        cantidades.put("Borrador", 0);
        cantidades.put("En revisión", 0);
        cantidades.put("Aprobada", 0);
        cantidades.put("Rechazada", 0);
        cantidades.put("Eliminada", 0);
        for (Pregunta pregunta : preguntaRepository.listarPorAutor(actor.getId())) {
            if (pregunta.getEstado() == EstadoPregunta.BORRADOR) {
                cantidades.computeIfPresent("Borrador", (k, v) -> v + 1);
            } else if (pregunta.getEstado() == EstadoPregunta.PENDIENTE_REVISION
                    || pregunta.getEstado() == EstadoPregunta.EN_REVISION) {
                cantidades.computeIfPresent("En revisión", (k, v) -> v + 1);
            } else if (pregunta.getEstado() == EstadoPregunta.APROBADA) {
                cantidades.computeIfPresent("Aprobada", (k, v) -> v + 1);
            } else if (pregunta.getEstado() == EstadoPregunta.RECHAZADA) {
                cantidades.computeIfPresent("Rechazada", (k, v) -> v + 1);
            } else if (pregunta.getEstado() == EstadoPregunta.ELIMINADA) {
                cantidades.computeIfPresent("Eliminada", (k, v) -> v + 1);
            }
        }
        return cantidades;
    }

    @Override
    public Map<String, Integer> contarEstadosVisiblesPorAutor(String autorId) {
        Usuario autor = usuarioRepository.buscarPorId(autorId)
                .orElseThrow(() -> new IllegalArgumentException("Autor no encontrado: " + autorId));
        return contarEstadosVisiblesPorAutor(autor);
    }

    private void validar(Pregunta pregunta) {
        ResultadoValidacion resultado = validador.validar(pregunta);
        if (!resultado.esValido()) {
            throw new ValidacionException(resultado.getErrores());
        }
    }

    private void registrar(Usuario actor, Pregunta pregunta, String accion, String detalle) {
        auditoria.registrar(actor.getId(), pregunta.getId(), accion, detalle, LocalDateTime.now());
    }

    private void exigirRol(Usuario actor, Rol rol) {
        if (actor == null || actor.getRol() != rol
                || usuarioRepository.buscarPorId(actor.getId())
                .filter(registrado -> registrado.getRol() == rol)
                .isEmpty()) {
            throw new SecurityException("La operación requiere el rol " + rol.getEtiqueta() + ".");
        }
    }

    private void exigirPropietario(Pregunta pregunta, Usuario actor) {
        if (!pregunta.getAutorId().equals(actor.getId())) {
            throw new SecurityException("Solo el autor propietario puede modificar esta pregunta.");
        }
    }

    private Pregunta obtenerOFallar(String preguntaId) {
        return preguntaRepository.buscarPorId(preguntaId)
                .orElseThrow(() -> new IllegalArgumentException("Pregunta no encontrada: " + preguntaId));
    }

    private boolean coincideTexto(Pregunta pregunta, String texto) {
        if (texto == null || texto.isBlank()) return true;
        String normalizado = texto.toLowerCase();
        return pregunta.getPreguntaDirecta().toLowerCase().contains(normalizado)
                || pregunta.getContexto().toLowerCase().contains(normalizado);
    }

    private boolean coincideCampo(String valorCampo, String filtro) {
        return filtro == null || filtro.isBlank()
                || (valorCampo != null && valorCampo.toLowerCase().contains(filtro.toLowerCase()));
    }
}
