package pkgPresentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import pkgController.clsQuestionController;
import pkgController.clsResult;
import pkgDomain.pkgEntities.clsQuestion;
import pkgServices.pkgMicrokernel.pkgKernel.IQuestionPlugin;

/**
 * Formulario de una pregunta (HU01). Sirve para crear, para editar (solo en
 * Borrador) y para consultar. Antes de guardar muestra todos los errores de
 * la validación estructural.
 */
public class dlgQuestionEditor extends JDialog {

    private final clsQuestionController attController;
    private final clsQuestion attQuestion;
    private final boolean attEditable;
    private boolean attSaved;

    private final JComboBox<IQuestionPlugin> jcbType = new JComboBox<>();
    private final JLabel jlTypeHelp = new JLabel();
    private final JComboBox<String> jcbCompetence = new JComboBox<>();
    private final JComboBox<String> jcbDifficulty = new JComboBox<>();
    private final JTextField jtfTopic = new JTextField();
    private final JTextField jtfSubtopic = new JTextField();
    private final JTextArea jtaContext = clsUiKit.opTextArea(5);
    private final JTextArea jtaStatement = clsUiKit.opTextArea(2);
    private final JTextField[] jtfOptions = {new JTextField(), new JTextField(), new JTextField(), new JTextField()};
    private final JRadioButton[] jrbRight = new JRadioButton[4];
    private final JTextArea jtaJustification = clsUiKit.opTextArea(3);
    private final JTextArea jtaBibliography = clsUiKit.opTextArea(2);
    private final JTextArea jtaErrors = clsUiKit.opTextArea(7);

    /** @param prmQuestion null para crear una pregunta nueva. */
    public dlgQuestionEditor(Window prmOwner, clsQuestionController prmController, clsQuestion prmQuestion) {
        super(prmOwner, ModalityType.APPLICATION_MODAL);
        attController = prmController;
        attQuestion = prmQuestion;
        attEditable = prmQuestion == null || prmController.opCanEdit(prmQuestion);
        setTitle(prmQuestion == null ? "Nueva pregunta" : (attEditable ? "Editar pregunta" : "Detalle de la pregunta"));
        opBuild();
        if (prmQuestion != null) {
            opLoad(prmQuestion);
        }
        opApplyMode();
        setSize(new Dimension(860, 800));
        setLocationRelativeTo(prmOwner);
    }

    public boolean opWasSaved() {
        return attSaved;
    }

