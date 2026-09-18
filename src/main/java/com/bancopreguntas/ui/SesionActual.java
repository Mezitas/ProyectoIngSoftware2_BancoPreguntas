package com.bancopreguntas.ui;

import com.bancopreguntas.domain.Usuario;

/**
 * Contenedor simple del usuario actualmente activo en la aplicacion de
 * escritorio. En esta entrega se selecciona desde un combo en la ventana
 * principal (no hay HU de autenticacion en el primer corte), pero centraliza
 * el concepto de "usuario en sesion" para toda la capa de presentacion.
 */
public final class SesionActual {

    private static Usuario usuarioActual;

    private SesionActual() {
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }
}
