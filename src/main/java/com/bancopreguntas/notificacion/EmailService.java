package com.bancopreguntas.notificacion;

/**
 * Abstraccion (principio DIP) para el envio de correos. Permite sustituir la
 * implementacion de consola usada en esta entrega por un servicio SMTP real
 * sin modificar la logica de notificacion.
 */
public interface EmailService {
    void enviarCorreo(String destinatario, String asunto, String cuerpo);
}