    private void opBuild() {
        JPanel varForm = new JPanel(new GridBagLayout());
        varForm.setBackground(clsUiKit.BACKGROUND);
        varForm.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        GridBagConstraints varC = new GridBagConstraints();
        varC.insets = new Insets(4, 4, 4, 4);
        varC.fill = GridBagConstraints.HORIZONTAL;
        int varRow = 0;

        if (attQuestion != null) {
            JPanel varInfo = clsUiKit.opFlow(FlowLayout.LEFT, new JLabel("Estado:"),
                    clsUiKit.opStateBadge(attQuestion.opGetState()),
                    new JLabel("   Creada: " + attQuestion.opGetCreatedAt()
                            + "   Última modificación: " + attQuestion.opGetUpdatedAt()));
            opAdd(varForm, varC, varRow++, null, varInfo);
        }

        attController.opGetTypes().forEach(jcbType::addItem);
        clsUiKit.opUsePluginNames(jcbType);
        jcbType.addActionListener(varE -> opShowTypeHelp());
        opShowTypeHelp();
        jlTypeHelp.setForeground(clsUiKit.PRIMARY);
        JPanel varType = new JPanel(new BorderLayout(10, 0));
        varType.setBackground(clsUiKit.BACKGROUND);
        varType.add(jcbType, BorderLayout.WEST);
        varType.add(jlTypeHelp, BorderLayout.CENTER);
        opAdd(varForm, varC, varRow++, "Tipo de pregunta *", varType);

        jcbCompetence.addItem("");
        attController.opGetCompetences().forEach(jcbCompetence::addItem);
        jcbDifficulty.addItem("");
        attController.opGetDifficulties().forEach(jcbDifficulty::addItem);
        opAdd(varForm, varC, varRow++, "Competencia / Dificultad *", opPair(jcbCompetence, jcbDifficulty));
        opAdd(varForm, varC, varRow++, "Tema / Subtema *", opPair(jtfTopic, jtfSubtopic));
        opAdd(varForm, varC, varRow++, "Contexto *", new JScrollPane(jtaContext));
        opAdd(varForm, varC, varRow++, "Pregunta directa *", new JScrollPane(jtaStatement));

        ButtonGroup varGroup = new ButtonGroup();
        JPanel varOptions = new JPanel(new GridBagLayout());
        varOptions.setBackground(clsUiKit.BACKGROUND);
        varOptions.setBorder(BorderFactory.createTitledBorder("Marque la respuesta correcta; las otras tres son los distractores"));
        GridBagConstraints varO = new GridBagConstraints();
        varO.insets = new Insets(2, 2, 2, 2);
        for (int varI = 0; varI < 4; varI++) {
            jrbRight[varI] = new JRadioButton(clsQuestion.LETTERS.get(varI));
            jrbRight[varI].setBackground(clsUiKit.BACKGROUND);
            varGroup.add(jrbRight[varI]);
            varO.gridy = varI;
            varO.gridx = 0;
            varO.weightx = 0;
            varO.fill = GridBagConstraints.NONE;
            varOptions.add(jrbRight[varI], varO);
            varO.gridx = 1;
            varO.weightx = 1;
            varO.fill = GridBagConstraints.HORIZONTAL;
            varOptions.add(jtfOptions[varI], varO);
        }
        opAdd(varForm, varC, varRow++, "Opciones *", varOptions);
        opAdd(varForm, varC, varRow++, "Justificación *", new JScrollPane(jtaJustification));
        opAdd(varForm, varC, varRow++, "Bibliografía *", new JScrollPane(jtaBibliography));

        jtaErrors.setEditable(false);
        jtaErrors.setForeground(clsUiKit.ERROR);
        jtaErrors.setBackground(new Color(253, 240, 240));
        JScrollPane varErrors = new JScrollPane(jtaErrors);
        varErrors.setBorder(BorderFactory.createTitledBorder("Validación estructural"));
        opAdd(varForm, varC, varRow++, null, varErrors);
        varC.gridy = varRow;
        varC.weighty = 1;
        varForm.add(new JLabel(), varC); // relleno para que el formulario quede arriba

        JButton cmdValidate = clsUiKit.opButton("Validar");
        JButton cmdSave = clsUiKit.opButton("Guardar");
        JButton cmdClose = clsUiKit.opButton("Cerrar");
        cmdValidate.addActionListener(varE -> opValidate());
        cmdSave.addActionListener(varE -> opSave());
        cmdClose.addActionListener(varE -> dispose());
        cmdValidate.setVisible(attEditable);
        cmdSave.setVisible(attEditable);

        getContentPane().setLayout(new BorderLayout());
        JScrollPane varScroll = new JScrollPane(varForm);
        varScroll.getVerticalScrollBar().setUnitIncrement(16);
        getContentPane().add(varScroll, BorderLayout.CENTER);
        getContentPane().add(clsUiKit.opFlow(FlowLayout.RIGHT, cmdValidate, cmdSave, cmdClose), BorderLayout.SOUTH);
    }

    /** Descripción del plugin seleccionado. */
    private void opShowTypeHelp() {
        Object varPlugin = jcbType.getSelectedItem();
        jlTypeHelp.setText(varPlugin instanceof IQuestionPlugin ? ((IQuestionPlugin) varPlugin).opGetDescription() : "");
    }

    private static JPanel opPair(JComponent prmLeft, JComponent prmRight) {
        JPanel varPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        varPanel.setBackground(clsUiKit.BACKGROUND);
        varPanel.add(prmLeft);
        varPanel.add(prmRight);
        return varPanel;
    }

    private static void opAdd(JPanel prmForm, GridBagConstraints prmC, int prmRow, String prmLabel, JComponent prmField) {
        prmC.gridy = prmRow;
        prmC.anchor = GridBagConstraints.NORTHWEST;
        if (prmLabel == null) {
            prmC.gridx = 0;
            prmC.gridwidth = 2;
            prmC.weightx = 1;
            prmForm.add(prmField, prmC);
            prmC.gridwidth = 1;
            return;
        }
        prmC.gridx = 0;
        prmC.weightx = 0;
        prmForm.add(new JLabel(prmLabel), prmC);
        prmC.gridx = 1;
        prmC.weightx = 1;
        prmForm.add(prmField, prmC);
    }

