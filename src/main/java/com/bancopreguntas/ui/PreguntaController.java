package com.bancopreguntas.ui;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.domain.Rol;
import com.bancopreguntas.domain.Usuario;
import com.bancopreguntas.repository.UsuarioRepository;
import com.bancopreguntas.service.FiltroPregunta;
import com.bancopreguntas.service.PreguntaService;
import com.bancopreguntas.service.ResultadoPaginado;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Controlador del micro-patron MVC: es el unico punto de la capa de
 * presentacion que conoce la capa de negocio ({@link PreguntaService}). Las
 * vistas (paneles Swing) solo interactuan con este controlador y nunca
 * directamente con los repositorios o el modelo de dominio persistido.
 */
public class PreguntaController {

    private final PreguntaService preguntaService;
    private final UsuarioRepository usuarioRepository;

    public PreguntaController(PreguntaService preguntaService, UsuarioRepository usuarioRepository) {
        this.preguntaService = Objects.requireNonNull(preguntaService);
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
    }

    // ---- HU01 ----
    public Pregunta crearPregunta(Pregunta pregunta) {
        return preguntaService.crearPregunta(pregunta);
    }

    // ---- HU02 ----
    public Pregunta enviarARevision(String preguntaId) {
        return preguntaService.cambiarEstado(preguntaId, EstadoPregunta.PENDIENTE_REVISION);
    }

    public Pregunta eliminarPregunta(String preguntaId) {
        return preguntaService.cambiarEstado(preguntaId, EstadoPregunta.ELIMINADA);
    }

    public Map<String, Integer> contarEstadosVisiblesPorAutor(String autorId) {
        return preguntaService.contarEstadosVisiblesPorAutor(autorId);
    }

    // ---- HU03 ----
    public ResultadoPaginado<Pregunta> listarMisPreguntas(String autorId, FiltroPregunta filtro,
                                                            int pagina, int tamanoPagina) {
        return preguntaService.listarPreguntasPorAutor(autorId, filtro, pagina, tamanoPagina);
    }

    // ---- HU04 ----
    public List<Pregunta> listarPendientesDeRevision() {
        return preguntaService.listarPendientesDeRevision();
    }

    public List<Usuario> listarRevisoresDisponibles() {
        return usuarioRepository.listarPorRol(Rol.REVISOR);
    }

    public Pregunta asignarRevisores(String preguntaId, List<String> revisoresIds) {
        return preguntaService.asignarRevisores(preguntaId, revisoresIds);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.listarTodos();
    }
}
