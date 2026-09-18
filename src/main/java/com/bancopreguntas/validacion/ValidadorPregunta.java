package com.bancopreguntas.validacion;

import com.bancopreguntas.domain.Pregunta;

/**
 * Patron de diseno GoF "Strategy": define el contrato para las distintas
 * estrategias de validacion que se puedan aplicar a una pregunta antes de
 * grabarla (principio Open/Closed: se pueden agregar nuevas estrategias de
 * validacion sin modificar la capa de negocio que las consume).
 */
public interface ValidadorPregunta {
    ResultadoValidacion validar(Pregunta pregunta);
}
