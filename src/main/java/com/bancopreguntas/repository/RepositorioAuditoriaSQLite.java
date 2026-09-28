package com.bancopreguntas.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class RepositorioAuditoriaSQLite implements RepositorioAuditoria {

    private final SQLiteDatabase database;

    public RepositorioAuditoriaSQLite(SQLiteDatabase database) {
        this.database = database;
    }

    @Override
    public void registrar(String actorId, String preguntaId, String accion, String detalle, LocalDateTime fecha) {
        String sql = "INSERT INTO auditoria (actor_id, pregunta_id, accion, detalle, fecha) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = database.abrirConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, actorId);
            statement.setString(2, preguntaId);
            statement.setString(3, accion);
            statement.setString(4, detalle);
            statement.setString(5, fecha.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo registrar la auditoría.", e);
        }
    }
}