    private void opLoad(clsQuestion prmQ) {
        for (int varI = 0; varI < jcbType.getItemCount(); varI++) {
            if (jcbType.getItemAt(varI).opGetCode().equals(prmQ.opGetType())) {
                jcbType.setSelectedIndex(varI);
            }
        }
        jcbCompetence.setSelectedItem(prmQ.opGetCompetence());
        jcbDifficulty.setSelectedItem(prmQ.opGetDifficulty());
        jtfTopic.setText(prmQ.opGetTopic());
        jtfSubtopic.setText(prmQ.opGetSubtopic());
        jtaContext.setText(prmQ.opGetContext());
        jtaStatement.setText(prmQ.opGetStatement());
        List<String> varOptions = prmQ.opGetOptions();
        for (int varI = 0; varI < 4; varI++) {
            jtfOptions[varI].setText(varOptions.get(varI));
            jrbRight[varI].setSelected(clsQuestion.LETTERS.get(varI).equals(prmQ.opGetRightAnswer()));
        }
        jtaJustification.setText(prmQ.opGetJustification());
        jtaBibliography.setText(prmQ.opGetBibliography());
        jtaContext.setCaretPosition(0);
    }

    private void opApplyMode() {
        jcbType.setEnabled(attEditable);
        jcbCompetence.setEnabled(attEditable);
        jcbDifficulty.setEnabled(attEditable);
        for (JTextField varField : List.of(jtfTopic, jtfSubtopic, jtfOptions[0], jtfOptions[1], jtfOptions[2], jtfOptions[3])) {
            varField.setEditable(attEditable);
        }
        for (JTextArea varArea : List.of(jtaContext, jtaStatement, jtaJustification, jtaBibliography)) {
            varArea.setEditable(attEditable);
        }
        for (JRadioButton varRadio : jrbRight) {
            varRadio.setEnabled(attEditable);
        }
        if (!attEditable) {
            jtaErrors.setForeground(clsUiKit.PRIMARY);
            jtaErrors.setText("Modo consulta: solo se pueden editar preguntas en estado Borrador.");
        }
    }

    /** Lee el formulario y arma una pregunta (sin id, autor ni estado). */
    private clsQuestion opReadForm() {
        String varRight = "";
        for (int varI = 0; varI < 4; varI++) {
            if (jrbRight[varI].isSelected()) {
                varRight = clsQuestion.LETTERS.get(varI);
            }
        }
        IQuestionPlugin varType = (IQuestionPlugin) jcbType.getSelectedItem();
        return new clsQuestion()
                .opSetType(varType == null ? "" : varType.opGetCode())
                .opSetCompetence(Objects.toString(jcbCompetence.getSelectedItem(), ""))
                .opSetDifficulty(Objects.toString(jcbDifficulty.getSelectedItem(), ""))
                .opSetTopic(jtfTopic.getText())
                .opSetSubtopic(jtfSubtopic.getText())
                .opSetContext(jtaContext.getText())
                .opSetStatement(jtaStatement.getText())
                .opSetOptions(jtfOptions[0].getText(), jtfOptions[1].getText(), jtfOptions[2].getText(), jtfOptions[3].getText())
                .opSetRightAnswer(varRight)
                .opSetJustification(jtaJustification.getText())
                .opSetBibliography(jtaBibliography.getText());
    }

    private boolean opValidate() {
        List<String> varErrors = attController.opValidate(opReadForm());
        if (varErrors.isEmpty()) {
            jtaErrors.setForeground(clsUiKit.SUCCESS);
            jtaErrors.setText("✔ La pregunta cumple todos los criterios de la validación estructural.");
            return true;
        }
        jtaErrors.setForeground(clsUiKit.ERROR);
        jtaErrors.setText("Corrija lo siguiente:\n • " + String.join("\n • ", varErrors));
        jtaErrors.setCaretPosition(0);
        return false;
    }

    private void opSave() {
        if (!opValidate()) {
            return;
        }
        clsResult<clsQuestion> varResult = attQuestion == null
                ? attController.opCreate(opReadForm())
                : attController.opUpdate(attQuestion.opGetId(), opReadForm());
        if (clsUiKit.opShowResult(this, varResult)) {
            attSaved = true;
            dispose();
        }
    }
}
