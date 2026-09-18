package com.bancopreguntas.ui;

import com.bancopreguntas.domain.NivelDificultad;
import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.service.ValidacionException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Vista (micro-patron MVC) para la HU01: creacion de preguntas de seleccion
 * multiple con unica respuesta siguiendo los principios de Diseno Centrado
 * en Evidencia.
 */
public class PanelCrearPregunta extends JPanel {

    private final PreguntaController controller;
    private final Runnable alGuardarExitoso;

    private final JTextArea txtContexto = new JTextArea(3, 30);
    private final JTextField txtPreguntaDirecta = new JTextField();
    private final JTextField[] txtDistractores = new JTextField[4];
    private final JTextField txtRespuestaCorrecta = new JTextField();
    private final JTextArea txtJustificacion = new JTextArea(3, 30);
    private final JTextField txtBibliografia = new JTextField();
    private final JTextField txtCompetencia = new JTextField();
    private final JTextField txtTema = new JTextField();
    private final JTextField txtSubtema = new JTextField();
    private final JComboBox<NivelDificultad> cmbNivelDificultad =
            new JComboBox<>(NivelDificultad.values());

    private final JLabel lblMensaje = new JLabel(" ");

    public PanelCrearPregunta(PreguntaController controller, Runnable alGuardarExitoso) {
        this.controller = controller;
        this.alGuardarExitoso = alGuardarExitoso;
        construirUI();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;

        int fila = 0;
        fila = agregarCampo(form, gc, fila, "Contexto:", new JScrollPane(txtContexto));
        fila = agregarCampo(form, gc, fila, "Pregunta directa:", txtPreguntaDirecta);

        for (int i = 0; i < 4; i++) {
            txtDistractores[i] = new JTextField();
            fila = agregarCampo(form, gc, fila, "Distractor " + (i + 1) + ":", txtDistractores[i]);
        }

        fila = agregarCampo(form, gc, fila, "Respuesta correcta:", txtRespuestaCorrecta);
        fila = agregarCampo(form, gc, fila, "Justificación:", new JScrollPane(txtJustificacion));
        fila = agregarCampo(form, gc, fila, "Bibliografía:", txtBibliografia);
        fila = agregarCampo(form, gc, fila, "Competencia:", txtCompetencia);
        fila = agregarCampo(form, gc, fila, "Tema:", txtTema);
        fila = agregarCampo(form, gc, fila, "Subtema:", txtSubtema);
        fila = agregarCampo(form, gc, fila, "Nivel de dificultad:", cmbNivelDificultad);

        JButton btnGuardar = new JButton("Guardar pregunta (queda en Borrador)");
        btnGuardar.addActionListener(e -> guardar());

        gc.gridx = 1;
        gc.gridy = fila;
        form.add(btnGuardar, gc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
        lblMensaje.setForeground(new Color(198, 40, 40));
        add(lblMensaje, BorderLayout.SOUTH);
    }

    private int agregarCampo(JPanel form, GridBagConstraints gc, int fila, String etiqueta, Component campo) {
        gc.gridx = 0;
        gc.gridy = fila;
        gc.weightx = 0;
        form.add(new JLabel(etiqueta), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        form.add(campo, gc);
        return fila + 1;
    }

    private void guardar() {
        List<String> distractores = new ArrayList<>();
        for (JTextField txt : txtDistractores) {
            distractores.add(txt.getText());
        }

        Pregunta pregunta = Pregunta.builder()
                .contexto(txtContexto.getText())
                .preguntaDirecta(txtPreguntaDirecta.getText())
                .distractores(distractores)
                .respuestaCorrecta(txtRespuestaCorrecta.getText())
                .justificacion(txtJustificacion.getText())
                .bibliografia(txtBibliografia.getText())
                .competencia(txtCompetencia.getText())
                .tema(txtTema.getText())
                .subtema(txtSubtema.getText())
                .nivelDificultad((NivelDificultad) cmbNivelDificultad.getSelectedItem())
                .autorId(SesionActual.getUsuarioActual().getId())
                .build();

        try {
            controller.crearPregunta(pregunta);
            lblMensaje.setForeground(new Color(46, 125, 50));
            lblMensaje.setText("Pregunta guardada correctamente en estado Borrador.");
            limpiarFormulario();
            if (alGuardarExitoso != null) alGuardarExitoso.run();
        } catch (ValidacionException ex) {
            lblMensaje.setForeground(new Color(198, 40, 40));
            lblMensaje.setText("<html>Errores de validación estructural:<br>"
                    + String.join("<br>", ex.getErrores()) + "</html>");
        }
    }

    private void limpiarFormulario() {
        txtContexto.setText("");
        txtPreguntaDirecta.setText("");
        Arrays.stream(txtDistractores).forEach(t -> t.setText(""));
        txtRespuestaCorrecta.setText("");
        txtJustificacion.setText("");
        txtBibliografia.setText("");
        txtCompetencia.setText("");
        txtTema.setText("");
        txtSubtema.setText("");
        cmbNivelDificultad.setSelectedIndex(0);
    }
}
