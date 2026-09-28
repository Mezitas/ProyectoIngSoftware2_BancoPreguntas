package com.bancopreguntas.ui;

import com.bancopreguntas.domain.Pregunta;

import javax.swing.*;
import java.awt.*;

public class PanelMisRevisiones extends JPanel {

    private final PreguntaController controller;
    private final DefaultListModel<Pregunta> modelo = new DefaultListModel<>();
    private final JList<Pregunta> lista = new JList<>(modelo);
    private final JLabel mensaje = new JLabel(" ");
    private final JButton aprobar = new JButton("Aceptar / aprobar");
    private final JButton rechazar = new JButton("Denegar / rechazar");

    public PanelMisRevisiones(PreguntaController controller) {
        this.controller = controller;
        construirUI();
        cargar();
    }

    private void construirUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JLabel("Preguntas asignadas para revisión:"), BorderLayout.NORTH);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setCellRenderer((list, pregunta, index, selected, focus) -> {
            JLabel label = new JLabel(pregunta.getTema() + " — " + pregunta.getPreguntaDirecta());
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            label.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
            label.setForeground(selected ? list.getSelectionForeground() : list.getForeground());
            return label;
        });
        lista.addListSelectionListener(event -> actualizarBotones());
        add(new JScrollPane(lista), BorderLayout.CENTER);

        JButton ver = new JButton("Ver pregunta");
        ver.addActionListener(event -> verPregunta());
        aprobar.addActionListener(event -> resolver(true));
        rechazar.addActionListener(event -> resolver(false));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(ver);
        aprobar.setEnabled(false);
        rechazar.setEnabled(false);
        botones.add(aprobar);
        botones.add(rechazar);
        JPanel pie = new JPanel(new BorderLayout());
        pie.add(botones, BorderLayout.CENTER);
        pie.add(mensaje, BorderLayout.SOUTH);
        add(pie, BorderLayout.SOUTH);
    }

    public void cargar() {
        modelo.clear();
        controller.listarMisRevisiones().forEach(modelo::addElement);
        mensaje.setText(" ");
    }

    private void actualizarBotones() {
        boolean seleccion = lista.getSelectedValue() != null;
        aprobar.setEnabled(seleccion);
        rechazar.setEnabled(seleccion);
    }

    private void verPregunta() {
        Pregunta pregunta = lista.getSelectedValue();
        if (pregunta == null) {
            mensaje.setText("Seleccione una pregunta.");
            return;
        }
        String texto = "Contexto: " + pregunta.getContexto()
                + "\nPregunta: " + pregunta.getPreguntaDirecta()
                + "\nDistractores: " + String.join(" | ", pregunta.getDistractores())
                + "\nRespuesta correcta: " + pregunta.getRespuestaCorrecta()
                + "\nJustificación: " + pregunta.getJustificacion()
                + "\nBibliografía: " + pregunta.getBibliografia()
                + "\nCompetencia: " + pregunta.getCompetencia()
                + "\nTema: " + pregunta.getTema() + " / " + pregunta.getSubtema()
                + "\nDificultad: " + pregunta.getNivelDificultad();
        JTextArea detalle = new JTextArea(texto, 18, 60);
        detalle.setEditable(false);
        detalle.setLineWrap(true);
        detalle.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(detalle), "Pregunta asignada",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void resolver(boolean aprobada) {
        Pregunta pregunta = lista.getSelectedValue();
        if (pregunta == null) {
            mensaje.setText("Seleccione una pregunta.");
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this,
                aprobada ? "¿Confirma que desea aprobar la pregunta?" : "¿Confirma que desea rechazar la pregunta?",
                "Confirmar decisión", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) return;
        try {
            controller.resolverRevision(pregunta.getId(), aprobada);
            cargar();
            mensaje.setForeground(new Color(46, 125, 50));
            mensaje.setText(aprobada ? "Pregunta aprobada." : "Pregunta rechazada.");
        } catch (IllegalStateException | SecurityException ex) {
            mensaje.setForeground(new Color(198, 40, 40));
            mensaje.setText(ex.getMessage());
        }
    }
}
