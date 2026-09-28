package com.bancopreguntas.service;

import com.bancopreguntas.domain.Usuario;
import com.bancopreguntas.repository.UsuarioRepository;

import java.util.Optional;

public class AutenticacionServiceImpl implements AutenticacionService {

    private final UsuarioRepository usuarioRepository;

    public AutenticacionServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Optional<Usuario> autenticar(String email, String password) {
        if (email == null || email.isBlank() || password == null) {
            return Optional.empty();
        }
        return usuarioRepository.buscarPorEmail(email.trim())
                .filter(usuario -> PasswordHasher.verificar(password, usuario.getPasswordHash()));
    }
}
