package com.bancopreguntas.repository;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.Pregunta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementacion en memoria de {@link PreguntaRepository}, suficiente para
 * la demostracion funcional del primer corte. Implementa el patron GoF
 * "Singleton" para garantizar una unica fuente de verdad de los datos
 * durante la ejecucion de la aplicacion de escritorio.
 *
 * Nota de diseno: la aplicacion tambien puede instanciar este repositorio
 * directamente (constructor publico) para facilitar las pruebas unitarias
 * aisladas, mientras que la interfaz de usuario utiliza {@link #getInstance()}.
 */
public class PreguntaRepositoryEnMemoria implements PreguntaRepository {

    private static PreguntaRepositoryEnMemoria instancia;

    private final Map<String, Pregunta> preguntas = new LinkedHashMap<>();

    public PreguntaRepositoryEnMemoria() {
    }

    public static synchronized PreguntaRepositoryEnMemoria getInstance() {
        if (instancia == null) {
            instancia = new PreguntaRepositoryEnMemoria();
        }
        return instancia;
    }

    @Override
    public synchronized Pregunta guardar(Pregunta pregunta) {
        preguntas.put(pregunta.getId(), pregunta);
        return pregunta;
    }

    @Override
    public synchronized Optional<Pregunta> buscarPorId(String id) {
        return Optional.ofNullable(preguntas.get(id));
    }

    @Override
    public synchronized List<Pregunta> listarTodas() {
        return new ArrayList<>(preguntas.values());
    }

    @Override
    public synchronized List<Pregunta> listarPorAutor(String autorId) {
        return preguntas.values().stream()
                .filter(p -> p.getAutorId().equals(autorId))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<Pregunta> listarPorEstado(EstadoPregunta estado) {
        return preguntas.values().stream()
                .filter(p -> p.getEstado() == estado)
                .collect(Collectors.toList());
    }
}
