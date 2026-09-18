package com.bancopreguntas.ui;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.NivelDificultad;
import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.notificacion.ObservadorPreguntas;
import com.bancopreguntas.notificacion.SujetoPreguntas;
import com.bancopreguntas.service.FiltroPregunta;
import com.bancopreguntas.service.ResultadoPaginado;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * Vista (MVC) para la HU03: listar, paginar y filtrar las preguntas creadas
 * por el autor en sesion; y para la HU02: cambiar el estado de una pregunta
 * de "Borrador" a "Pendiente de revision" (los estados se ven con colores
 * gracias a {@link EstadoBadgeRenderer}).
 */
public class PanelListarPreguntas extends JPanel {

    private static final int TAMANO_PAGINA = 5;

    private final PreguntaController controller;
    private final SujetoPreguntas sujetoPreguntas;
    private final ObservadorPreguntas observadorPreguntas = this::actualizarDesdeObserver;

    private final JTextField txtBusqueda = new JTextField(12);
    private final JTextField txtTema = new JTextField(10);
    private final JTextField txtSubtema = new JTextField(10);
    private final JComboBox<String> cmbNivel = new JComboBox<>();
    private final JComboBox<String> cmbEstado = new JComboBox<>();

    private final ModeloTablaPreguntas modeloTabla = new ModeloTablaPreguntas();
    private final JTable tabla = new JTable(modeloTabla);

    private final JLabel lblPagina = new JLabel();
    private final JButton btnAnterior = new JButton("< Anterior");
    private final JButton btnSiguiente = new JButton("Siguiente >");
    private final JButton btnEnviarRevision = new JButton("Enviar a revisión");
    private final JButton btnEliminar = new JButton("Eliminar pregunta");
    private final JLabel lblMensaje = new JLabel(" ");
    private final PanelGraficoEstados panelGrafico = new PanelGraficoEstados();

    private int paginaActual = 1;

    public PanelListarPreguntas(PreguntaController controller, SujetoPreguntas sujetoPreguntas) {
        this.controller = controller;
        this.sujetoPreguntas = sujetoPreguntas;
        sujetoPreguntas.suscribir(observadorPreguntas);
        construirUI();
        cargar();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        cmbNivel.addItem("Todos");
        for (NivelDificultad n : NivelDificultad.values()) cmbNivel.addItem(n.getEtiqueta());

        cmbEstado.addItem("Todos");
        for (EstadoPregunta e : EstadoPregunta.values()) cmbEstado.addItem(e.getEtiqueta());

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Buscar:"));
        filtros.add(txtBusqueda);
        filtros.add(new JLabel("Tema:"));
        filtros.add(txtTema);
        filtros.add(new JLabel("Subtema:"));
        filtros.add(txtSubtema);
        filtros.add(new JLabel("Nivel:"));
        filtros.add(cmbNivel);
        filtros.add(new JLabel("Estado:"));
        filtros.add(cmbEstado);
        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.addActionListener(e -> { paginaActual = 1; cargar(); });
        filtros.add(btnFiltrar);

        JPanel superior = new JPanel(new BorderLayout(8, 8));
        superior.add(panelGrafico, BorderLayout.CENTER);
        superior.add(filtros, BorderLayout.SOUTH);
        add(superior, BorderLayout.NORTH);

        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(5).setCellRenderer(new EstadoBadgeRenderer());
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout());

        JPanel paginacion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAnterior.addActionListener(e -> { paginaActual--; cargar(); });
        btnSiguiente.addActionListener(e -> { paginaActual++; cargar(); });
        paginacion.add(btnAnterior);
        paginacion.add(lblPagina);
        paginacion.add(btnSiguiente);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnEnviarRevision.addActionListener(e -> enviarARevision());
        acciones.add(btnEnviarRevision);
        btnEliminar.addActionListener(e -> eliminarPregunta());
        acciones.add(btnEliminar);

