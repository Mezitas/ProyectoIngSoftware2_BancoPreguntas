package com.bancopreguntas.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PreguntaTest {

    private Pregunta preguntaValida() {
        return Pregunta.builder()
                .contexto("Contexto de prueba")
                .preguntaDirecta("¿Pregunta de prueba?")
                .distractores(List.of("A", "B", "C", "D"))
                .respuestaCorrecta("E")
                .justificacion("Justificación de prueba")
                .bibliografia("Bibliografía de prueba")
                .competencia("Competencia de prueba")
                .tema("Tema de prueba")
                .subtema("Subtema de prueba")
                .nivelDificultad(NivelDificultad.MEDIO)
                .autorId("autor-1")
                .build();
    }

    @Test
    void elBuilderCreaUnaPreguntaEnEstadoBorradorPorDefecto() {
        Pregunta pregunta = preguntaValida();

        assertEquals(EstadoPregunta.BORRADOR, pregunta.getEstado());
        assertNotNull(pregunta.getId());
        assertNotNull(pregunta.getFechaCreacion());
        assertTrue(pregunta.getRevisoresAsignados().isEmpty());
    }

    @Test
    void elBuilderCopiaLaListaDeDistractoresParaEvitarMutacionesExternas() {
        List<String> distractoresOriginales = new java.util.ArrayList<>(List.of("A", "B", "C", "D"));
        Pregunta pregunta = Pregunta.builder()
                .distractores(distractoresOriginales)
                .autorId("autor-1")
                .build();

        distractoresOriginales.add("E");

        assertEquals(4, pregunta.getDistractores().size());
    }

    @Test
    void laListaDeDistractoresExpuestaEsInmutable() {
        Pregunta pregunta = preguntaValida();
        assertThrows(UnsupportedOperationException.class,
                () -> pregunta.getDistractores().add("otro"));
    }

    @Test
    void cambiarEstadoActualizaElEstadoYLaFechaDeActualizacion() throws InterruptedException {
        Pregunta pregunta = preguntaValida();
        var fechaInicial = pregunta.getFechaActualizacion();

        Thread.sleep(5);
        pregunta.cambiarEstado(EstadoPregunta.PENDIENTE_REVISION);

        assertEquals(EstadoPregunta.PENDIENTE_REVISION, pregunta.getEstado());
        assertTrue(pregunta.getFechaActualizacion().isAfter(fechaInicial)
                || pregunta.getFechaActualizacion().isEqual(fechaInicial));
    }

    @Test
    void asignarRevisoresGuardaLosIdsIndicados() {
        Pregunta pregunta = preguntaValida();
        pregunta.asignarRevisores(List.of("rev-1", "rev-2"));

        assertEquals(2, pregunta.getRevisoresAsignados().size());
        assertTrue(pregunta.getRevisoresAsignados().containsAll(List.of("rev-1", "rev-2")));
    }
}
