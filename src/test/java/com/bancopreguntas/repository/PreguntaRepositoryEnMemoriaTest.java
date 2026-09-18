package com.bancopreguntas.repository;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.NivelDificultad;
import com.bancopreguntas.domain.Pregunta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PreguntaRepositoryEnMemoriaTest {

    private PreguntaRepository repositorio;

    @BeforeEach
    void inicializar() {
        // Se usa una instancia nueva (no el Singleton) para que cada prueba
        // sea independiente y no comparta estado con otras pruebas.
        repositorio = new PreguntaRepositoryEnMemoria();
    }

    private Pregunta preguntaDe(String autorId, EstadoPregunta estado) {
        return Pregunta.builder()
                .contexto("Contexto")
                .preguntaDirecta("¿Pregunta?")
                .distractores(List.of("A", "B", "C", "D"))
                .respuestaCorrecta("E")
                .justificacion("Justificación")
                .bibliografia("Bibliografía")
                .competencia("Competencia")
                .tema("Tema")
                .subtema("Subtema")
                .nivelDificultad(NivelDificultad.BAJO)
                .autorId(autorId)
                .estado(estado)
                .build();
    }

    @Test
    void guardarYBuscarPorIdDevuelveLaMismaPregunta() {
        Pregunta pregunta = preguntaDe("autor-1", EstadoPregunta.BORRADOR);

        repositorio.guardar(pregunta);
        Optional<Pregunta> encontrada = repositorio.buscarPorId(pregunta.getId());

        assertTrue(encontrada.isPresent());
        assertEquals(pregunta.getId(), encontrada.get().getId());
    }

    @Test
    void buscarPorIdInexistenteDevuelveOptionalVacio() {
        assertTrue(repositorio.buscarPorId("no-existe").isEmpty());
    }

    @Test
    void listarPorAutorSoloDevuelveLasPreguntasDeEseAutor() {
        repositorio.guardar(preguntaDe("autor-1", EstadoPregunta.BORRADOR));
        repositorio.guardar(preguntaDe("autor-1", EstadoPregunta.BORRADOR));
        repositorio.guardar(preguntaDe("autor-2", EstadoPregunta.BORRADOR));

        List<Pregunta> deAutor1 = repositorio.listarPorAutor("autor-1");

        assertEquals(2, deAutor1.size());
        assertTrue(deAutor1.stream().allMatch(p -> p.getAutorId().equals("autor-1")));
    }

    @Test
    void listarPorEstadoFiltraCorrectamente() {
        repositorio.guardar(preguntaDe("autor-1", EstadoPregunta.BORRADOR));
        repositorio.guardar(preguntaDe("autor-1", EstadoPregunta.PENDIENTE_REVISION));

        List<Pregunta> pendientes = repositorio.listarPorEstado(EstadoPregunta.PENDIENTE_REVISION);

        assertEquals(1, pendientes.size());
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, pendientes.get(0).getEstado());
    }
}
