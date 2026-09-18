package com.bancopreguntas.service;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.Pregunta;

import java.util.List;
import java.util.Map;

/**
 * Reglas de negocio del banco de preguntas. Esta interfaz es el "contrato"
 * que consume la capa de presentacion (Controlador en el micro-patron MVC),
 * cumpliendo el principio de Inversion de Dependencias.
 */
public interface PreguntaService {

    /** HU01: crea una pregunta aplicando la validacion estructural antes de grabar. */
    Pregunta crearPregunta(Pregunta pregunta);

    /** HU02: cambia el estado de una pregunta validando que la transicion sea permitida. */
    Pregunta cambiarEstado(String preguntaId, EstadoPregunta nuevoEstado);

    /** HU03: lista, pagina y filtra las preguntas creadas por un autor. */
    ResultadoPaginado<Pregunta> listarPreguntasPorAutor(String autorId, FiltroPregunta filtro,
                                                         int pagina, int tamanoPagina);

    /** HU04: asigna uno o mas revisores a una pregunta en estado "Pendiente de revision". */
    Pregunta asignarRevisores(String preguntaId, List<String> revisoresIds);

    List<Pregunta> listarPendientesDeRevision();

    /** Cantidades agrupadas para el grafico de seguimiento del autor. */
    Map<String, Integer> contarEstadosVisiblesPorAutor(String autorId);
}
