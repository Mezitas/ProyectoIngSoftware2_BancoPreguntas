package com.bancopreguntas.ui;

import com.bancopreguntas.domain.EstadoPregunta;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

/**
 * Renderer de celda de JTable que pinta el estado de la pregunta con su
 * color asociado (HU02: "los estados se deben visualizar con colores").
 */
public class EstadoBadgeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        if (value instanceof EstadoPregunta estado) {
            label.setText(estado.getEtiqueta());
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBackground(estado.getColor());
            label.setForeground(colorTextoLegible(estado.getColor()));
        }
        return label;
    }

    private Color colorTextoLegible(Color fondo) {
        double luminancia = (0.299 * fondo.getRed() + 0.587 * fondo.getGreen() + 0.114 * fondo.getBlue()) / 255;
        return luminancia > 0.6 ? Color.BLACK : Color.WHITE;
    }
}
