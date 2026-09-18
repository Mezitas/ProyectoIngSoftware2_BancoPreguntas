package com.bancopreguntas.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Usuario del sistema: autor de preguntas, administrador o revisor.
 */
public class Usuario implements Serializable {

    private final String id;
    private String nombre;
    private String email;
    private Rol rol;

    public Usuario(String nombre, String email, Rol rol) {
        this(UUID.randomUUID().toString(), nombre, email, rol);
    }

    public Usuario(String id, String nombre, String email, Rol rol) {
        this.id = Objects.requireNonNull(id, "id no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre, "nombre no puede ser nulo");
        this.email = Objects.requireNonNull(email, "email no puede ser nulo");
        this.rol = Objects.requireNonNull(rol, "rol no puede ser nulo");
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre + " (" + rol.getEtiqueta() + ")";
    }
}
