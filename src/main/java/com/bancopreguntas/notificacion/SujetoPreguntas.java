package com.bancopreguntas.notificacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Sujeto del patron Observer para mantener sincronizadas las vistas con el
 * estado actual de las preguntas.
 */
public class SujetoPreguntas {

    private final List<ObservadorPreguntas> observadores = new ArrayList<>();

    public void suscribir(ObservadorPreguntas observador) {
        observadores.add(observador);
    }

    public void desuscribir(ObservadorPreguntas observador) {
        observadores.remove(observador);
    }

    public void notificarActualizacion() {
        for (ObservadorPreguntas observador : List.copyOf(observadores)) {
            observador.onPreguntasActualizadas();
        }
    }
}
