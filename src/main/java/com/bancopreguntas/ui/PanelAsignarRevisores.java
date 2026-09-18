package com.bancopreguntas.ui;

import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.domain.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vista (MVC) para la HU04: el administrador asigna al menos un revisor a
 * las preguntas en estado "Pendiente de revisión"; al confirmar, el sistema
 * notifica por correo (patron Observer en la capa de negocio).
 */
public class PanelAsignarRevisores extends JPanel {

    private final PreguntaController controller;

    private final DefaultListModel<Pregunta> modeloPreguntas = new DefaultListModel<>();
    private final JList<Pregunta> listaPreguntas = new JList<>(modeloPreguntas);

    private final DefaultListModel<Usuario> modeloRevisores = new DefaultListModel<>();
    private final JList<Usuario> listaRevisores = new JList<>(modeloRevisores);

    private final JLabel lblMensaje = new JLabel(" ");

    public PanelAsignarRevisores(PreguntaController controller) {
        this.controller = controller;
        construirUI();
        cargar();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel centro = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel panelIzq = new JPanel(new BorderLayout());
        panelIzq.add(new JLabel("Preguntas pendientes de revisión:"), BorderLayout.NORTH);
        listaPreguntas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelIzq.add(new JScrollPane(listaPreguntas), BorderLayout.CENTER);

        JPanel panelDer = new JPanel(new BorderLayout());
        panelDer.add(new JLabel("Revisores disponibles (selección múltiple):"), BorderLayout.NORTH);
        listaRevisores.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        panelDer.add(new JScrollPane(listaRevisores), BorderLayout.CENTER);

        centro.add(panelIzq);
        centro.add(panelDer);
        add(centro, BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout());
        JButton btnAsignar = new JButton("Asignar revisor(es) y notificar por correo");
        btnAsignar.addActionListener(e -> asignar());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(btnAsignar);
        sur.add(botones, BorderLayout.NORTH);
        lblMensaje.setForeground(new Color(198, 40, 40));
        sur.add(lblMensaje, BorderLayout.SOUTH);
        add(sur, BorderLayout.SOUTH);
    }

    public void cargar() {
        modeloPreguntas.clear();
        List<Pregunta> pendientes = controller.listarPendientesDeRevision();
        pendientes.forEach(modeloPreguntas::addElement);

        modeloRevisores.clear();
        controller.listarRevisoresDisponibles().forEach(modeloRevisores::addElement);

        lblMensaje.setText(" ");
    }

    private void asignar() {
        Pregunta pregunta = listaPreguntas.getSelectedValue();
        List<Usuario> revisores = listaRevisores.getSelectedValuesList();

        if (pregunta == null) {
            lblMensaje.setText("Seleccione una pregunta pendiente de revisión.");
            return;
        }
        if (revisores.isEmpty()) {
            lblMensaje.setText("Seleccione al menos un revisor.");
            return;
        }

        try {
            List<String> revisoresIds = revisores.stream().map(Usuario::getId).collect(Collectors.toList());
            controller.asignarRevisores(pregunta.getId(), revisoresIds);
            lblMensaje.setForeground(new Color(46, 125, 50));
            lblMensaje.setText("Revisor(es) asignado(s). Se envió la notificación por correo (ver consola).");
            cargar();
        } catch (RuntimeException ex) {
            lblMensaje.setForeground(new Color(198, 40, 40));
            lblMensaje.setText(ex.getMessage());
        }
    }
}
