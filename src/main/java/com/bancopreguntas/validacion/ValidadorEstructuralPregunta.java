package com.bancopreguntas.validacion;

import com.bancopreguntas.domain.Pregunta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Estrategia concreta de validacion estructural aplicada al grabar una
 * pregunta (HU01, "Al momento de grabar el sistema debe aplicar la
 * validacion estructural - ver HU03"). Verifica que la pregunta cumpla con
 * los campos exigidos por el Diseno Centrado en Evidencia y con las reglas
 * de integridad de una pregunta de seleccion multiple con unica respuesta:
 * exactamente 4 distractores distintos y una respuesta correcta que no se
 * repita entre ellos.
 */
public class ValidadorEstructuralPregunta implements ValidadorPregunta {

    private static final int NUMERO_DISTRACTORES = 4;

    @Override
    public ResultadoValidacion validar(Pregunta pregunta) {
        List<String> errores = new ArrayList<>();

        requerido(pregunta.getContexto(), "El contexto es obligatorio.", errores);
        requerido(pregunta.getPreguntaDirecta(), "La pregunta directa es obligatoria.", errores);
        requerido(pregunta.getJustificacion(), "La justificacion de la respuesta es obligatoria.", errores);
        requerido(pregunta.getBibliografia(), "La bibliografia es obligatoria.", errores);
        requerido(pregunta.getCompetencia(), "La competencia es obligatoria.", errores);
        requerido(pregunta.getTema(), "El tema es obligatorio.", errores);
        requerido(pregunta.getSubtema(), "El subtema es obligatorio.", errores);

        if (pregunta.getNivelDificultad() == null) {
            errores.add("El nivel de dificultad es obligatorio.");
        }

        validarDistractoresYRespuesta(pregunta, errores);

        if (pregunta.getAutorId() == null || pregunta.getAutorId().isBlank()) {
            errores.add("La pregunta debe tener un autor asociado.");
        }

        return errores.isEmpty() ? ResultadoValidacion.ok() : ResultadoValidacion.error(errores);
    }

    private void validarDistractoresYRespuesta(Pregunta pregunta, List<String> errores) {
        List<String> distractores = pregunta.getDistractores();

        if (distractores == null || distractores.size() != NUMERO_DISTRACTORES) {
            errores.add("La pregunta debe tener exactamente " + NUMERO_DISTRACTORES + " distractores.");
            return;
        }

        Set<String> normalizados = new HashSet<>();
        for (String distractor : distractores) {
            if (distractor == null || distractor.isBlank()) {
                errores.add("Ningun distractor puede estar vacio.");
                continue;
            }
            normalizados.add(normalizar(distractor));
        }

        if (normalizados.size() != NUMERO_DISTRACTORES) {
            errores.add("Los " + NUMERO_DISTRACTORES + " distractores deben ser distintos entre si.");
        }

        String respuesta = pregunta.getRespuestaCorrecta();
        if (respuesta == null || respuesta.isBlank()) {
            errores.add("La respuesta correcta es obligatoria.");
        } else if (normalizados.contains(normalizar(respuesta))) {
            errores.add("La respuesta correcta no puede coincidir con ninguno de los distractores.");
        }
    }

    private void requerido(String valor, String mensaje, List<String> errores) {
        if (valor == null || valor.isBlank()) {
            errores.add(mensaje);
        }
    }

    private String normalizar(String texto) {
        return texto.trim().toLowerCase();
    }
}
