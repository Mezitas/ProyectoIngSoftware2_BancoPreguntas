package com.bancopreguntas.repository;

import java.time.LocalDateTime;

public interface RepositorioAuditoria {
    void registrar(String actorId, String preguntaId, String accion, String detalle, LocalDateTime fecha);
}
