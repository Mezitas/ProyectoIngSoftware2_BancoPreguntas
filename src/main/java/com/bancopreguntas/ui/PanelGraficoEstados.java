package com.bancopreguntas.ui;

import com.bancopreguntas.domain.EstadoPregunta;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Vista estadistica de las preguntas del autor actual. El dibujo se realiza
 * con Swing para no agregar dependencias externas al proyecto.
 */
public class PanelGraficoEstados extends JPanel {

    private static final Color COLOR_BORRADOR = EstadoPregunta.BORRADOR.getColor();
    private static final Color COLOR_REVISION = EstadoPregunta.EN_REVISION.getColor();
    private static final Color COLOR_ELIMINADA = EstadoPregunta.ELIMINADA.getColor();

    private final GraficoCircular grafico = new GraficoCircular();
    private final JLabel lblBorrador = new JLabel();
    private final JLabel lblRevision = new JLabel();
    private final JLabel lblEliminada = new JLabel();

    public PanelGraficoEstados() {
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createTitledBorder("Estado de mis preguntas"));

        add(grafico, BorderLayout.CENTER);

        JPanel leyenda = new JPanel();
        leyenda.setLayout(new BoxLayout(leyenda, BoxLayout.Y_AXIS));
        leyenda.add(crearLeyenda(COLOR_BORRADOR, lblBorrador));
        leyenda.add(crearLeyenda(COLOR_REVISION, lblRevision));
        leyenda.add(crearLeyenda(COLOR_ELIMINADA, lblEliminada));
        add(leyenda, BorderLayout.EAST);
    }

    public void actualizar(Map<String, Integer> cantidades) {
        int borrador = cantidades.getOrDefault("Borrador", 0);
        int revision = cantidades.getOrDefault("En revisión", 0);
        int eliminada = cantidades.getOrDefault("Eliminada", 0);
        grafico.actualizar(borrador, revision, eliminada);
        lblBorrador.setText("Borrador: " + borrador);
        lblRevision.setText("En revisión: " + revision);
        lblEliminada.setText("Eliminadas: " + eliminada);
    }

    private JPanel crearLeyenda(Color color, JLabel texto) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JLabel indicador = new JLabel("■");
        indicador.setForeground(color);
        fila.add(indicador);
        fila.add(texto);
        return fila;
    }

    private static class GraficoCircular extends JPanel {
        private int borrador;
        private int revision;
        private int eliminada;

        GraficoCircular() {
            setPreferredSize(new Dimension(220, 145));
            setOpaque(false);
        }

        void actualizar(int borrador, int revision, int eliminada) {
            this.borrador = borrador;
            this.revision = revision;
            this.eliminada = eliminada;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int total = borrador + revision + eliminada;
            int diametro = Math.min(getHeight() - 20, 110);
            int x = 20;
            int y = (getHeight() - diametro) / 2;

            if (total == 0) {
                g.setColor(new Color(224, 224, 224));
                g.fillOval(x, y, diametro, diametro);
            } else {
                int inicio = 90;
                int anguloBorrador = (int) Math.round(360.0 * borrador / total);
                int anguloRevision = (int) Math.round(360.0 * revision / total);
                g.setColor(COLOR_BORRADOR);
                g.fillArc(x, y, diametro, diametro, inicio, anguloBorrador);
                g.setColor(COLOR_REVISION);
                g.fillArc(x, y, diametro, diametro, inicio + anguloBorrador, anguloRevision);
                g.setColor(COLOR_ELIMINADA);
                g.fillArc(x, y, diametro, diametro,
                        inicio + anguloBorrador + anguloRevision,
                        360 - anguloBorrador - anguloRevision);
            }

            g.setColor(Color.WHITE);
            g.fillOval(x + diametro / 3, y + diametro / 3, diametro / 3, diametro / 3);
            g.dispose();
        }
    }
}
