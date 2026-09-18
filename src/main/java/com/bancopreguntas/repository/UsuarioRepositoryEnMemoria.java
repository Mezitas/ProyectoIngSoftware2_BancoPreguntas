package com.bancopreguntas.repository;

import com.bancopreguntas.domain.Rol;
import com.bancopreguntas.domain.Usuario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementacion en memoria de {@link UsuarioRepository} (patron Singleton).
 */
public class UsuarioRepositoryEnMemoria implements UsuarioRepository {

    private static UsuarioRepositoryEnMemoria instancia;

    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();

    public UsuarioRepositoryEnMemoria() {
    }

    public static synchronized UsuarioRepositoryEnMemoria getInstance() {
        if (instancia == null) {
            instancia = new UsuarioRepositoryEnMemoria();
        }
        return instancia;
    }

    @Override
    public synchronized Usuario guardar(Usuario usuario) {
        usuarios.put(usuario.getId(), usuario);
        return usuario;
    }

    @Override
    public synchronized Optional<Usuario> buscarPorId(String id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    @Override
    public synchronized List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios.values());
    }

    @Override
    public synchronized List<Usuario> listarPorRol(Rol rol) {
        return usuarios.values().stream()
                .filter(u -> u.getRol() == rol)
                .collect(Collectors.toList());
    }
}
