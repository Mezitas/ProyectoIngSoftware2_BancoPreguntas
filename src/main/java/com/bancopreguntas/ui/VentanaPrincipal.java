package com.bancopreguntas.ui;

import com.bancopreguntas.domain.Rol;
import com.bancopreguntas.domain.Usuario;
import com.bancopreguntas.notificacion.SujetoPreguntas;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal de la aplicacion de escritorio. Compone las tres vistas
 * (Crear, Mis preguntas, Asignar revisores) en pestanas y ofrece un
 * selector de "usuario actual" para poder demostrar en vivo los flujos de
 * autor y de administrador sin necesidad de un modulo de autenticacion,
 * el cual esta fuera del alcance funcional del primer corte.
 */
public class VentanaPrincipal extends JFrame {

    private final PreguntaController controller;
    private final SujetoPreguntas sujetoPreguntas;

    private final JComboBox<Usuario> cmbUsuario = new JComboBox<>();
    private final JTabbedPane tabs = new JTabbedPane();

    private PanelListarPreguntas panelListar;
    private PanelAsignarRevisores panelAsignar;
    private int indiceTabAsignar = -1;

    public VentanaPrincipal(PreguntaController controller, SujetoPreguntas sujetoPreguntas) {
        super("Banco de Preguntas Saber Pro - Ingeniería de Software II");
        this.controller = controller;
        this.sujetoPreguntas = sujetoPreguntas;
        construirUI();
    }

    private void construirUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel barraSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraSuperior.add(new JLabel("Usuario en sesión (demo):"));

        controller.listarUsuarios().forEach(cmbUsuario::addItem);
        cmbUsuario.addActionListener(e -> onCambioUsuario());
        barraSuperior.add(cmbUsuario);
        add(barraSuperior, BorderLayout.NORTH);

        add(tabs, BorderLayout.CENTER);

        if (cmbUsuario.getItemCount() > 0) {
            cmbUsuario.setSelectedIndex(0);
        }
    }

    private void onCambioUsuario() {
        Usuario seleccionado = (Usuario) cmbUsuario.getSelectedItem();
        if (seleccionado == null) return;
        SesionActual.setUsuarioActual(seleccionado);
        reconstruirTabs(seleccionado);
    }

    private void reconstruirTabs(Usuario usuario) {
        if (panelListar != null) {
            panelListar.cerrar();
        }
        tabs.removeAll();

        PanelCrearPregunta panelCrear = new PanelCrearPregunta(controller, this::refrescarListado);
        panelListar = new PanelListarPreguntas(controller, sujetoPreguntas);

        tabs.addTab("Crear pregunta", panelCrear);
        tabs.addTab("Mis preguntas", panelListar);

        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            panelAsignar = new PanelAsignarRevisores(controller);
            tabs.addTab("Asignar revisores", panelAsignar);
            indiceTabAsignar = tabs.indexOfComponent(panelAsignar);
        } else {
            panelAsignar = null;
            indiceTabAsignar = -1;
        }
    }

    private void refrescarListado() {
        if (panelListar != null) {
            panelListar.cargar();
        }
    }
}
