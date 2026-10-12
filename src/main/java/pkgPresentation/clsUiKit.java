package pkgPresentation;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import pkgController.clsResult;
import pkgDomain.pkgLifecycle.clsQuestionState;
import pkgServices.pkgMicrokernel.pkgKernel.IQuestionPlugin;

/**
 * Colores, botones y mensajes comunes para que todas las ventanas se vean
 * igual.
 */
public final class clsUiKit {

    public static final Color BACKGROUND = Color.WHITE;
    public static final Color PRIMARY = new Color(0, 51, 102);
    public static final Color ERROR = new Color(176, 0, 32);
    public static final Color SUCCESS = new Color(0, 110, 60);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);

    /** Valor de los combos de filtro que significa "sin filtrar". */
    public static final String ALL = "(Todos)";

    private clsUiKit() {
    }

    /** HU02: cada estado se muestra con su propio color. */
    public static Color opStateColor(clsQuestionState prmState) {
        switch (prmState.opGetCode()) {
            case "DRAFT":
                return new Color(158, 158, 158);      // gris
            case "PENDING_REVIEW":
                return new Color(245, 166, 35);       // naranja
            case "IN_REVIEW":
                return new Color(33, 118, 210);       // azul
            default:
                return Color.LIGHT_GRAY;
        }
    }

    /** Etiqueta con el color de un estado (para la leyenda y los formularios). */
    public static JLabel opStateBadge(clsQuestionState prmState) {
        JLabel varLabel = new JLabel(" " + prmState.opGetName() + " ");
        varLabel.setOpaque(true);
        varLabel.setBackground(opStateColor(prmState));
        varLabel.setForeground(Color.WHITE);
        varLabel.setFont(varLabel.getFont().deriveFont(Font.BOLD));
        return varLabel;
    }

    /** Pinta la celda de estado de una tabla con el color correspondiente. */
    public static void opUseStateColors(JTable prmTable, int prmColumn) {
        prmTable.getColumnModel().getColumn(prmColumn).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable prmT, Object prmValue, boolean prmSelected,
                    boolean prmFocus, int prmRow, int prmCol) {
                super.getTableCellRendererComponent(prmT, prmValue, prmSelected, prmFocus, prmRow, prmCol);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (prmValue instanceof clsQuestionState) {
                    setBackground(opStateColor((clsQuestionState) prmValue));
                    setForeground(Color.WHITE);
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return this;
            }
        });
    }

    /** Muestra el nombre del plugin (tipo de pregunta) en un combo. */
    public static void opUsePluginNames(javax.swing.JComboBox<?> prmCombo) {
        prmCombo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> prmList, Object prmValue,
                    int prmIndex, boolean prmSelected, boolean prmFocus) {
                Object varText = prmValue instanceof IQuestionPlugin ? ((IQuestionPlugin) prmValue).opGetName() : prmValue;
                return super.getListCellRendererComponent(prmList, varText, prmIndex, prmSelected, prmFocus);
            }
        });
    }

    public static JButton opButton(String prmText) {
        JButton varButton = new JButton(prmText);
        varButton.setFocusPainted(false);
        return varButton;
    }

    public static JLabel opTitle(String prmText) {
        JLabel varLabel = new JLabel(prmText);
        varLabel.setFont(TITLE_FONT);
        varLabel.setForeground(PRIMARY);
        return varLabel;
    }

    public static JPanel opFlow(int prmAlign, Component... prmComponents) {
        JPanel varPanel = new JPanel(new FlowLayout(prmAlign, 8, 6));
        varPanel.setBackground(BACKGROUND);
        for (Component varC : prmComponents) {
            varPanel.add(varC);
        }
        return varPanel;
    }

    public static JTextArea opTextArea(int prmRows) {
        JTextArea varArea = new JTextArea(prmRows, 40);
        varArea.setLineWrap(true);
        varArea.setWrapStyleWord(true);
        return varArea;
    }

    /** Muestra el resultado de un caso de uso. @return true si salió bien. */
    public static boolean opShowResult(Component prmParent, clsResult<?> prmResult) {
        if (prmResult.opIsOk()) {
            if (!prmResult.opGetMessage().isEmpty()) {
                opInfo(prmParent, prmResult.opGetFullMessage());
            }
            return true;
        }
        opError(prmParent, prmResult.opGetFullMessage());
        return false;
    }

    public static void opError(Component prmParent, String prmMessage) {
        JOptionPane.showMessageDialog(prmParent, prmMessage, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    public static void opInfo(Component prmParent, String prmMessage) {
        JOptionPane.showMessageDialog(prmParent, prmMessage, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean opConfirm(Component prmParent, String prmMessage) {
        return JOptionPane.showConfirmDialog(prmParent, prmMessage, "Confirmar", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    /** Texto recortado para mostrar en una tabla. */
    public static String opShort(String prmText, int prmMax) {
        String varText = prmText == null ? "" : prmText.replace('\n', ' ');
        return varText.length() <= prmMax ? varText : varText.substring(0, prmMax - 3) + "...";
    }
}
