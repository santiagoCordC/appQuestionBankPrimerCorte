package pkgPresentation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import pkgController.clsAuthController;
import pkgController.clsResult;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgEntities.clsUserData;
import pkgDomain.pkgEntities.enRole;

/**
 * Vista de registro de usuarios. Los datos se validan con la tubería de
 * filtros de registro (en el controlador). Desde aquí solo se crean cuentas
 * de Autor de preguntas o Revisor.
 */
public class uiRegisterUser extends JDialog {

    private final clsAuthController attAuth;

    private final JTextField jtfFullName = new JTextField(22);
    private final JTextField jtfNickName = new JTextField(22);
    private final JTextField jtfEmail = new JTextField(22);
    private final JPasswordField jpfPassword = new JPasswordField(22);
    private final JPasswordField jpfConfirm = new JPasswordField(22);
    private final JComboBox<enRole> jcbRole = new JComboBox<>();
    private String attRegisteredNick;

    public uiRegisterUser(Window prmOwner, clsAuthController prmAuth) {
        super(prmOwner, ModalityType.APPLICATION_MODAL);
        attAuth = prmAuth;
        setTitle("Registro de usuario");
        prmAuth.opGetRegisterRoles().forEach(jcbRole::addItem);
        opBuild();
        pack();
        setResizable(false);
        setLocationRelativeTo(prmOwner);
    }

    /** @return el usuario registrado, o null si se canceló. */
    public String opGetRegisteredNick() {
        return attRegisteredNick;
    }

    private void opBuild() {
        JPanel varForm = new JPanel(new GridBagLayout());
        varForm.setBackground(clsUiKit.BACKGROUND);
        varForm.setBorder(BorderFactory.createEmptyBorder(14, 22, 6, 22));
        GridBagConstraints varC = new GridBagConstraints();
        varC.insets = new Insets(5, 5, 5, 5);
        varC.fill = GridBagConstraints.HORIZONTAL;

        JLabel varTitle = clsUiKit.opTitle("Crear cuenta");
        java.net.URL varUrl = getClass().getResource("/icons/icon_register.png");
        if (varUrl != null) {
            varTitle.setIcon(new ImageIcon(new ImageIcon(varUrl).getImage().getScaledInstance(48, 48, java.awt.Image.SCALE_SMOOTH)));
        }
        varTitle.setHorizontalAlignment(SwingConstants.CENTER);
        varC.gridwidth = 2;
        varForm.add(varTitle, varC);
        varC.gridwidth = 1;

        int varRow = 1;
        varRow = opRow(varForm, varC, varRow, "Nombre completo:", jtfFullName);
        varRow = opRow(varForm, varC, varRow, "Usuario:", jtfNickName);
        varRow = opRow(varForm, varC, varRow, "Correo electrónico:", jtfEmail);
        varRow = opRow(varForm, varC, varRow, "Contraseña:", jpfPassword);
        varRow = opRow(varForm, varC, varRow, "Confirmar contraseña:", jpfConfirm);
        varRow = opRow(varForm, varC, varRow, "Rol:", jcbRole);
        varC.gridx = 0;
        varC.gridy = varRow;
        varC.gridwidth = 2;
        JLabel varHint = new JLabel("<html><small>La contraseña debe tener mínimo 8 caracteres, una mayúscula,<br>"
                + "un número y un carácter especial. El correo se usa para las notificaciones.</small></html>");
        varHint.setForeground(java.awt.Color.GRAY);
        varForm.add(varHint, varC);

        JButton cmdRegister = clsUiKit.opButton("Registrarme");
        JButton cmdCancel = clsUiKit.opButton("Cancelar");
        cmdRegister.addActionListener(varE -> opRegister());
        cmdCancel.addActionListener(varE -> dispose());
        getRootPane().setDefaultButton(cmdRegister);

        getContentPane().setBackground(clsUiKit.BACKGROUND);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(varForm, BorderLayout.CENTER);
        getContentPane().add(clsUiKit.opFlow(FlowLayout.CENTER, cmdRegister, cmdCancel), BorderLayout.SOUTH);
    }

    private static int opRow(JPanel prmForm, GridBagConstraints prmC, int prmRow, String prmLabel, JComponent prmField) {
        prmC.gridy = prmRow;
        prmC.gridx = 0;
        prmForm.add(new JLabel(prmLabel), prmC);
        prmC.gridx = 1;
        prmForm.add(prmField, prmC);
        return prmRow + 1;
    }

    private void opRegister() {
        clsUserData varData = new clsUserData(jtfFullName.getText(), jtfNickName.getText(), jtfEmail.getText(),
                new String(jpfPassword.getPassword()), new String(jpfConfirm.getPassword()),
                (enRole) jcbRole.getSelectedItem());
        clsResult<clsUser> varResult = attAuth.opRegister(varData);
        if (clsUiKit.opShowResult(this, varResult)) {
            attRegisteredNick = varResult.opGetValue().opGetNickName();
            dispose();
        }
    }
}
