package com.bancopreguntas.notificacion;

import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.domain.Usuario;

import java.util.List;
import java.util.Objects;

/**
 * Observador concreto (GoF Observer) que, al ser notificado de una
 * asignacion de revisores, envia un correo a cada revisor usando el
 * {@link EmailService} inyectado (principio DIP: depende de la abstraccion,
 * no de una implementacion concreta de envio de correo).
 */
public class NotificadorEmailObservador implements ObservadorAsignacion {

    private final EmailService emailService;

    public NotificadorEmailObservador(EmailService emailService) {
        this.emailService = Objects.requireNonNull(emailService);
    }

    @Override
    public void onRevisoresAsignados(Pregunta pregunta, List<Usuario> revisores) {
        String asunto = "Nueva pregunta asignada para revision";
        String cuerpo = "Se le ha asignado la revision de la pregunta: \""
                + pregunta.getPreguntaDirecta() + "\" (tema: " + pregunta.getTema()
                + ", subtema: " + pregunta.getSubtema() + ").";

        for (Usuario revisor : revisores) {
            emailService.enviarCorreo(revisor.getEmail(), asunto, cuerpo);
        }
    }
}
