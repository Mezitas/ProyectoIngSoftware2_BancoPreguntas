package com.bancopreguntas.ui;

import com.bancopreguntas.domain.Usuario;

import javax.swing.*;
import java.awt.*;

public class DialogoLogin extends JDialog {

    private final PreguntaController controller;
    private final JTextField email = new JTextField(24);
    private final JPasswordField password = new JPasswordField(24);
    private final JLabel mensaje = new JLabel(" ");
    private Usuario usuarioAutenticado;

    public DialogoLogin(Window owner, PreguntaController controller) {
        super(owner, "Inicio de sesión - Banco de Preguntas", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        construirUI();
    }

    public Usuario mostrar() {
        setVisible(true);
        return usuarioAutenticado;
    }

    private void construirUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        formulario.add(new JLabel("Correo:"), gc);
        gc.gridx = 1;
        formulario.add(email, gc);
        gc.gridx = 0;
        gc.gridy = 1;
        formulario.add(new JLabel("Contraseña:"), gc);
        gc.gridx = 1;
        formulario.add(password, gc);

        JButton ingresar = new JButton("Ingresar");
        ingresar.addActionListener(event -> ingresar());
        password.addActionListener(event -> ingresar());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(ingresar);
        mensaje.setForeground(new Color(198, 40, 40));
        JPanel contenido = new JPanel(new BorderLayout());
        contenido.add(formulario, BorderLayout.CENTER);
        contenido.add(mensaje, BorderLayout.NORTH);
        contenido.add(botones, BorderLayout.SOUTH);
        setContentPane(contenido);
        getRootPane().setDefaultButton(ingresar);
        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    private void ingresar() {
        usuarioAutenticado = controller.autenticar(email.getText(), new String(password.getPassword()));
        if (usuarioAutenticado == null) {
            mensaje.setText("Correo o contraseña incorrectos.");
            password.setText("");
            return;
        }
        dispose();
    }
}
