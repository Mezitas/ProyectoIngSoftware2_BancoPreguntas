package com.bancopreguntas.repository;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.Pregunta;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de la capa de datos para preguntas (principio de Inversion de
 * Dependencias: la capa de negocio depende de esta abstraccion y no de una
 * implementacion concreta de almacenamiento).
 */
public interface PreguntaRepository {

    Pregunta guardar(Pregunta pregunta);

    Optional<Pregunta> buscarPorId(String id);

    List<Pregunta> listarTodas();

    List<Pregunta> listarPorAutor(String autorId);

    List<Pregunta> listarPorEstado(EstadoPregunta estado);
}
