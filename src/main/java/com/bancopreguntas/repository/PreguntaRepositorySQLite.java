package com.bancopreguntas.repository;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.NivelDificultad;
import com.bancopreguntas.domain.Pregunta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PreguntaRepositorySQLite implements PreguntaRepository {

    private final SQLiteDatabase database;

    public PreguntaRepositorySQLite(SQLiteDatabase database) {
        this.database = database;
    }

    @Override
    public synchronized Pregunta guardar(Pregunta pregunta) {
        String sql = """
                INSERT INTO preguntas (id, contexto, pregunta_directa, respuesta_correcta, justificacion,
                    bibliografia, competencia, tema, subtema, nivel_dificultad, estado, autor_id,
                    fecha_creacion, fecha_actualizacion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    contexto=excluded.contexto, pregunta_directa=excluded.pregunta_directa,
                    respuesta_correcta=excluded.respuesta_correcta, justificacion=excluded.justificacion,
                    bibliografia=excluded.bibliografia, competencia=excluded.competencia, tema=excluded.tema,
                    subtema=excluded.subtema, nivel_dificultad=excluded.nivel_dificultad,
                    estado=excluded.estado, autor_id=excluded.autor_id,
                    fecha_creacion=excluded.fecha_creacion, fecha_actualizacion=excluded.fecha_actualizacion
                """;
        try (Connection connection = database.abrirConexion()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, pregunta.getId());
                statement.setString(2, pregunta.getContexto());
                statement.setString(3, pregunta.getPreguntaDirecta());
                statement.setString(4, pregunta.getRespuestaCorrecta());
                statement.setString(5, pregunta.getJustificacion());
                statement.setString(6, pregunta.getBibliografia());
                statement.setString(7, pregunta.getCompetencia());
                statement.setString(8, pregunta.getTema());
                statement.setString(9, pregunta.getSubtema());
                statement.setString(10, pregunta.getNivelDificultad().name());
                statement.setString(11, pregunta.getEstado().name());
                statement.setString(12, pregunta.getAutorId());
                statement.setString(13, pregunta.getFechaCreacion().toString());
                statement.setString(14, pregunta.getFechaActualizacion().toString());
                statement.executeUpdate();
            }
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM pregunta_distractores WHERE pregunta_id = ?")) {
                delete.setString(1, pregunta.getId());
                delete.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO pregunta_distractores (pregunta_id, posicion, texto) VALUES (?, ?, ?)")) {
                for (int i = 0; i < pregunta.getDistractores().size(); i++) {
                    insert.setString(1, pregunta.getId());
                    insert.setInt(2, i);
                    insert.setString(3, pregunta.getDistractores().get(i));
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM pregunta_revisores WHERE pregunta_id = ?")) {
                delete.setString(1, pregunta.getId());
                delete.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO pregunta_revisores (pregunta_id, revisor_id) VALUES (?, ?)")) {
                for (String revisorId : pregunta.getRevisoresAsignados()) {
                    insert.setString(1, pregunta.getId());
                    insert.setString(2, revisorId);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            connection.commit();
            return pregunta;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar la pregunta en SQLite.", e);
        }
    }

    @Override
    public Optional<Pregunta> buscarPorId(String id) {
        try (Connection connection = database.abrirConexion();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM preguntas WHERE id = ?")) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapear(connection, result)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar la pregunta en SQLite.", e);
        }
    }

    @Override
    public List<Pregunta> listarTodas() {
        return consultar("SELECT * FROM preguntas ORDER BY fecha_creacion DESC", null);
    }

    @Override
    public List<Pregunta> listarPorAutor(String autorId) {
        return consultar("SELECT * FROM preguntas WHERE autor_id = ? ORDER BY fecha_creacion DESC", autorId);
    }

    @Override
    public List<Pregunta> listarPorEstado(EstadoPregunta estado) {
        return consultar("SELECT * FROM preguntas WHERE estado = ? ORDER BY fecha_creacion DESC", estado.name());
    }

    @Override
    public List<Pregunta> listarAsignadasARevisor(String revisorId) {
        String sql = """
                SELECT p.* FROM preguntas p
                JOIN pregunta_revisores r ON r.pregunta_id = p.id
                WHERE r.revisor_id = ? AND p.estado = ?
                ORDER BY p.fecha_actualizacion DESC
                """;
        try (Connection connection = database.abrirConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, revisorId);
            statement.setString(2, EstadoPregunta.EN_REVISION.name());
            try (ResultSet result = statement.executeQuery()) {
                List<Pregunta> preguntas = new ArrayList<>();
                while (result.next()) {
                    preguntas.add(mapear(connection, result));
                }
                return preguntas;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron listar las preguntas del revisor.", e);
        }
    }

    private List<Pregunta> consultar(String sql, String parametro) {
        try (Connection connection = database.abrirConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parametro != null) {
                statement.setString(1, parametro);
            }
            try (ResultSet result = statement.executeQuery()) {
                List<Pregunta> preguntas = new ArrayList<>();
                while (result.next()) {
                    preguntas.add(mapear(connection, result));
                }
                return preguntas;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar las preguntas en SQLite.", e);
        }
    }

    private Pregunta mapear(Connection connection, ResultSet result) throws SQLException {
        String id = result.getString("id");
        List<String> distractores = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT texto FROM pregunta_distractores WHERE pregunta_id = ? ORDER BY posicion")) {
            statement.setString(1, id);
            try (ResultSet options = statement.executeQuery()) {
                while (options.next()) {
                    distractores.add(options.getString(1));
                }
            }
        }
        List<String> revisores = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT revisor_id FROM pregunta_revisores WHERE pregunta_id = ? ORDER BY revisor_id")) {
            statement.setString(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    revisores.add(rows.getString(1));
                }
            }
        }
        return Pregunta.builder()
                .id(id)
                .contexto(result.getString("contexto"))
                .preguntaDirecta(result.getString("pregunta_directa"))
                .distractores(distractores)
                .respuestaCorrecta(result.getString("respuesta_correcta"))
                .justificacion(result.getString("justificacion"))
                .bibliografia(result.getString("bibliografia"))
                .competencia(result.getString("competencia"))
                .tema(result.getString("tema"))
                .subtema(result.getString("subtema"))
                .nivelDificultad(NivelDificultad.valueOf(result.getString("nivel_dificultad")))
                .estado(EstadoPregunta.valueOf(result.getString("estado")))
                .autorId(result.getString("autor_id"))
                .revisoresAsignados(revisores)
                .fechas(LocalDateTime.parse(result.getString("fecha_creacion")),
                        LocalDateTime.parse(result.getString("fecha_actualizacion")))
                .build();
    }
}
