package pkgPresentation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import pkgController.clsQuestionController;
import pkgController.clsResult;
import pkgController.clsReviewController;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsUser;

/**
 * Vista del administrador (HU04): preguntas en "Pendiente de revisión" y
 * asignación de uno o varios revisores.
 */
public class pnlAssignReviewers extends JPanel {

    private static final String[] COLUMNS = {"Pregunta", "Autor", "Competencia", "Dificultad", "Estado", "Enviada el"};

    private final clsReviewController attReviews;
    private final clsQuestionController attQuestions;

    private final DefaultTableModel attModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int prmRow, int prmColumn) {
            return false;
        }
    };
    private final JTable attTable = new JTable(attModel);
    private final List<clsQuestion> attRows = new ArrayList<>();
    private final JLabel jlCount = new JLabel();

    public pnlAssignReviewers(clsReviewController prmReviews, clsQuestionController prmQuestions) {
        super(new BorderLayout(6, 6));
        attReviews = prmReviews;
        attQuestions = prmQuestions;
        setBackground(clsUiKit.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(clsUiKit.opFlow(FlowLayout.LEFT, new JLabel("Preguntas pendientes de revisión. Seleccione una y asígnele "
                + "al menos un revisor; cada revisor recibirá un correo."), jlCount), BorderLayout.NORTH);

        attTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attTable.setRowHeight(26);
        attTable.getTableHeader().setReorderingAllowed(false);
        int[] varWidths = {400, 90, 170, 70, 150, 140};
        for (int varI = 0; varI < varWidths.length; varI++) {
            attTable.getColumnModel().getColumn(varI).setPreferredWidth(varWidths[varI]);
        }
        clsUiKit.opUseStateColors(attTable, 4);
        add(new JScrollPane(attTable), BorderLayout.CENTER);

        JButton cmdView = clsUiKit.opButton("Ver pregunta");
        JButton cmdAssign = clsUiKit.opButton("Asignar revisores");
        JButton cmdRefresh = clsUiKit.opButton("Actualizar");
        cmdView.addActionListener(varE -> opView());
        cmdAssign.addActionListener(varE -> opAssign());
        cmdRefresh.addActionListener(varE -> opRefresh());
        add(clsUiKit.opFlow(FlowLayout.LEFT, cmdView, cmdAssign, cmdRefresh), BorderLayout.SOUTH);
        opRefresh();
    }

    public void opRefresh() {
        clsResult<List<clsQuestion>> varResult = attReviews.opListPendingReview();
        if (!varResult.opIsOk()) {
            clsUiKit.opError(this, varResult.opGetMessage());
            return;
        }
        attRows.clear();
        attModel.setRowCount(0);
        for (clsQuestion varQ : varResult.opGetValue()) {
            attRows.add(varQ);
            attModel.addRow(new Object[]{clsUiKit.opShort(varQ.opGetStatement(), 90), varQ.opGetAuthorName(),
                varQ.opGetCompetence(), varQ.opGetDifficulty(), varQ.opGetState(), varQ.opGetUpdatedAt()});
        }
        jlCount.setText("   (" + attRows.size() + " pendiente(s))");
    }

    private clsQuestion opSelected() {
        int varRow = attTable.getSelectedRow();
        if (varRow < 0) {
            clsUiKit.opError(this, "Seleccione una pregunta de la tabla.");
            return null;
        }
        return attRows.get(varRow);
    }

    private void opView() {
        clsQuestion varQuestion = opSelected();
        if (varQuestion != null) {
            new dlgQuestionEditor(SwingUtilities.getWindowAncestor(this), attQuestions, varQuestion).setVisible(true);
        }
    }

    private void opAssign() {
        clsQuestion varQuestion = opSelected();
        if (varQuestion == null) {
            return;
        }
        List<clsUser> varCandidates = attReviews.opGetReviewerCandidates(varQuestion);
        if (varCandidates.isEmpty()) {
            clsUiKit.opInfo(this, "No hay usuarios activos con rol Revisor.");
            return;
        }
        JList<clsUser> varList = new JList<>(varCandidates.toArray(new clsUser[0]));
        varList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        varList.setVisibleRowCount(Math.min(8, varCandidates.size()));
        JPanel varPanel = new JPanel(new BorderLayout(4, 4));
        varPanel.add(new JLabel("Seleccione uno o varios revisores (Ctrl + clic):"), BorderLayout.NORTH);
        varPanel.add(new JScrollPane(varList), BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, varPanel, "Asignar revisores", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        List<String> varIds = varList.getSelectedValuesList().stream().map(clsUser::opGetId).collect(Collectors.toList());
        clsUiKit.opShowResult(this, attReviews.opAssignReviewers(varQuestion.opGetId(), varIds));
        opRefresh();
    }
}
