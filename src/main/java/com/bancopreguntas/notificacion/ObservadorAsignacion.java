package com.bancopreguntas.notificacion;

import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.domain.Usuario;

import java.util.List;

/**
 * Patron de diseno GoF "Observer": contrato que deben cumplir los
 * interesados en reaccionar cuando se asignan revisores a una pregunta
 * (HU04). Desacopla al servicio de negocio (el "sujeto") de los mecanismos
 * concretos de notificacion (correo, log, futuras integraciones), cumpliendo
 * el principio Open/Closed.
 */
public interface ObservadorAsignacion {
    void onRevisoresAsignados(Pregunta pregunta, List<Usuario> revisores);
}
