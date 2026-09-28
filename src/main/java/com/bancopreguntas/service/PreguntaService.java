package com.bancopreguntas.service;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.domain.Usuario;

import java.util.List;
import java.util.Map;

/**
 * Reglas de negocio del banco de preguntas. Esta interfaz es el "contrato"
 * que consume la capa de presentacion (Controlador en el micro-patron MVC),
 * cumpliendo el principio de Inversion de Dependencias.
 */
public interface PreguntaService {

    /** HU01: crea una pregunta aplicando la validacion estructural antes de grabar. */
    Pregunta crearPregunta(Pregunta pregunta, Usuario actor);

    /** HU02: cambia el estado de una pregunta validando que la transicion sea permitida. */
    Pregunta cambiarEstado(String preguntaId, EstadoPregunta nuevoEstado, Usuario actor);

    Pregunta actualizarPregunta(String preguntaId, Pregunta contenido, Usuario actor);

    /** HU03: lista, pagina y filtra las preguntas creadas por un autor. */
    ResultadoPaginado<Pregunta> listarPreguntasPorAutor(Usuario actor, FiltroPregunta filtro,
                                                         int pagina, int tamanoPagina);

    /** HU04: asigna uno o mas revisores a una pregunta en estado "Pendiente de revision". */
    Pregunta asignarRevisores(String preguntaId, List<String> revisoresIds, Usuario actor);

    List<Pregunta> listarPendientesDeRevision(Usuario actor);

    /** @deprecated Use the overload that receives the authenticated administrator. */
    @Deprecated
    default List<Pregunta> listarPendientesDeRevision() {
        throw new SecurityException("Debe proporcionar el administrador autenticado para listar pendientes.");
    }

    List<Pregunta> listarAsignadasARevisor(Usuario actor);

    Pregunta resolverRevision(String preguntaId, boolean aprobada, Usuario actor);

    /** Cantidades agrupadas para el grafico de seguimiento del autor. */
    Map<String, Integer> contarEstadosVisiblesPorAutor(Usuario actor);

    /** Compatibilidad para clientes que identifican al autor por su id. */
    Map<String, Integer> contarEstadosVisiblesPorAutor(String autorId);
}
