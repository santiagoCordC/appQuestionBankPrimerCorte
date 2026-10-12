package pkgPresentation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import pkgController.clsQuestionController;
import pkgController.clsResult;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgLifecycle.clsQuestionState;
import pkgServices.pkgMicrokernel.pkgKernel.IQuestionPlugin;
import pkgDomain.pkgRepository.clsPage;
import pkgDomain.pkgRepository.clsSearchCriteria;

/**
 * Vista "Mis preguntas" del autor:
 * <ul>
 * <li>HU03: listado con filtros y paginación.</li>
 * <li>HU01: botones para crear y ver/editar preguntas.</li>
 * <li>HU02: enviar a revisión; el estado se muestra con colores.</li>
 * </ul>
 */
public class pnlMyQuestions extends JPanel {

    private static final String[] COLUMNS = {"Pregunta", "Tipo", "Competencia", "Tema", "Dificultad", "Estado", "Última modificación"};
    private static final int STATE_COLUMN = 5;

    private final clsQuestionController attController;

    private final JTextField jtfText = new JTextField(16);
    private final JComboBox<Object> jcbState = new JComboBox<>();
    private final JComboBox<Object> jcbType = new JComboBox<>();
    private final JComboBox<String> jcbCompetence = new JComboBox<>();
    private final JComboBox<String> jcbDifficulty = new JComboBox<>();

