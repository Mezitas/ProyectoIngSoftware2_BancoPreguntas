package com.bancopreguntas.notificacion;

import java.time.LocalDateTime;

/**
 * Implementacion de {@link EmailService} usada para la sustentacion: simula
 * el envio del correo imprimiendolo en consola, de forma que el envio real
 * (SMTP, servicio en la nube, etc.) pueda conectarse mas adelante
 * implementando la misma interfaz, sin tocar el resto de la aplicacion.
 */
public class ConsoleEmailService implements EmailService {

    @Override
    public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        System.out.println("========== CORREO ENVIADO ==========");
        System.out.println("Fecha     : " + LocalDateTime.now());
        System.out.println("Para      : " + destinatario);
        System.out.println("Asunto    : " + asunto);
        System.out.println("Mensaje   : " + cuerpo);
        System.out.println("=====================================");
    }
}
