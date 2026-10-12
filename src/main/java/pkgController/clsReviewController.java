package pkgController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsReviewAssignment;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgEntities.enRole;
import pkgDomain.pkgRepository.IAssignmentRepository;
import pkgDomain.pkgRepository.IQuestionRepository;
import pkgDomain.pkgRepository.IUserRepository;
import pkgServices.pkgNotification.IAssignmentObserver;

/**
 * HU04: el administrador asigna al menos un revisor a las preguntas en
 * "Pendiente de revisión"; luego se notifica a cada revisor.
 * <p>
 * Es el sujeto del patrón Observer: guarda una lista de observadores y les
 * avisa de cada asignación. El envío del correo lo hace un observador
 * ({@code clsEmailNotifier}), así este controlador no depende del correo.
 */
public class clsReviewController {

    private final IQuestionRepository attQuestions;
    private final IUserRepository attUsers;
    private final IAssignmentRepository attAssignments;
    private final clsSession attSession;
    private final List<IAssignmentObserver> attObservers = new ArrayList<>();

    public clsReviewController(IQuestionRepository prmQuestions, IUserRepository prmUsers,
            IAssignmentRepository prmAssignments, clsSession prmSession) {
        attQuestions = prmQuestions;
        attUsers = prmUsers;
        attAssignments = prmAssignments;
        attSession = prmSession;
    }

    /* ===================== Observer ===================== */

    public void opAddObserver(IAssignmentObserver prmObserver) {
        attObservers.add(prmObserver);
    }

    public void opRemoveObserver(IAssignmentObserver prmObserver) {
        attObservers.remove(prmObserver);
    }

    private void opNotify(clsReviewAssignment prmAssignment) {
        for (IAssignmentObserver varObserver : attObservers) {
            varObserver.opOnReviewerAssigned(prmAssignment);
        }
    }

    /* ===================== Casos de uso ===================== */

    /** Preguntas en "Pendiente de revisión", de la más antigua a la más reciente. */
    public clsResult<List<clsQuestion>> opListPendingReview() {
        if (!attSession.opHasRole(enRole.ADMIN)) {
            return clsResult.opFail("Solo el administrador puede asignar revisores.");
        }
        List<clsQuestion> varPending = attQuestions.opFindAll().stream()
                .filter(varQ -> varQ.opGetState().opCanAssignReviewers())
                .sorted(Comparator.comparing(clsQuestion::opGetUpdatedAt))
                .collect(Collectors.toList());
        return clsResult.opOk(varPending, "");
    }

    /** Revisores activos que pueden revisar la pregunta (nunca su propio autor). */
    public List<clsUser> opGetReviewerCandidates(clsQuestion prmQuestion) {
        return attUsers.opFindByRole(enRole.REVIEWER).stream()
                .filter(clsUser::opIsActive)
                .filter(varU -> !prmQuestion.opIsAuthor(varU))
                .collect(Collectors.toList());
    }

    public clsResult<List<clsReviewAssignment>> opAssignReviewers(String prmQuestionId, List<String> prmReviewerIds) {
        if (!attSession.opHasRole(enRole.ADMIN)) {
            return clsResult.opFail("Solo el administrador puede asignar revisores.");
        }
        clsQuestion varQuestion = attQuestions.opFindById(prmQuestionId);
        if (varQuestion == null) {
            return clsResult.opFail("La pregunta no existe.");
        }
        if (!varQuestion.opGetState().opCanAssignReviewers()) {
            return clsResult.opFail("Solo se asignan revisores a preguntas en \"Pendiente de revisión\".");
        }
        if (prmReviewerIds == null || prmReviewerIds.isEmpty()) {
            return clsResult.opFail("Seleccione al menos un revisor.");
        }
        List<clsUser> varCandidates = opGetReviewerCandidates(varQuestion);
        List<clsUser> varReviewers = new ArrayList<>();
        for (String varId : prmReviewerIds) {
            clsUser varReviewer = varCandidates.stream()
                    .filter(varU -> varU.opGetId().equals(varId)).findFirst().orElse(null);
            if (varReviewer == null) {
                return clsResult.opFail("Uno de los usuarios seleccionados no es un revisor activo o es el autor de la pregunta.");
            }
            if (!varReviewers.contains(varReviewer)) {
                varReviewers.add(varReviewer);
            }
        }

        varQuestion.opStartReview();
        varQuestion.opSetUpdatedAt(clsQuestionController.opNow());
        if (!attQuestions.opUpdate(varQuestion)) {
            return clsResult.opFail("No se pudo guardar el cambio de estado de la pregunta.");
        }
        List<clsReviewAssignment> varCreated = new ArrayList<>();
        for (clsUser varReviewer : varReviewers) {
            clsReviewAssignment varAssignment = new clsReviewAssignment(UUID.randomUUID().toString(), varQuestion,
                    varReviewer, attSession.opGetUser(), clsQuestionController.opNow());
            if (attAssignments.opSave(varAssignment)) {
                varCreated.add(varAssignment);
                opNotify(varAssignment);
            }
        }
        String varNames = varCreated.stream().map(varA -> varA.opGetReviewer().opGetFullName()
                + " <" + varA.opGetReviewer().opGetEmail() + ">").collect(Collectors.joining("\n • ", " • ", ""));
        return clsResult.opOk(varCreated, "Se asignaron " + varCreated.size()
                + " revisor(es) y se les envió un correo de notificación:\n" + varNames
                + "\n\nLa pregunta quedó \"" + varQuestion.opGetState() + "\".");
    }

    public List<clsReviewAssignment> opGetAssignments(String prmQuestionId) {
        return attAssignments.opFindByQuestion(prmQuestionId);
    }
}