    private final DefaultTableModel attModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int prmRow, int prmColumn) {
            return false;
        }
    };
    private final JTable attTable = new JTable(attModel);
    private final List<clsQuestion> attRows = new ArrayList<>();

    private final JButton cmdPrevious = clsUiKit.opButton("« Anterior");
    private final JButton cmdNext = clsUiKit.opButton("Siguiente »");
    private final JLabel jlPage = new JLabel();
    private int attPage = 1;

    public pnlMyQuestions(clsQuestionController prmController) {
        super(new BorderLayout(6, 6));
        attController = prmController;
        setBackground(clsUiKit.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        opBuild();
        opRefresh();
    }

    private void opBuild() {
        // ----- Filtros -----
        jcbState.addItem(clsUiKit.ALL);
        attController.opGetStates().forEach(jcbState::addItem);
        jcbType.addItem(clsUiKit.ALL);
        for (IQuestionPlugin varPlugin : attController.opGetTypes()) {
            jcbType.addItem(varPlugin);
        }
        clsUiKit.opUsePluginNames(jcbType);
        jcbCompetence.addItem(clsUiKit.ALL);
        attController.opGetCompetences().forEach(jcbCompetence::addItem);
        jcbDifficulty.addItem(clsUiKit.ALL);
        attController.opGetDifficulties().forEach(jcbDifficulty::addItem);

        JButton cmdSearch = clsUiKit.opButton("Buscar");
        JButton cmdClear = clsUiKit.opButton("Limpiar");
        cmdSearch.addActionListener(varE -> opSearch());
        jtfText.addActionListener(varE -> opSearch());
        cmdClear.addActionListener(varE -> {
            jtfText.setText("");
            jcbState.setSelectedIndex(0);
            jcbType.setSelectedIndex(0);
            jcbCompetence.setSelectedIndex(0);
            jcbDifficulty.setSelectedIndex(0);
            opSearch();
        });
        JPanel varFilters = new JPanel(new GridLayout(2, 1));
        varFilters.setBackground(clsUiKit.BACKGROUND);
        varFilters.setBorder(BorderFactory.createTitledBorder("Buscar en mis preguntas"));
        varFilters.add(clsUiKit.opFlow(FlowLayout.LEFT, new JLabel("Texto:"), jtfText, new JLabel("Estado:"), jcbState,
                new JLabel("Dificultad:"), jcbDifficulty));
        varFilters.add(clsUiKit.opFlow(FlowLayout.LEFT, new JLabel("Tipo:"), jcbType, new JLabel("Competencia:"), jcbCompetence,
                cmdSearch, cmdClear));
        add(varFilters, BorderLayout.NORTH);

        // ----- Tabla -----
        attTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attTable.setRowHeight(26);
        attTable.getTableHeader().setReorderingAllowed(false);
        int[] varWidths = {340, 130, 160, 130, 70, 150, 140};
        for (int varI = 0; varI < varWidths.length; varI++) {
            attTable.getColumnModel().getColumn(varI).setPreferredWidth(varWidths[varI]);
        }
        clsUiKit.opUseStateColors(attTable, STATE_COLUMN);
        attTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent prmEvent) {
                if (prmEvent.getClickCount() == 2) {
                    opOpen();
                }
            }
        });
        add(new JScrollPane(attTable), BorderLayout.CENTER);

        // ----- Paginación, leyenda y acciones -----
        cmdPrevious.addActionListener(varE -> {
            attPage--;
            opRefresh();
        });
        cmdNext.addActionListener(varE -> {
            attPage++;
            opRefresh();
        });
        JPanel varLegend = clsUiKit.opFlow(FlowLayout.RIGHT, new JLabel("Estados:"));
        for (clsQuestionState varState : attController.opGetStates()) {
            varLegend.add(clsUiKit.opStateBadge(varState));
        }
        JPanel varPaging = new JPanel(new BorderLayout());
        varPaging.setBackground(clsUiKit.BACKGROUND);
        varPaging.add(clsUiKit.opFlow(FlowLayout.LEFT, cmdPrevious, jlPage, cmdNext), BorderLayout.WEST);
        varPaging.add(varLegend, BorderLayout.EAST);

        JButton cmdNew = clsUiKit.opButton("Nueva pregunta");
        JButton cmdOpen = clsUiKit.opButton("Ver / Editar");
        JButton cmdSubmit = clsUiKit.opButton("Enviar a revisión");
        cmdNew.addActionListener(varE -> opNew());
        cmdOpen.addActionListener(varE -> opOpen());
        cmdSubmit.addActionListener(varE -> opSubmit());

        JPanel varSouth = new JPanel(new GridLayout(2, 1));
        varSouth.add(varPaging);
        varSouth.add(clsUiKit.opFlow(FlowLayout.LEFT, cmdNew, cmdOpen, cmdSubmit));
        add(varSouth, BorderLayout.SOUTH);
    }

    private void opSearch() {
        attPage = 1;
        opRefresh();
    }

    /** Pide al controlador la página actual con los filtros y la pinta en la tabla. */
    public void opRefresh() {
        Object varState = jcbState.getSelectedItem();
        Object varType = jcbType.getSelectedItem();
        clsSearchCriteria varFilter = new clsSearchCriteria()
                .opText(jtfText.getText())
                .opState(varState instanceof clsQuestionState ? (clsQuestionState) varState : null)
                .opType(varType instanceof IQuestionPlugin ? ((IQuestionPlugin) varType).opGetCode() : "")
                .opCompetence(opValue(jcbCompetence))
                .opDifficulty(opValue(jcbDifficulty));
        clsResult<clsPage<clsQuestion>> varResult = attController.opListMine(varFilter, attPage);
        if (!varResult.opIsOk()) {
            clsUiKit.opError(this, varResult.opGetMessage());
            return;
        }
        clsPage<clsQuestion> varPage = varResult.opGetValue();
        attPage = varPage.opGetNumber();
        attRows.clear();
        attModel.setRowCount(0);
        for (clsQuestion varQ : varPage.opGetItems()) {
            attRows.add(varQ);
            attModel.addRow(new Object[]{clsUiKit.opShort(varQ.opGetStatement(), 90),
                attController.opGetTypeName(varQ.opGetType()), varQ.opGetCompetence(),
                varQ.opGetTopic(), varQ.opGetDifficulty(), varQ.opGetState(), varQ.opGetUpdatedAt()});
        }
        jlPage.setText("Página " + varPage.opGetNumber() + " de " + varPage.opGetTotalPages()
                + "  (" + varPage.opGetTotalItems() + " pregunta(s))");
        cmdPrevious.setEnabled(varPage.opHasPrevious());
        cmdNext.setEnabled(varPage.opHasNext());
    }

    private static String opValue(JComboBox<String> prmCombo) {
        Object varValue = prmCombo.getSelectedItem();
        return varValue == null || clsUiKit.ALL.equals(varValue) ? "" : varValue.toString();
    }

    private clsQuestion opSelected() {
        int varRow = attTable.getSelectedRow();
        if (varRow < 0) {
            clsUiKit.opError(this, "Seleccione una pregunta de la tabla.");
            return null;
        }
        return attRows.get(varRow);
    }

    private void opNew() {
        dlgQuestionEditor varEditor = new dlgQuestionEditor(SwingUtilities.getWindowAncestor(this), attController, null);
        varEditor.setVisible(true);
        if (varEditor.opWasSaved()) {
            opSearch();
        }
    }

    private void opOpen() {
        clsQuestion varQuestion = opSelected();
        if (varQuestion != null) {
            new dlgQuestionEditor(SwingUtilities.getWindowAncestor(this), attController, varQuestion).setVisible(true);
            opRefresh();
        }
    }

    private void opSubmit() {
        clsQuestion varQuestion = opSelected();
        if (varQuestion == null) {
            return;
        }
        if (clsUiKit.opConfirm(this, "¿Enviar la pregunta a revisión? Después ya no podrá editarla.\n\n"
                + clsUiKit.opShort(varQuestion.opGetStatement(), 120))) {
            clsUiKit.opShowResult(this, attController.opSubmitForReview(varQuestion.opGetId()));
            opRefresh();
        }
    }
}