        sur.add(paginacion, BorderLayout.NORTH);
        sur.add(acciones, BorderLayout.CENTER);
        lblMensaje.setForeground(new Color(198, 40, 40));
        sur.add(lblMensaje, BorderLayout.SOUTH);

        add(sur, BorderLayout.SOUTH);
    }

    public void cargar() {
        FiltroPregunta filtro = FiltroPregunta.vacio()
                .texto(vacioSiTodos(txtBusqueda.getText()))
                .tema(vacioSiTodos(txtTema.getText()))
                .subtema(vacioSiTodos(txtSubtema.getText()));

        if (cmbNivel.getSelectedIndex() > 0) {
            filtro.nivelDificultad(NivelDificultad.values()[cmbNivel.getSelectedIndex() - 1]);
        }
        if (cmbEstado.getSelectedIndex() > 0) {
            filtro.estado(EstadoPregunta.values()[cmbEstado.getSelectedIndex() - 1]);
        }

        ResultadoPaginado<Pregunta> resultado = controller.listarMisPreguntas(
                SesionActual.getUsuarioActual().getId(), filtro, paginaActual, TAMANO_PAGINA);
        panelGrafico.actualizar(controller.contarEstadosVisiblesPorAutor(
                SesionActual.getUsuarioActual().getId()));

        paginaActual = resultado.getPagina();
        modeloTabla.actualizar(resultado.getElementos());

        lblPagina.setText("Página " + resultado.getPagina() + " de " + resultado.getTotalPaginas()
                + "  (" + resultado.getTotalElementos() + " preguntas)");
        btnAnterior.setEnabled(resultado.hayPaginaAnterior());
        btnSiguiente.setEnabled(resultado.hayPaginaSiguiente());
        lblMensaje.setText(" ");
    }

    private String vacioSiTodos(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto;
    }

    private void enviarARevision() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            lblMensaje.setText("Seleccione una pregunta de la tabla.");
            return;
        }
        Pregunta seleccionada = modeloTabla.get(fila);
        try {
            controller.enviarARevision(seleccionada.getId());
            lblMensaje.setForeground(new Color(46, 125, 50));
            lblMensaje.setText("La pregunta ahora está \"Pendiente de revisión\".");
            cargar();
        } catch (IllegalStateException ex) {
            lblMensaje.setForeground(new Color(198, 40, 40));
            lblMensaje.setText(ex.getMessage());
        }
    }

    private void eliminarPregunta() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            lblMensaje.setText("Seleccione una pregunta de la tabla.");
            return;
        }
        Pregunta seleccionada = modeloTabla.get(fila);
        try {
            controller.eliminarPregunta(seleccionada.getId());
            lblMensaje.setForeground(new Color(46, 125, 50));
            lblMensaje.setText("La pregunta fue marcada como eliminada.");
            cargar();
        } catch (IllegalStateException ex) {
            lblMensaje.setForeground(new Color(198, 40, 40));
            lblMensaje.setText(ex.getMessage());
        }
    }

    private void actualizarDesdeObserver() {
        SwingUtilities.invokeLater(this::cargar);
    }

    public void cerrar() {
        sujetoPreguntas.desuscribir(observadorPreguntas);
    }

    /** Modelo de tabla (parte "Model" del MVC de Swing, interno a esta vista). */
    private static class ModeloTablaPreguntas extends AbstractTableModel {
        private final String[] columnas = {"Tema", "Subtema", "Competencia", "Nivel", "Pregunta", "Estado"};
        private List<Pregunta> datos = List.of();

        void actualizar(List<Pregunta> nuevosDatos) {
            this.datos = nuevosDatos;
            fireTableDataChanged();
        }

        Pregunta get(int fila) {
            return datos.get(fila);
        }

        @Override
        public int getRowCount() {
            return datos.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnas[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Pregunta p = datos.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> p.getTema();
                case 1 -> p.getSubtema();
                case 2 -> p.getCompetencia();
                case 3 -> p.getNivelDificultad();
                case 4 -> p.getPreguntaDirecta();
                case 5 -> p.getEstado();
                default -> "";
            };
        }
    }
}
