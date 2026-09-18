package com.bancopreguntas.domain;

/**
 * Roles de usuario soportados por la aplicacion.
 */
public enum Rol {
    AUTOR("Autor de preguntas"),
    ADMINISTRADOR("Administrador"),
    REVISOR("Revisor");

    private final String etiqueta;

    Rol(String etiqueta) {
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
