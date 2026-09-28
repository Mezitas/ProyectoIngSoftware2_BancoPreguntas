package com.bancopreguntas.service;

import com.bancopreguntas.domain.Usuario;

import java.util.Optional;

public interface AutenticacionService {
    Optional<Usuario> autenticar(String email, String password);
}
