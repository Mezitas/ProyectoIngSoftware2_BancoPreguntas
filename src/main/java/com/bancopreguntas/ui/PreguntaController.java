package com.bancopreguntas.ui;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.domain.Rol;
import com.bancopreguntas.domain.Usuario;
import com.bancopreguntas.repository.UsuarioRepository;
import com.bancopreguntas.service.AutenticacionService;
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
    private final AutenticacionService autenticacionService;

    // ---- HU01 ----
    public Pregunta crearPregunta(Pregunta pregunta) {
        return preguntaService.crearPregunta(pregunta, usuarioActual());
    }

    public Pregunta actualizarPregunta(String preguntaId, Pregunta contenido) {
        return preguntaService.actualizarPregunta(preguntaId, contenido, usuarioActual());
    }

    // ---- HU02 ----
    public Pregunta enviarARevision(String preguntaId) {
        return preguntaService.cambiarEstado(preguntaId, EstadoPregunta.PENDIENTE_REVISION, usuarioActual());
    }

    public Pregunta eliminarPregunta(String preguntaId) {
        return preguntaService.cambiarEstado(preguntaId, EstadoPregunta.ELIMINADA, usuarioActual());
    }

    public Map<String, Integer> contarEstadosVisiblesPorAutor() {
        return preguntaService.contarEstadosVisiblesPorAutor(usuarioActual());
    }

    // ---- HU03 ----
    public ResultadoPaginado<Pregunta> listarMisPreguntas(FiltroPregunta filtro, int pagina, int tamanoPagina) {
        return preguntaService.listarPreguntasPorAutor(usuarioActual(), filtro, pagina, tamanoPagina);
    }

    // ---- HU04 ----
    public List<Pregunta> listarPendientesDeRevision() {
        return preguntaService.listarPendientesDeRevision(usuarioActual());
    }

    public List<Usuario> listarRevisoresDisponibles() {
        exigirRol(Rol.ADMINISTRADOR);
        return usuarioRepository.listarPorRol(Rol.REVISOR);
    }

    public Pregunta asignarRevisores(String preguntaId, List<String> revisoresIds) {
        return preguntaService.asignarRevisores(preguntaId, revisoresIds, usuarioActual());
    }

    public List<Pregunta> listarMisRevisiones() {
        return preguntaService.listarAsignadasARevisor(usuarioActual());
    }

    public Pregunta resolverRevision(String preguntaId, boolean aprobada) {
        return preguntaService.resolverRevision(preguntaId, aprobada, usuarioActual());
    }

    public Usuario autenticar(String email, String password) {
        return autenticacionService.autenticar(email, password).orElse(null);
    }

    public PreguntaController(PreguntaService preguntaService, UsuarioRepository usuarioRepository,
                              AutenticacionService autenticacionService) {
        this.preguntaService = Objects.requireNonNull(preguntaService);
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
        this.autenticacionService = Objects.requireNonNull(autenticacionService);
    }

    private Usuario usuarioActual() {
        Usuario usuario = SesionActual.getUsuarioActual();
        if (usuario == null) {
            throw new SecurityException("Debe iniciar sesión para continuar.");
        }
        return usuario;
    }

    private void exigirRol(Rol rol) {
        if (usuarioActual().getRol() != rol) {
            throw new SecurityException("Esta operación requiere el rol " + rol.getEtiqueta() + ".");
        }
    }
}
