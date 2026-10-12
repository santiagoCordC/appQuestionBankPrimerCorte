package pkgApp;

import javax.swing.SwingUtilities;
import pkgController.clsAuthController;
import pkgController.clsDemoDataSeeder;
import pkgController.clsQuestionController;
import pkgController.clsReviewController;
import pkgController.clsSession;
import pkgDomain.pkgRepository.IAssignmentRepository;
import pkgDomain.pkgRepository.IQuestionRepository;
import pkgDomain.pkgRepository.IUserRepository;
import pkgPersistence.clsConnectionSQL;
import pkgPersistence.clsSqliteAssignmentRepository;
import pkgPersistence.clsSqliteQuestionRepository;
import pkgPersistence.clsSqliteUserRepository;
import pkgPresentation.uiLogin;
import pkgServices.clsConfig;
import pkgServices.pkgNotification.clsEmailNotifier;
import pkgServices.pkgNotification.clsSmtpEmailSender;
import pkgServices.pkgMicrokernel.pkgKernel.clsQuestionMicrokernel;

/**
 * Punto de entrada. Arma las piezas de las tres capas (datos → lógica →
 * presentación) y abre la ventana de inicio de sesión. Es el único lugar
 * donde se eligen las implementaciones concretas (SQLite, SMTP, plugins).
 */
public class App {

    public static void main(String[] args) {
        clsConfig varConfig = clsConfig.opLoadDefault();

        // Capa de datos
        clsConnectionSQL.opGetInstance().opCreateTables();
        IUserRepository varUsers = new clsSqliteUserRepository();
        IQuestionRepository varQuestions = new clsSqliteQuestionRepository(varUsers);
        IAssignmentRepository varAssignments = new clsSqliteAssignmentRepository(varUsers, varQuestions);
        new clsDemoDataSeeder(varUsers, varQuestions, varConfig).opSeed();

        // Capa de lógica (controladores MVC + servicios)
        clsSession varSession = new clsSession();
        clsAuthController varAuth = new clsAuthController(varUsers, varSession);
        clsQuestionMicrokernel varKernel = clsQuestionMicrokernel.opLoadDefault(); // carga los plugins de plugins.properties
        clsQuestionController varQuestionController = new clsQuestionController(varQuestions, varKernel, varConfig, varSession);
        clsReviewController varReviewController = new clsReviewController(varQuestions, varUsers, varAssignments, varSession);
        varReviewController.opAddObserver(new clsEmailNotifier(new clsSmtpEmailSender(varConfig)));

        // Capa de presentación
        SwingUtilities.invokeLater(() -> new uiLogin(varAuth, varQuestionController, varReviewController).setVisible(true));
    }
}
