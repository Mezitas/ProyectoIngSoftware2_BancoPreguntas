package com.bancopreguntas.repository;

import java.time.LocalDateTime;

public class RepositorioAuditoriaNoOp implements RepositorioAuditoria {
    @Override
    public void registrar(String actorId, String preguntaId, String accion, String detalle, LocalDateTime fecha) {
        // Implementación vacía para pruebas unitarias aisladas.
    }
}
