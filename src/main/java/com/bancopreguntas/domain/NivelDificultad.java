package com.bancopreguntas.domain;

/**
 * Nivel de dificultad de una pregunta del banco Saber Pro.
 */
public enum NivelDificultad {
    BAJO("Bajo"),
    MEDIO("Medio"),
    ALTO("Alto");

    private final String etiqueta;

    NivelDificultad(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
