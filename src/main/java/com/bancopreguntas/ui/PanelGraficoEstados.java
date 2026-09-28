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

    private static final Color COLOR_BORRADOR = new Color(230, 126, 0);
    private static final Color COLOR_REVISION = new Color(21, 101, 192);
    private static final Color COLOR_APROBADA = new Color(0, 128, 64);
    private static final Color COLOR_RECHAZADA = new Color(198, 40, 40);
    private static final Color COLOR_ELIMINADA = new Color(97, 97, 97);

    private final GraficoCircular grafico = new GraficoCircular();
    private final JLabel lblBorrador = new JLabel();
    private final JLabel lblRevision = new JLabel();
    private final JLabel lblAprobada = new JLabel();
    private final JLabel lblRechazada = new JLabel();
    private final JLabel lblEliminada = new JLabel();

    public PanelGraficoEstados() {
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createTitledBorder("Estado de mis preguntas"));

        add(grafico, BorderLayout.CENTER);

        JPanel leyenda = new JPanel();
        leyenda.setLayout(new BoxLayout(leyenda, BoxLayout.Y_AXIS));
        leyenda.add(crearLeyenda(COLOR_BORRADOR, lblBorrador));
        leyenda.add(crearLeyenda(COLOR_REVISION, lblRevision));
        leyenda.add(crearLeyenda(COLOR_APROBADA, lblAprobada));
        leyenda.add(crearLeyenda(COLOR_RECHAZADA, lblRechazada));
        leyenda.add(crearLeyenda(COLOR_ELIMINADA, lblEliminada));
        add(leyenda, BorderLayout.EAST);
    }

    public void actualizar(Map<String, Integer> cantidades) {
        int borrador = cantidades.getOrDefault("Borrador", 0);
        int revision = cantidades.getOrDefault("En revisión", 0);
        int aprobada = cantidades.getOrDefault("Aprobada", 0);
        int rechazada = cantidades.getOrDefault("Rechazada", 0);
        int eliminada = cantidades.getOrDefault("Eliminada", 0);
        grafico.actualizar(borrador, revision, aprobada, rechazada, eliminada);
        lblBorrador.setText("Borrador: " + borrador);
        lblRevision.setText("En revisión: " + revision);
        lblAprobada.setText("Aprobada: " + aprobada);
        lblRechazada.setText("Rechazada: " + rechazada);
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
        private int aprobada;
        private int rechazada;
        private int eliminada;

        GraficoCircular() {
            setPreferredSize(new Dimension(220, 145));
            setOpaque(false);
        }

        void actualizar(int borrador, int revision, int aprobada, int rechazada, int eliminada) {
            this.borrador = borrador;
            this.revision = revision;
            this.aprobada = aprobada;
            this.rechazada = rechazada;
            this.eliminada = eliminada;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int total = borrador + revision + aprobada + rechazada + eliminada;
            int diametro = Math.min(getHeight() - 20, 110);
            int x = 20;
            int y = (getHeight() - diametro) / 2;

            if (total == 0) {
                g.setColor(new Color(224, 224, 224));
                g.fillOval(x, y, diametro, diametro);
            } else {
                int inicio = 90;
                int[] valores = {borrador, revision, aprobada, rechazada, eliminada};
                Color[] colores = {
                        COLOR_BORRADOR, COLOR_REVISION, COLOR_APROBADA, COLOR_RECHAZADA, COLOR_ELIMINADA
                };
                int anguloAcumulado = 0;
                for (int i = 0; i < valores.length; i++) {
                    int angulo = i == valores.length - 1
                            ? 360 - anguloAcumulado
                            : (int) Math.round(360.0 * valores[i] / total);
                    g.setColor(colores[i]);
                    g.fillArc(x, y, diametro, diametro, inicio + anguloAcumulado, angulo);
                    anguloAcumulado += angulo;
                }
            }

            g.setColor(Color.WHITE);
            g.fillOval(x + diametro / 3, y + diametro / 3, diametro / 3, diametro / 3);
            g.dispose();
        }
    }
}
