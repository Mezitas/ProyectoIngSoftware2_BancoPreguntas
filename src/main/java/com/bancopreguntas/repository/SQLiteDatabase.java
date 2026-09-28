package com.bancopreguntas.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * Shared SQLite connection factory and schema initializer.
 */
public class SQLiteDatabase {

    private final String jdbcUrl;

    public SQLiteDatabase(Path databaseFile) {
        Objects.requireNonNull(databaseFile, "databaseFile no puede ser nulo");
        try {
            Path parent = databaseFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear el directorio de la base de datos.", e);
        }
        this.jdbcUrl = "jdbc:sqlite:" + databaseFile.toAbsolutePath();
        inicializarEsquema();
    }

    public Connection abrirConexion() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        }
        return connection;
    }

    private void inicializarEsquema() {
        try (Connection connection = abrirConexion(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS usuarios (
                        id TEXT PRIMARY KEY,
                        nombre TEXT NOT NULL,
                        email TEXT NOT NULL COLLATE NOCASE UNIQUE,
                        rol TEXT NOT NULL,
                        password_hash TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS preguntas (
                        id TEXT PRIMARY KEY,
                        contexto TEXT NOT NULL,
                        pregunta_directa TEXT NOT NULL,
                        respuesta_correcta TEXT NOT NULL,
                        justificacion TEXT NOT NULL,
                        bibliografia TEXT NOT NULL,
                        competencia TEXT NOT NULL,
                        tema TEXT NOT NULL,
                        subtema TEXT NOT NULL,
                        nivel_dificultad TEXT NOT NULL,
                        estado TEXT NOT NULL,
                        autor_id TEXT NOT NULL REFERENCES usuarios(id),
                        fecha_creacion TEXT NOT NULL,
                        fecha_actualizacion TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS pregunta_distractores (
                        pregunta_id TEXT NOT NULL REFERENCES preguntas(id) ON DELETE CASCADE,
                        posicion INTEGER NOT NULL,
                        texto TEXT NOT NULL,
                        PRIMARY KEY (pregunta_id, posicion)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS pregunta_revisores (
                        pregunta_id TEXT NOT NULL REFERENCES preguntas(id) ON DELETE CASCADE,
                        revisor_id TEXT NOT NULL REFERENCES usuarios(id),
                        PRIMARY KEY (pregunta_id, revisor_id)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS auditoria (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        actor_id TEXT NOT NULL REFERENCES usuarios(id),
                        pregunta_id TEXT REFERENCES preguntas(id),
                        accion TEXT NOT NULL,
                        detalle TEXT NOT NULL,
                        fecha TEXT NOT NULL
                    )
                    """);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo inicializar el esquema SQLite.", e);
        }
    }
}
