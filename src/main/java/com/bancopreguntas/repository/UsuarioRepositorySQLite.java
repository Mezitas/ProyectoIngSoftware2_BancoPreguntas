package com.bancopreguntas.repository;

import com.bancopreguntas.domain.Rol;
import com.bancopreguntas.domain.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepositorySQLite implements UsuarioRepository {

    private final SQLiteDatabase database;

    public UsuarioRepositorySQLite(SQLiteDatabase database) {
        this.database = database;
    }

    @Override
    public synchronized Usuario guardar(Usuario usuario) {
        String sql = """
                INSERT INTO usuarios (id, nombre, email, rol, password_hash) VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET nombre=excluded.nombre, email=excluded.email,
                    rol=excluded.rol, password_hash=excluded.password_hash
                """;
        try (Connection connection = database.abrirConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, usuario.getId());
            statement.setString(2, usuario.getNombre());
            statement.setString(3, usuario.getEmail());
            statement.setString(4, usuario.getRol().name());
            statement.setString(5, usuario.getPasswordHash());
            statement.executeUpdate();
            return usuario;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el usuario en SQLite.", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return buscar("SELECT * FROM usuarios WHERE id = ?", id);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return buscar("SELECT * FROM usuarios WHERE email = ? COLLATE NOCASE", email);
    }

    @Override
    public List<Usuario> listarTodos() {
        try (Connection connection = database.abrirConexion();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM usuarios ORDER BY nombre");
             ResultSet result = statement.executeQuery()) {
            List<Usuario> usuarios = new ArrayList<>();
            while (result.next()) {
                usuarios.add(mapear(result));
            }
            return usuarios;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron listar los usuarios en SQLite.", e);
        }
    }

    @Override
    public List<Usuario> listarPorRol(Rol rol) {
        try (Connection connection = database.abrirConexion();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM usuarios WHERE rol = ?")) {
            statement.setString(1, rol.name());
            try (ResultSet result = statement.executeQuery()) {
                List<Usuario> usuarios = new ArrayList<>();
                while (result.next()) {
                    usuarios.add(mapear(result));
                }
                return usuarios;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron filtrar usuarios en SQLite.", e);
        }
    }

    private Optional<Usuario> buscar(String sql, String value) {
        try (Connection connection = database.abrirConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapear(result)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar el usuario en SQLite.", e);
        }
    }

    private Usuario mapear(ResultSet result) throws SQLException {
        return new Usuario(result.getString("id"), result.getString("nombre"),
                result.getString("email"), Rol.valueOf(result.getString("rol")),
                result.getString("password_hash"));
    }
}
