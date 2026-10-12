package pkgPresentation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import pkgController.clsAuthController;
import pkgController.clsQuestionController;
import pkgController.clsResult;
import pkgController.clsReviewController;
import pkgDomain.pkgEntities.clsUser;

/**
 * Vista de inicio de sesión (y acceso al registro de usuarios).
 */
public class uiLogin extends JFrame {

    private final clsAuthController attAuth;
    private final clsQuestionController attQuestions;
    private final clsReviewController attReviews;

    private final JTextField jtfNickName = new JTextField(18);
    private final JPasswordField jpfPassword = new JPasswordField(18);

    public uiLogin(clsAuthController prmAuth, clsQuestionController prmQuestions, clsReviewController prmReviews) {
        attAuth = prmAuth;
        attQuestions = prmQuestions;
        attReviews = prmReviews;
        setTitle("Banco de Preguntas Saber Pro - Iniciar sesión");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        opBuild();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void opBuild() {
        JPanel varForm = new JPanel(new GridBagLayout());
        varForm.setBackground(clsUiKit.BACKGROUND);
        varForm.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints varC = new GridBagConstraints();
        varC.insets = new Insets(6, 6, 6, 6);
        varC.fill = GridBagConstraints.HORIZONTAL;

        JLabel varIcon = new JLabel();
        java.net.URL varUrl = getClass().getResource("/icons/icon_user.png");
        if (varUrl != null) {
            varIcon.setIcon(new ImageIcon(varUrl));
        }
        varIcon.setHorizontalAlignment(SwingConstants.CENTER);
        varC.gridx = 0;
        varC.gridy = 0;
        varC.gridwidth = 2;
        varForm.add(varIcon, varC);
        varC.gridy = 1;
        JLabel varTitle = clsUiKit.opTitle("Banco de Preguntas Saber Pro");
        varTitle.setHorizontalAlignment(SwingConstants.CENTER);
        varForm.add(varTitle, varC);

        varC.gridwidth = 1;
        varC.gridy = 2;
        varForm.add(new JLabel("Usuario:"), varC);
        varC.gridx = 1;
        varForm.add(jtfNickName, varC);
        varC.gridx = 0;
        varC.gridy = 3;
        varForm.add(new JLabel("Contraseña:"), varC);
        varC.gridx = 1;
        varForm.add(jpfPassword, varC);

        varC.gridx = 0;
        varC.gridy = 4;
        varC.gridwidth = 2;
        JLabel varHint = new JLabel("<html><small>Demo: admin / Admin123* &nbsp;·&nbsp; autor1 / Clave123*</small></html>");
        varHint.setForeground(java.awt.Color.GRAY);
        varForm.add(varHint, varC);

        JButton cmdLogin = clsUiKit.opButton("Ingresar");
        JButton cmdRegister = clsUiKit.opButton("Registrarse");
        JButton cmdExit = clsUiKit.opButton("Salir");
        cmdLogin.addActionListener(varE -> opLogin());
        cmdRegister.addActionListener(varE -> opRegister());
        jpfPassword.addActionListener(varE -> opLogin());
        cmdExit.addActionListener(varE -> System.exit(0));
        getRootPane().setDefaultButton(cmdLogin);

        getContentPane().setBackground(clsUiKit.BACKGROUND);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(varForm, BorderLayout.CENTER);
        getContentPane().add(clsUiKit.opFlow(FlowLayout.CENTER, cmdLogin, cmdRegister, cmdExit), BorderLayout.SOUTH);
    }

    private void opLogin() {
        clsResult<clsUser> varResult = attAuth.opLogin(jtfNickName.getText(), new String(jpfPassword.getPassword()));
        jpfPassword.setText("");
        if (!varResult.opIsOk()) {
            clsUiKit.opError(this, varResult.opGetMessage());
            return;
        }
        setVisible(false);
        new uiMainMenu(this, attAuth, attQuestions, attReviews).setVisible(true);
    }

    /** Abre el registro; si la persona se registra, deja su usuario escrito para que inicie sesión. */
    private void opRegister() {
        uiRegisterUser varRegister = new uiRegisterUser(this, attAuth);
        varRegister.setVisible(true);
        if (varRegister.opGetRegisteredNick() != null) {
            jtfNickName.setText(varRegister.opGetRegisteredNick());
            jpfPassword.requestFocusInWindow();
        }
    }

    /** Vuelve a mostrar el login al cerrar sesión. */
    public void opShowAgain() {
        jtfNickName.setText("");
        jpfPassword.setText("");
        setVisible(true);
        jtfNickName.requestFocusInWindow();
    }
}
