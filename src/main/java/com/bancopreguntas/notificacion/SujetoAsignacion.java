package com.bancopreguntas.notificacion;

import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.domain.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Sujeto (publicador) del patron Observer: mantiene la lista de
 * {@link ObservadorAsignacion} interesados y los notifica cuando el
 * servicio de negocio asigna revisores a una pregunta.
 */
public class SujetoAsignacion {

    private final List<ObservadorAsignacion> observadores = new ArrayList<>();

    public void suscribir(ObservadorAsignacion observador) {
        observadores.add(observador);
    }

    public void desuscribir(ObservadorAsignacion observador) {
        observadores.remove(observador);
    }

    public void notificarAsignacion(Pregunta pregunta, List<Usuario> revisores) {
        for (ObservadorAsignacion observador : observadores) {
            observador.onRevisoresAsignados(pregunta, revisores);
        }
    }
}
