package com.bancopreguntas.validacion;

import com.bancopreguntas.domain.NivelDificultad;
import com.bancopreguntas.domain.Pregunta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorEstructuralPreguntaTest {

    private final ValidadorPregunta validador = new ValidadorEstructuralPregunta();

    private Pregunta.Builder preguntaValidaBuilder() {
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
                .nivelDificultad(NivelDificultad.ALTO)
                .autorId("autor-1");
    }

    @Test
    void unaPreguntaCompletaYCorrectaEsValida() {
        ResultadoValidacion resultado = validador.validar(preguntaValidaBuilder().build());

        assertTrue(resultado.esValido());
        assertTrue(resultado.getErrores().isEmpty());
    }

    @Test
    void faltarContextoGeneraError() {
        Pregunta pregunta = preguntaValidaBuilder().contexto("").build();

        ResultadoValidacion resultado = validador.validar(pregunta);

        assertFalse(resultado.esValido());
        assertTrue(resultado.getErrores().stream().anyMatch(e -> e.toLowerCase().contains("contexto")));
    }

    @Test
    void debeTenerExactamenteCuatroDistractores() {
        Pregunta pregunta = preguntaValidaBuilder().distractores(List.of("A", "B", "C")).build();

        ResultadoValidacion resultado = validador.validar(pregunta);

        assertFalse(resultado.esValido());
        assertTrue(resultado.getErrores().stream().anyMatch(e -> e.contains("4 distractores")));
    }

    @Test
    void losDistractoresNoPuedenRepetirse() {
        Pregunta pregunta = preguntaValidaBuilder()
                .distractores(List.of("A", "A", "C", "D"))
                .build();

        ResultadoValidacion resultado = validador.validar(pregunta);

        assertFalse(resultado.esValido());
        assertTrue(resultado.getErrores().stream().anyMatch(e -> e.contains("distintos")));
    }

    @Test
    void laRespuestaCorrectaNoPuedeCoincidirConUnDistractor() {
        Pregunta pregunta = preguntaValidaBuilder()
                .distractores(List.of("A", "B", "C", "D"))
                .respuestaCorrecta("A")
                .build();

        ResultadoValidacion resultado = validador.validar(pregunta);

        assertFalse(resultado.esValido());
        assertTrue(resultado.getErrores().stream().anyMatch(e -> e.toLowerCase().contains("respuesta correcta")));
    }

    @Test
    void faltarNivelDeDificultadGeneraError() {
        Pregunta pregunta = preguntaValidaBuilder().nivelDificultad(null).build();

        ResultadoValidacion resultado = validador.validar(pregunta);

        assertFalse(resultado.esValido());
        assertTrue(resultado.getErrores().stream().anyMatch(e -> e.toLowerCase().contains("nivel de dificultad")));
    }

    @Test
    void acumulaVariosErroresALaVez() {
        Pregunta pregunta = Pregunta.builder()
                .distractores(List.of("A", "B"))
                .autorId("autor-1")
                .build();

        ResultadoValidacion resultado = validador.validar(pregunta);

        assertFalse(resultado.esValido());
        assertTrue(resultado.getErrores().size() > 1);
    }
}
