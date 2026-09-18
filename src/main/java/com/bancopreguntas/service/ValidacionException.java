package com.bancopreguntas.service;

import java.util.Collections;
import java.util.List;

/**
 * Excepcion lanzada cuando una pregunta no cumple la validacion estructural
 * al intentar grabarla (HU01/HU03).
 */
public class ValidacionException extends RuntimeException {

    private final List<String> errores;

    public ValidacionException(List<String> errores) {
        super("La pregunta no cumple la validacion estructural: " + String.join(" | ", errores));
        this.errores = Collections.unmodifiableList(errores);
    }

    public List<String> getErrores() {
        return errores;
    }
}
