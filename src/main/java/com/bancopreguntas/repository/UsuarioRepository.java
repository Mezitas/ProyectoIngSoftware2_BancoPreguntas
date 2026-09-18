package com.bancopreguntas.repository;

import com.bancopreguntas.domain.Rol;
import com.bancopreguntas.domain.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de la capa de datos para usuarios.
 */
public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(String id);

    List<Usuario> listarTodos();

    List<Usuario> listarPorRol(Rol rol);
}
