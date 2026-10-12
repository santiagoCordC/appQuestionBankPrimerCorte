package pkgServices.pkgNotification;

import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsReviewAssignment;
import pkgDomain.pkgEntities.clsUser;
import pkgUtil.clsText;

/**
 * Observador concreto: envía un correo al revisor asignado (HU04).
 */
public class clsEmailNotifier implements IAssignmentObserver {

    private final IEmailSender attSender;

    public clsEmailNotifier(IEmailSender prmSender) {
        attSender = prmSender;
    }

    @Override
    public void opOnReviewerAssigned(clsReviewAssignment prmAssignment) {
        clsUser varReviewer = prmAssignment.opGetReviewer();
        if (clsText.opIsBlank(varReviewer.opGetEmail())) {
            System.err.println("El revisor " + varReviewer.opGetNickName() + " no tiene correo registrado.");
            return;
        }
        attSender.opSend(varReviewer.opGetEmail(), opBuildSubject(prmAssignment), opBuildBody(prmAssignment));
    }

    public static String opBuildSubject(clsReviewAssignment prmAssignment) {
        return "Banco de preguntas Saber Pro: tiene una pregunta asignada para revisión";
    }

    public static String opBuildBody(clsReviewAssignment prmAssignment) {
        clsQuestion varQuestion = prmAssignment.opGetQuestion();
        return "Hola " + prmAssignment.opGetReviewer().opGetFullName() + ",\n\n"
                + "El administrador " + prmAssignment.opGetAssignedBy().opGetFullName()
                + " le asignó la revisión de la siguiente pregunta:\n\n"
                + "  Pregunta: " + varQuestion.opGetStatement() + "\n"
                + "  Competencia: " + varQuestion.opGetCompetence() + "\n"
                + "  Tema / subtema: " + varQuestion.opGetTopic() + " / " + varQuestion.opGetSubtopic() + "\n"
                + "  Dificultad: " + varQuestion.opGetDifficulty() + "\n"
                + "  Autor: " + varQuestion.opGetAuthorName() + "\n"
                + "  Fecha de asignación: " + prmAssignment.opGetAssignedAt() + "\n\n"
                + "Ingrese al sistema para revisarla.\n";
    }
}
