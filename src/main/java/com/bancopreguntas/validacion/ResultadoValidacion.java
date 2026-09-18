package com.bancopreguntas.validacion;

import java.util.Collections;
import java.util.List;

/**
 * Resultado inmutable de aplicar una estrategia de validacion sobre una
 * pregunta: indica si es valida y, de no serlo, la lista de errores.
 */
public final class ResultadoValidacion {

    private final boolean valido;
    private final List<String> errores;

    private ResultadoValidacion(boolean valido, List<String> errores) {
        this.valido = valido;
        this.errores = Collections.unmodifiableList(errores);
    }

    public static ResultadoValidacion ok() {
        return new ResultadoValidacion(true, Collections.emptyList());
    }

    public static ResultadoValidacion error(List<String> errores) {
        return new ResultadoValidacion(false, errores);
    }

    public boolean esValido() {
        return valido;
    }

    public List<String> getErrores() {
        return errores;
    }
}
