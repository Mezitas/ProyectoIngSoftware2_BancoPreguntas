package com.bancopreguntas.ui;

import com.bancopreguntas.domain.Rol;
import com.bancopreguntas.domain.Usuario;
import com.bancopreguntas.notificacion.SujetoPreguntas;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal de la aplicacion de escritorio. Compone las tres vistas
 * pestañas disponibles según el rol del usuario autenticado.
 */
public class VentanaPrincipal extends JFrame {

    private final PreguntaController controller;
    private final SujetoPreguntas sujetoPreguntas;

    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel usuarioActivo = new JLabel();

    private PanelListarPreguntas panelListar;

    public VentanaPrincipal(PreguntaController controller, SujetoPreguntas sujetoPreguntas, Usuario usuario) {
        super("Banco de Preguntas Saber Pro - Ingeniería de Software II");
        this.controller = controller;
        this.sujetoPreguntas = sujetoPreguntas;
        SesionActual.setUsuarioActual(usuario);
        construirUI();
    }

    private void construirUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel barraSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraSuperior.add(usuarioActivo);
        JButton cerrarSesion = new JButton("Cerrar sesión");
        cerrarSesion.addActionListener(event -> cambiarUsuario());
        barraSuperior.add(cerrarSesion);
        add(barraSuperior, BorderLayout.NORTH);

        add(tabs, BorderLayout.CENTER);
        reconstruirTabs(SesionActual.getUsuarioActual());
    }

    private void reconstruirTabs(Usuario usuario) {
        if (panelListar != null) {
            panelListar.cerrar();
        }
        tabs.removeAll();
        usuarioActivo.setText("Sesión: " + usuario.getNombre() + " — " + usuario.getRol().getEtiqueta());
        panelListar = null;

        if (usuario.getRol() == Rol.AUTOR) {
            PanelCrearPregunta panelCrear = new PanelCrearPregunta(controller, this::refrescarListado);
            panelListar = new PanelListarPreguntas(controller, sujetoPreguntas, this::abrirEdicion);
            tabs.addTab("Crear pregunta", panelCrear);
            tabs.addTab("Mis preguntas", panelListar);
        } else if (usuario.getRol() == Rol.ADMINISTRADOR) {
            tabs.addTab("Asignar revisores", new PanelAsignarRevisores(controller));
        } else {
            tabs.addTab("Mis revisiones", new PanelMisRevisiones(controller));
        }
    }

    private void cambiarUsuario() {
        Usuario usuario = new DialogoLogin(this, controller).mostrar();
        if (usuario != null) {
            SesionActual.setUsuarioActual(usuario);
            reconstruirTabs(usuario);
        }
    }

    private void abrirEdicion(com.bancopreguntas.domain.Pregunta pregunta) {
        JDialog dialogo = new JDialog(this, "Editar pregunta", true);
        PanelCrearPregunta formulario = new PanelCrearPregunta(controller, () -> {
            if (panelListar != null) panelListar.cargar();
            dialogo.dispose();
        }, pregunta);
        dialogo.setContentPane(formulario);
        dialogo.setSize(700, 600);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private void refrescarListado() {
        if (panelListar != null) {
            panelListar.cargar();
        }
    }
}
