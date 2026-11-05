package com.bancopreguntas.desktop;

import com.bancopreguntas.desktop.ApiClient.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BancoPreguntasApp {
    private static final Color NAVY = new Color(20, 43, 79);
    private static final Color BLUE = new Color(46, 91, 156);
    private static final Color TEXT = new Color(37, 51, 74);
    private static final Color MUTED = new Color(115, 128, 149);
    private static final Color BACKGROUND = new Color(246, 248, 252);
    private static final int PAGE_SIZE = 8;

    private final ApiClient api = new ApiClient();
    private final JFrame frame = new JFrame("Banco de Preguntas Saber Pro");
    private final JLabel statusLabel = new JLabel("Listo");
    private JsonNode currentUser;
    private QuestionTablePanel authorQuestions;
    private QuestionTablePanel adminQuestions;
    private QuestionTablePanel reviewerQuestions;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BancoPreguntasApp().showLogin());
    }

    private BancoPreguntasApp() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(980, 660));
        frame.setSize(1120, 760);
        frame.setLocationRelativeTo(null);
    }

    private void showLogin() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        JPanel brand = new JPanel(new BorderLayout(10, 10));
        brand.setBackground(NAVY);
        brand.setBorder(BorderFactory.createEmptyBorder(60, 48, 48, 48));
        JLabel brandTitle = new JLabel("<html><div style='font-size:29px'>Banco de<br>Preguntas</div></html>");
        brandTitle.setForeground(Color.WHITE);
        brandTitle.setFont(brandTitle.getFont().deriveFont(Font.BOLD, 30f));
        JLabel brandText = new JLabel("<html><div style='width:290px;color:#d4deed;font-size:14px;line-height:1.6'>"
                + "Plataforma académica para crear, organizar y revisar preguntas Saber Pro."
                + "</div></html>");
        brandText.setForeground(Color.WHITE);
        brand.add(brandTitle, BorderLayout.NORTH);
        brand.add(brandText, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 12));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(85, 68, 80, 68));
        JLabel title = heading("Iniciar sesión");
        JLabel subtitle = new JLabel("Ingresa con la cuenta del servicio de usuarios.");
        subtitle.setForeground(MUTED);
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JButton login = button("Conectar");
        JLabel feedback = new JLabel(" ");
        feedback.setForeground(new Color(175, 55, 55));
        form.add(title);
        form.add(subtitle);
        form.add(field("Usuario", username));
        form.add(field("Contraseña", password));
        form.add(login);
        form.add(feedback);

        login.addActionListener(event -> {
            login.setEnabled(false);
            feedback.setForeground(MUTED);
            feedback.setText("Conectando con el servicio de usuarios...");
            String user = username.getText().trim();
            String pass = new String(password.getPassword());
            async("Autenticando...", () -> api.login(user, pass), response -> {
                login.setEnabled(true);
                if (!response.path("authenticated").asBoolean(false) || response.path("user").isMissingNode()) {
                    feedback.setForeground(new Color(175, 55, 55));
                    feedback.setText(response.path("message").asText("Credenciales incorrectas"));
                    return;
                }
                currentUser = response.path("user");
                showWorkspace();
            }, error -> {
                login.setEnabled(true);
                feedback.setForeground(new Color(175, 55, 55));
                feedback.setText(error);
            });
        });
        password.addActionListener(event -> login.doClick());

        root.add(brand);
        root.add(form);
        root.setBackground(Color.WHITE);
        frame.setContentPane(root);
        frame.setVisible(true);
    }

    private void showWorkspace() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        JLabel appName = new JLabel("Banco de Preguntas  /  Saber Pro");
        appName.setForeground(NAVY);
        appName.setFont(appName.getFont().deriveFont(Font.BOLD, 17f));
        String person = currentUser.path("fullName").asText(currentUser.path("username").asText("Usuario"));
        JLabel profile = new JLabel(person + "   ·   " + roleText(currentUser));
        profile.setForeground(MUTED);
        JButton logout = button("Cerrar sesión");
        logout.addActionListener(event -> showLogin());
        JPanel identity = new JPanel(new BorderLayout(18, 0));
        identity.setOpaque(false);
        identity.add(profile, BorderLayout.CENTER);
        identity.add(logout, BorderLayout.EAST);
        header.add(appName, BorderLayout.WEST);
        header.add(identity, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        String roles = roles(currentUser);
        long userId = currentUser.path("id").asLong();
        if (hasRole(roles, "DOCENTE", "AUTOR")) {
            authorQuestions = new QuestionTablePanel();
            tabs.addTab("Mis preguntas", buildAuthorPanel());
            refreshAuthorQuestions();
        }
        if (hasRole(roles, "ADMIN")) {
            adminQuestions = new QuestionTablePanel();
            tabs.addTab("Asignar revisores", buildAdminPanel());
            refreshAdminQuestions();
        }
        if (hasRole(roles, "REVISOR")) {
            reviewerQuestions = new QuestionTablePanel();
            tabs.addTab("Mis revisiones", buildReviewerPanel());
            refreshReviewerQuestions();
        }
        tabs.addTab("Notificaciones", buildNotificationsPanel());

        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        statusLabel.setForeground(MUTED);
        root.add(header, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(statusLabel, BorderLayout.SOUTH);
        frame.setContentPane(root);
        frame.setTitle("Banco de Preguntas - " + person);
        frame.setVisible(true);
    }

    private JPanel buildAuthorPanel() {
        JPanel panel = sectionPanel("Preguntas creadas",
                "Consulta, filtra y administra el estado de tus preguntas.");
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton create = button("Crear pregunta");
        JButton edit = secondaryButton("Editar seleccionada");
        JButton submit = secondaryButton("Enviar a revisión");
        JButton results = secondaryButton("Ver evaluaciones");
        JButton refresh = secondaryButton("Actualizar");
        actions.add(create);
        actions.add(edit);
        actions.add(submit);
        actions.add(results);
        actions.add(refresh);
        create.addActionListener(event -> openEditor(null));
        edit.addActionListener(event -> {
            JsonNode selected = authorQuestions.selectedQuestion();
            if (selected == null) {
                showMessage("Selecciona una pregunta para editar.");
            } else if (!List.of("DRAFT", "REJECTED").contains(selected.path("status").asText())) {
                showMessage("Solo se pueden editar preguntas en borrador o rechazadas.");
            } else {
                openEditor(selected);
            }
        });
        submit.addActionListener(event -> submitSelectedQuestion());
        results.addActionListener(event -> showSelectedReviewResults());
        refresh.addActionListener(event -> refreshAuthorQuestions());
        panel.add(actions, BorderLayout.SOUTH);
        panel.add(authorQuestions, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAdminPanel() {
        JPanel panel = sectionPanel("Preguntas pendientes de revisión",
                "Asigna de uno a tres revisores ingresando sus identificadores separados por coma.");
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton assign = button("Asignar revisores");
        JButton refresh = secondaryButton("Actualizar");
        actions.add(assign);
        actions.add(refresh);
        assign.addActionListener(event -> assignReviewers());
        refresh.addActionListener(event -> refreshAdminQuestions());
        panel.add(actions, BorderLayout.SOUTH);
        panel.add(adminQuestions, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReviewerPanel() {
        JPanel panel = sectionPanel("Preguntas asignadas",
                "Consulta tus asignaciones activas y registra la evaluación de cada pregunta.");
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton evaluate = button("Evaluar seleccionada");
        JButton refresh = secondaryButton("Actualizar");
        actions.add(evaluate);
        actions.add(refresh);
        evaluate.addActionListener(event -> evaluateSelectedQuestion());
        refresh.addActionListener(event -> refreshReviewerQuestions());
        panel.add(actions, BorderLayout.SOUTH);
        panel.add(reviewerQuestions, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildNotificationsPanel() {
        JPanel panel = sectionPanel("Notificaciones",
                "Notificaciones disponibles para la cuenta autenticada.");
        DefaultTableModel model = readOnlyModel("Tipo", "Asunto", "Mensaje", "Estado", "Creada");
        JTable table = styledTable(model);
        JButton refresh = secondaryButton("Actualizar");
        refresh.addActionListener(event -> loadNotifications(model));
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.add(refresh);
        panel.add(actions, BorderLayout.SOUTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        loadNotifications(model);
        return panel;
    }

    private void loadNotifications(DefaultTableModel model) {
        long userId = currentUser.path("id").asLong();
        async("Consultando notificaciones...", () -> api.notifications(userId), response -> {
            model.setRowCount(0);
            for (JsonNode notification : response) {
                model.addRow(new Object[]{
                        notification.path("type").asText(""),
                        notification.path("subject").asText(""),
                        notification.path("message").asText(""),
                        notification.path("status").asText(""),
                        notification.path("createdAt").asText("")
                });
            }
            statusLabel.setText(response.size() + " notificación(es)");
        });
    }

    private void refreshAuthorQuestions() {
        if (authorQuestions == null) {
            return;
        }
        long userId = currentUser.path("id").asLong();
        async("Cargando preguntas...", () -> filterByAuthor(api.allQuestions(), userId), questions -> {
            authorQuestions.setQuestions(questions);
            statusLabel.setText(questions.size() + " pregunta(s) del autor");
        });
    }

    private void refreshAdminQuestions() {
        if (adminQuestions == null) {
            return;
        }
        async("Buscando preguntas pendientes...", () -> {
            List<JsonNode> pending = new ArrayList<>();
            for (JsonNode question : api.allQuestions()) {
                String state = question.path("status").asText();
                if ("PENDING_REVIEW".equals(state) || "IN_REVIEW".equals(state)) {
                    pending.add(question);
                }
            }
            return pending;
        }, questions -> {
            adminQuestions.setQuestions(questions);
            statusLabel.setText(questions.size() + " pregunta(s) pendiente(s)");
        });
    }

    private void refreshReviewerQuestions() {
        if (reviewerQuestions == null) {
            return;
        }
        long reviewerId = currentUser.path("id").asLong();
        async("Buscando asignaciones...", () -> {
            List<JsonNode> assigned = new ArrayList<>();
            for (JsonNode assignment : api.assignmentsForReviewer(reviewerId)) {
                if ("CONFIRMED".equals(assignment.path("status").asText())) {
                    assigned.add(api.question(assignment.path("questionId").asLong()));
                }
            }
            return assigned;
        }, questions -> {
            reviewerQuestions.setQuestions(questions);
            statusLabel.setText(questions.size() + " revisión(es) asignada(s)");
        });
    }

    private static List<JsonNode> filterByAuthor(JsonNode allQuestions, long authorId) {
        List<JsonNode> owned = new ArrayList<>();
        for (JsonNode question : allQuestions) {
            if (question.path("authorId").asLong(-1) == authorId) {
                owned.add(question);
            }
        }
        return owned;
    }

    private void openEditor(JsonNode original) {
        QuestionEditorDialog editor = new QuestionEditorDialog(frame, currentUser.path("id").asLong(), original);
        editor.setVisible(true);
        if (editor.payload() == null) {
            return;
        }
        Long id = original == null ? null : original.path("id").asLong();
        async(id == null ? "Creando pregunta..." : "Guardando cambios...",
                () -> api.saveQuestion(id, editor.payload()),
                saved -> {
                    showMessage(id == null ? "Pregunta creada correctamente." : "Pregunta actualizada correctamente.");
                    refreshAuthorQuestions();
                });
    }

    private void submitSelectedQuestion() {
        JsonNode question = authorQuestions.selectedQuestion();
        if (question == null) {
            showMessage("Selecciona una pregunta para enviarla a revisión.");
            return;
        }
        if (!"DRAFT".equals(question.path("status").asText())) {
            showMessage("Solo se pueden enviar preguntas en borrador.");
            return;
        }
        long id = question.path("id").asLong();
        long authorId = currentUser.path("id").asLong();
        async("Enviando pregunta...", () -> {
            JsonNode accepted = api.submitQuestion(id, authorId);
            String operationId = accepted.path("operationId").asText("");
            if (!operationId.isBlank()) {
                for (int attempt = 0; attempt < 8; attempt++) {
                    JsonNode operation = api.operation(operationId);
                    String state = operation.path("status").asText("");
                    if ("COMPLETED".equals(state) || "FAILED".equals(state)) {
                        return operation;
                    }
                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new ApiException("Se interrumpió la consulta del estado de la operación.", exception);
                    }
                }
                return accepted;
            }
            return accepted;
        }, operation -> {
            String state = operation.path("status").asText("EN PROCESO");
            String detail = operation.path("message").asText("");
            showMessage("Resultado del envío: " + state + (detail.isBlank() ? "" : "\n" + detail));
            refreshAuthorQuestions();
        });
    }

    private void showSelectedReviewResults() {
        JsonNode question = authorQuestions.selectedQuestion();
        if (question == null) {
            showMessage("Selecciona una pregunta para consultar sus evaluaciones.");
            return;
        }
        long questionId = question.path("id").asLong();
        async("Consultando evaluaciones...", () -> api.reviewResults(questionId), results -> {
            if (results.isEmpty()) {
                showMessage("Esta pregunta todavía no tiene evaluaciones.");
                return;
            }
            List<String> details = new ArrayList<>();
            for (JsonNode review : results) {
                details.add(review.path("result").asText("SIN RESULTADO")
                        + " — Revisor " + review.path("reviewerId").asText("")
                        + "\n" + review.path("comments").asText("Sin observaciones"));
            }
            showMessage(String.join("\n\n", details));
        });
    }

    private void assignReviewers() {
        JsonNode question = adminQuestions.selectedQuestion();
        if (question == null) {
            showMessage("Selecciona una pregunta pendiente.");
            return;
        }
        String input = JOptionPane.showInputDialog(frame,
                "Identificadores de 1 a 3 revisores (separados por coma):",
                "Asignar revisores", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.isBlank()) {
            return;
        }
        List<Long> reviewerIds;
        try {
            reviewerIds = java.util.Arrays.stream(input.trim().split("[,;\\s]+"))
                    .filter(value -> !value.isBlank())
                    .map(Long::parseLong)
                    .distinct()
                    .toList();
        } catch (NumberFormatException exception) {
            showMessage("Los identificadores deben ser números.");
            return;
        }
        if (reviewerIds.isEmpty() || reviewerIds.size() > 3) {
            showMessage("Debes indicar entre uno y tres revisores distintos.");
            return;
        }
        long questionId = question.path("id").asLong();
        long adminId = currentUser.path("id").asLong();
        async("Asignando revisores...", () -> {
            List<String> results = new ArrayList<>();
            for (long reviewerId : reviewerIds) {
                try {
                    JsonNode assigned = api.assignReviewer(questionId, reviewerId, adminId);
                    results.add("Revisor " + reviewerId + ": asignado (" + assigned.path("status").asText() + ")");
                } catch (ApiException exception) {
                    results.add("Revisor " + reviewerId + ": " + exception.getMessage());
                }
            }
            return results;
        }, results -> {
            showMessage(String.join("\n", results));
            refreshAdminQuestions();
        });
    }

    private void evaluateSelectedQuestion() {
        JsonNode question = reviewerQuestions.selectedQuestion();
        if (question == null) {
            showMessage("Selecciona una pregunta asignada.");
            return;
        }
        Object[] options = {"Aprobar", "Rechazar", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(frame, questionSummary(question),
                "Evaluar pregunta " + question.path("id").asText(), JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice < 0 || choice == 2) {
            return;
        }
        String result = choice == 0 ? "APPROVED" : "REJECTED";
        String comments = JOptionPane.showInputDialog(frame,
                result.equals("REJECTED") ? "Motivo de rechazo (obligatorio):" : "Observaciones (opcional):",
                result.equals("REJECTED") ? JOptionPane.QUESTION_MESSAGE : JOptionPane.PLAIN_MESSAGE);
        if (comments == null) {
            return;
        }
        if (result.equals("REJECTED") && comments.isBlank()) {
            showMessage("El backend exige un motivo para rechazar una pregunta.");
            return;
        }
        long questionId = question.path("id").asLong();
        long reviewerId = currentUser.path("id").asLong();
        async("Enviando evaluación...", () -> api.submitReview(questionId, reviewerId, result, comments),
                response -> {
                    showMessage("Evaluación registrada: " + response.path("questionStatus").asText(result));
                    refreshReviewerQuestions();
                });
    }

    private <T> void async(String loading, Supplier<T> action, Consumer<T> success) {
        async(loading, action, success, error -> showMessage(error));
    }

    private <T> void async(String loading, Supplier<T> action, Consumer<T> success, Consumer<String> failure) {
        statusLabel.setText(loading);
        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() {
                return action.get();
            }

            @Override
            protected void done() {
                try {
                    success.accept(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    failure.accept("La operación fue interrumpida.");
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    failure.accept(cause == null || cause.getMessage() == null
                            ? "Ocurrió un error inesperado al comunicarse con el backend."
                            : cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private static JPanel sectionPanel(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 18, 24));
        JPanel heading = new JPanel(new GridLayout(0, 1, 0, 5));
        heading.setOpaque(false);
        heading.add(heading(title));
        JLabel hint = new JLabel(subtitle);
        hint.setForeground(MUTED);
        heading.add(hint);
        panel.add(heading, BorderLayout.NORTH);
        return panel;
    }

    private static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
        return label;
    }

    private static JPanel field(String label, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel caption = new JLabel(label);
        caption.setForeground(TEXT);
        panel.add(caption, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private static JButton button(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(BLUE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 15, 9, 15));
        return button;
    }

    private static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(NAVY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 225, 235)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return button;
    }

    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(34);
        table.setSelectionBackground(new Color(226, 236, 250));
        table.setSelectionForeground(TEXT);
        table.setGridColor(new Color(235, 239, 245));
        table.setFillsViewportHeight(true);
        return table;
    }

    private static String roles(JsonNode user) {
        StringBuilder roles = new StringBuilder();
        for (JsonNode role : user.path("roles")) {
            roles.append(role.asText().toUpperCase(Locale.ROOT)).append(' ');
        }
        return roles.toString();
    }

    private static boolean hasRole(String roles, String... candidates) {
        for (String candidate : candidates) {
            if (roles.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static String roleText(JsonNode user) {
        String values = roles(user).trim().replace(' ', ',');
        return values.isBlank() ? "Usuario" : values;
    }

    private static String questionSummary(JsonNode question) {
        return question.path("statement").asText("Pregunta sin enunciado") + "\n\n"
                + "Contexto: " + question.path("context").asText("") + "\n"
                + "Competencia: " + question.path("competency").asText("")
                + "  ·  Tema: " + question.path("topic").asText("") + "\n"
                + "Bibliografía: " + question.path("bibliography").asText("")
                + "  ·  Dificultad: " + difficultyName(question.path("difficulty").asText(""));
    }

    private static String difficultyName(String value) {
        return switch (value) {
            case "BASIC" -> "Básica";
            case "HIGH" -> "Alta";
            case "MEDIUM" -> "Media";
            default -> value;
        };
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Banco de Preguntas", JOptionPane.INFORMATION_MESSAGE);
    }

    private final class QuestionTablePanel extends JPanel {
        private final JTextField search = new JTextField();
        private final JComboBox<String> status = new JComboBox<>(new String[]{
                "Todos los estados", "DRAFT", "PENDING_REVIEW", "IN_REVIEW", "APPROVED", "REJECTED"
        });
        private final DefaultTableModel model = readOnlyModel(
                "ID", "Estado", "Dificultad", "Competencia", "Tema", "Pregunta");
        private final JTable table = styledTable(model);
        private List<JsonNode> questions = List.of();
        private List<JsonNode> filtered = List.of();
        private int page;

        private QuestionTablePanel() {
            super(new BorderLayout(0, 10));
            setOpaque(false);
            JPanel controls = new JPanel(new BorderLayout(10, 0));
            controls.setOpaque(false);
            search.setToolTipText("Buscar por pregunta, competencia o tema");
            status.setPreferredSize(new Dimension(190, 32));
            controls.add(search, BorderLayout.CENTER);
            controls.add(status, BorderLayout.EAST);
            JPanel pager = new JPanel(new BorderLayout());
            pager.setOpaque(false);
            JLabel pageLabel = new JLabel(" ");
            pageLabel.setForeground(MUTED);
            JButton previous = secondaryButton("Anterior");
            JButton next = secondaryButton("Siguiente");
            JPanel navigation = new JPanel();
            navigation.setOpaque(false);
            navigation.add(previous);
            navigation.add(next);
            pager.add(pageLabel, BorderLayout.WEST);
            pager.add(navigation, BorderLayout.EAST);
            JPanel content = new JPanel(new BorderLayout(0, 10));
            content.setOpaque(false);
            content.add(controls, BorderLayout.NORTH);
            content.add(new JScrollPane(table), BorderLayout.CENTER);
            content.add(pager, BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
            search.addActionListener(event -> applyFilters(pageLabel));
            status.addActionListener(event -> applyFilters(pageLabel));
            previous.addActionListener(event -> {
                page = Math.max(0, page - 1);
                renderPage(pageLabel);
            });
            next.addActionListener(event -> {
                int lastPage = Math.max(0, (filtered.size() - 1) / PAGE_SIZE);
                page = Math.min(lastPage, page + 1);
                renderPage(pageLabel);
            });
            table.getColumnModel().getColumn(1).setCellRenderer(new StatusRenderer());
        }

        private void setQuestions(List<JsonNode> values) {
            questions = List.copyOf(values);
            applyFilters(null);
        }

        private void setStatusFilter(String value) {
            status.setSelectedItem(value);
        }

        private JsonNode selectedQuestion() {
            int row = table.getSelectedRow();
            if (row < 0) {
                return null;
            }
            int modelRow = table.convertRowIndexToModel(row);
            long id = ((Number) model.getValueAt(modelRow, 0)).longValue();
            return questions.stream().filter(question -> question.path("id").asLong(-1) == id)
                    .findFirst().orElse(null);
        }

        private void applyFilters(JLabel label) {
            String query = search.getText().trim().toLowerCase(Locale.ROOT);
            Object selected = status.getSelectedItem();
            String selectedStatus = selected == null || "Todos los estados".equals(selected)
                    ? "" : selected.toString();
            filtered = questions.stream().filter(question -> {
                boolean matchesStatus = selectedStatus.isBlank()
                        || selectedStatus.equals(question.path("status").asText());
                String searchable = question.path("statement").asText("") + " "
                        + question.path("competency").asText("") + " "
                        + question.path("topic").asText("");
                return matchesStatus && searchable.toLowerCase(Locale.ROOT).contains(query);
            }).toList();
            page = 0;
            if (label != null) {
                renderPage(label);
            } else {
                renderRows();
            }
        }

        private void renderPage(JLabel label) {
            renderRows();
            int count = Math.max(1, (filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
            label.setText("Página " + (page + 1) + " de " + count + "  ·  " + filtered.size() + " resultado(s)");
        }

        private void renderRows() {
            model.setRowCount(0);
            int start = page * PAGE_SIZE;
            int end = Math.min(filtered.size(), start + PAGE_SIZE);
            for (int index = start; index < end; index++) {
                JsonNode question = filtered.get(index);
                model.addRow(new Object[]{
                        question.path("id").asLong(),
                        question.path("status").asText("UNAVAILABLE"),
                        difficultyName(question.path("difficulty").asText("")),
                        question.path("competency").asText(""),
                        question.path("topic").asText(""),
                        question.path("statement").asText("")
                });
            }
        }
    }

    private static final class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);
            if (!selected) {
                String state = Objects.toString(value, "");
                switch (state) {
                    case "DRAFT" -> label.setBackground(new Color(235, 239, 245));
                    case "PENDING_REVIEW" -> label.setBackground(new Color(255, 242, 214));
                    case "IN_REVIEW" -> label.setBackground(new Color(226, 237, 255));
                    case "APPROVED" -> label.setBackground(new Color(220, 244, 231));
                    case "REJECTED" -> label.setBackground(new Color(252, 226, 226));
                    default -> label.setBackground(Color.WHITE);
                }
                label.setForeground(TEXT);
            }
            return label;
        }
    }

    private static final class QuestionEditorDialog extends JDialog {
        private final ObjectNode result;
        private final JTextArea context = textArea();
        private final JTextArea statement = textArea();
        private final JTextField[] options = {
                new JTextField(), new JTextField(), new JTextField(), new JTextField()
        };
        private final JComboBox<String> correctOption = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        private final JTextArea justification = textArea();
        private final JTextField bibliography = new JTextField();
        private final JTextField competency = new JTextField();
        private final JTextField topic = new JTextField();
        private final JTextField subtopic = new JTextField();
        private final JComboBox<String> difficulty = new JComboBox<>(new String[]{"Básica", "Media", "Alta"});
        private ObjectNode payload;

        private QuestionEditorDialog(JFrame owner, long authorId, JsonNode original) {
            super(owner, original == null ? "Crear pregunta" : "Editar pregunta", true);
            result = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode().put("authorId", authorId);
            JPanel fields = new JPanel(new GridLayout(0, 2, 10, 8));
            fields.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
            fields.add(label("Aviso"));
            fields.add(label("La API actual no persiste bibliografía ni dificultad."));
            fields.add(label("Contexto"));
            fields.add(new JScrollPane(context));
            fields.add(label("Pregunta directa"));
            fields.add(new JScrollPane(statement));
            for (int index = 0; index < options.length; index++) {
                fields.add(label("Opción " + (char) ('A' + index)));
                fields.add(options[index]);
            }
            fields.add(label("Respuesta correcta"));
            fields.add(correctOption);
            fields.add(label("Justificación"));
            fields.add(new JScrollPane(justification));
            fields.add(label("Bibliografía"));
            fields.add(bibliography);
            fields.add(label("Competencia"));
            fields.add(competency);
            fields.add(label("Tema"));
            fields.add(topic);
            fields.add(label("Subtema"));
            fields.add(subtopic);
            fields.add(label("Nivel de dificultad"));
            fields.add(difficulty);
            if (original != null) {
                fill(original);
            }
            JButton save = button("Guardar");
            JButton cancel = secondaryButton("Cancelar");
            save.addActionListener(event -> {
                if (context.getText().isBlank() || statement.getText().isBlank()
                        || justification.getText().isBlank() || competency.getText().isBlank()
                        || topic.getText().isBlank() || subtopic.getText().isBlank()
                        || bibliography.getText().isBlank()) {
                    JOptionPane.showMessageDialog(this,
                            "Completa contexto, pregunta, justificación, bibliografía, competencia, tema y subtema.");
                    return;
                }
                for (int index = 0; index < options.length; index++) {
                    if (options[index].getText().isBlank()) {
                        JOptionPane.showMessageDialog(this, "Completa las cuatro opciones.");
                        return;
                    }
                }
                int confirmation = JOptionPane.showConfirmDialog(this,
                        "El backend de referencia no guarda bibliografía ni nivel de dificultad. "
                                + "Estos dos datos no estarán disponibles al volver a abrir la pregunta. ¿Continuar?",
                        "Campos no persistidos por el backend", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirmation != JOptionPane.YES_OPTION) {
                    return;
                }
                ArrayNode optionList = result.putArray("options");
                for (int index = 0; index < options.length; index++) {
                    ObjectNode option = optionList.addObject();
                    option.put("label", String.valueOf((char) ('A' + index)));
                    option.put("text", options[index].getText().trim());
                }
                result.put("context", context.getText().trim());
                result.put("statement", statement.getText().trim());
                result.put("correctOption", Objects.toString(correctOption.getSelectedItem(), "A"));
                result.put("justification", justification.getText().trim());
                result.put("bibliography", bibliography.getText().trim());
                result.put("competency", competency.getText().trim());
                result.put("topic", topic.getText().trim());
                result.put("subtopic", subtopic.getText().trim());
                result.put("difficulty", difficultyCode(Objects.toString(difficulty.getSelectedItem(), "Media")));
                payload = result;
                dispose();
            });
            cancel.addActionListener(event -> dispose());
            JPanel actions = new JPanel();
            actions.add(cancel);
            actions.add(save);
            setLayout(new BorderLayout());
            add(new JScrollPane(fields), BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);
            setSize(700, 720);
            setLocationRelativeTo(owner);
        }

        private ObjectNode payload() {
            return payload;
        }

        private void fill(JsonNode question) {
            context.setText(question.path("context").asText(""));
            statement.setText(question.path("statement").asText(""));
            justification.setText(question.path("justification").asText(""));
            bibliography.setText(question.path("bibliography").asText(""));
            competency.setText(question.path("competency").asText(""));
            topic.setText(question.path("topic").asText(""));
            subtopic.setText(question.path("subtopic").asText(""));
            correctOption.setSelectedItem(question.path("correctOption").asText("A"));
            difficulty.setSelectedItem(difficultyLabel(question.path("difficulty").asText("MEDIUM")));
            JsonNode existingOptions = question.path("options");
            for (JsonNode option : existingOptions) {
                String label = option.path("label").asText("");
                if (label.length() == 1 && label.charAt(0) >= 'A' && label.charAt(0) <= 'D') {
                    options[label.charAt(0) - 'A'].setText(option.path("text").asText(""));
                }
            }
        }

        private static JLabel label(String text) {
            JLabel label = new JLabel(text);
            label.setForeground(TEXT);
            return label;
        }

        private static JTextArea textArea() {
            JTextArea area = new JTextArea(3, 25);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            return area;
        }

        private static String difficultyCode(String label) {
            return switch (label) {
                case "Básica" -> "BASIC";
                case "Alta" -> "HIGH";
                default -> "MEDIUM";
            };
        }

        private static String difficultyLabel(String difficulty) {
            return switch (difficulty) {
                case "BASIC" -> "Básica";
                case "HIGH" -> "Alta";
                default -> "Media";
            };
        }
    }
}
