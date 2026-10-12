package pkgPresentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import pkgController.clsAuthController;
import pkgController.clsQuestionController;
import pkgController.clsReviewController;
import pkgController.clsSession;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgEntities.enRole;

/**
 * Ventana principal. Muestra solo los módulos del rol que inició sesión:
 * <ul>
 * <li>Autor de preguntas: "Mis preguntas" (HU01, HU02, HU03).</li>
 * <li>Administrador: "Asignar revisores" (HU04).</li>
 * </ul>
 */
public class uiMainMenu extends JFrame {

    private final uiLogin attLogin;
    private final clsAuthController attAuth;

    public uiMainMenu(uiLogin prmLogin, clsAuthController prmAuth, clsQuestionController prmQuestions,
            clsReviewController prmReviews) {
        attLogin = prmLogin;
        attAuth = prmAuth;
        setTitle("Banco de Preguntas Saber Pro");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        clsSession varSession = prmAuth.opGetSession();
        clsUser varUser = varSession.opGetUser();

        JPanel varHeader = new JPanel(new BorderLayout());
        varHeader.setBackground(clsUiKit.PRIMARY);
        varHeader.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        JLabel varTitle = new JLabel("Banco de Preguntas Saber Pro");
        varTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        varTitle.setForeground(Color.WHITE);
        varHeader.add(varTitle, BorderLayout.WEST);
        JLabel varWho = new JLabel(varUser.opGetFullName() + "  |  " + varUser.opGetRole() + "   ");
        varWho.setForeground(Color.WHITE);
        JButton cmdLogout = clsUiKit.opButton("Cerrar sesión");
        cmdLogout.addActionListener(varE -> opLogout());
        JPanel varRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        varRight.setOpaque(false);
        varRight.add(varWho);
        varRight.add(cmdLogout);
        varHeader.add(varRight, BorderLayout.EAST);

        JTabbedPane varTabs = new JTabbedPane();
        if (varSession.opHasRole(enRole.AUTHOR)) {
            varTabs.addTab("Mis preguntas", new pnlMyQuestions(prmQuestions));
        }
        if (varSession.opHasRole(enRole.ADMIN)) {
            varTabs.addTab("Asignar revisores", new pnlAssignReviewers(prmReviews, prmQuestions));
        }

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(varHeader, BorderLayout.NORTH);
        if (varTabs.getTabCount() == 0) {
            JLabel varInfo = new JLabel("<html><center>Las preguntas que le asignen le llegarán por correo.<br>"
                    + "El módulo de evaluación de preguntas se implementa en el siguiente corte.</center></html>",
                    SwingConstants.CENTER);
            getContentPane().add(varInfo, BorderLayout.CENTER);
        } else {
            getContentPane().add(varTabs, BorderLayout.CENTER);
        }
        setSize(new Dimension(1100, 680));
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
    }

    private void opLogout() {
        attAuth.opLogout();
        dispose();
        attLogin.opShowAgain();
    }
}
