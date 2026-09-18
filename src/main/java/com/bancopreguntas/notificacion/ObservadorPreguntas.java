package com.bancopreguntas.notificacion;

/**
 * Observador de cambios en las preguntas. Las vistas pueden reaccionar a
 * creaciones y cambios de estado sin quedar acopladas al servicio.
 */
@FunctionalInterface
public interface ObservadorPreguntas {
    void onPreguntasActualizadas();
}
