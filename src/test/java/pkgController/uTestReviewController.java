package pkgController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsReviewAssignment;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgLifecycle.clsQuestionLifecycle;
import pkgDomain.pkgEntities.enRole;
import pkgTestDoubles.clsMemoryRepositories;
import pkgTestDoubles.clsSamples;

/**
 * Pruebas de HU04: asignar revisores y notificarlos (Observer).
 */
public class uTestReviewController {

    private clsMemoryRepositories.Questions attQuestions;
    private clsMemoryRepositories.Users attUsers;
    private clsMemoryRepositories.Assignments attAssignments;
    private clsSession attSession;
    private clsReviewController attController;
    private final List<clsReviewAssignment> attNotified = new ArrayList<>();

    private final clsUser attAdmin = clsSamples.opUser("admin", enRole.ADMIN);
    private final clsUser attAuthor = clsSamples.opUser("autor1", enRole.AUTHOR);
    private final clsUser attRev1 = clsSamples.opUser("rev1", enRole.REVIEWER);
    private final clsUser attRev2 = clsSamples.opUser("rev2", enRole.REVIEWER);
    private clsQuestion attPending;

    @Before
    public void opSetUp() {
        attQuestions = new clsMemoryRepositories.Questions();
        attUsers = new clsMemoryRepositories.Users();
        attAssignments = new clsMemoryRepositories.Assignments();
        List.of(attAdmin, attAuthor, attRev1, attRev2).forEach(attUsers::opSave);
        attPending = clsSamples.opValidQuestion().opSetId("q1").opSetAuthor(attAuthor)
                .opSetState(clsQuestionLifecycle.PENDING_REVIEW).opSetUpdatedAt("2026-10-01 10:00:00");
        attQuestions.opSave(attPending);
        attQuestions.opSave(clsSamples.opValidQuestion().opSetId("q2").opSetAuthor(attAuthor).opSetUpdatedAt("2026-10-01 11:00:00"));

        attSession = new clsSession();
        attSession.opStart(attAdmin);
        attController = new clsReviewController(attQuestions, attUsers, attAssignments, attSession);
        attController.opAddObserver(attNotified::add);
    }

    @Test
    public void listaSoloPendientesDeRevision() {
        List<clsQuestion> varPending = attController.opListPendingReview().opGetValue();
        assertEquals(1, varPending.size());
        assertEquals("q1", varPending.get(0).opGetId());
    }

    @Test
    public void soloElAdministradorAsigna() {
        attSession.opStart(attAuthor);
        assertFalse(attController.opListPendingReview().opIsOk());
        assertFalse(attController.opAssignReviewers("q1", List.of("rev1")).opIsOk());
        assertTrue(attAssignments.attItems.isEmpty());
    }

    @Test
    public void asignaVariosRevisoresYLosNotifica() {
        clsResult<List<clsReviewAssignment>> varResult = attController.opAssignReviewers("q1", List.of("rev1", "rev2"));
        assertTrue(varResult.opGetFullMessage(), varResult.opIsOk());
        assertEquals(2, attAssignments.opFindByQuestion("q1").size());
        assertEquals("Se notifica a cada revisor", 2, attNotified.size());
        assertEquals(clsQuestionLifecycle.IN_REVIEW, attPending.opGetState());
        assertTrue("Sale de la lista de pendientes", attController.opListPendingReview().opGetValue().isEmpty());
        assertEquals(attAdmin, attAssignments.attItems.get(0).opGetAssignedBy());
    }

    @Test
    public void exigeAlMenosUnRevisor() {
        assertFalse(attController.opAssignReviewers("q1", List.of()).opIsOk());
        assertFalse(attController.opAssignReviewers("q1", null).opIsOk());
        assertEquals(clsQuestionLifecycle.PENDING_REVIEW, attPending.opGetState());
        assertTrue(attNotified.isEmpty());
    }

    @Test
    public void soloPreguntasPendientesDeRevision() {
        assertFalse("q2 está en Borrador", attController.opAssignReviewers("q2", List.of("rev1")).opIsOk());
        assertFalse(attController.opAssignReviewers("no-existe", List.of("rev1")).opIsOk());
    }

    @Test
    public void rechazaUsuariosQueNoSonRevisoresActivos() {
        assertFalse("Un autor no es revisor", attController.opAssignReviewers("q1", List.of("autor1")).opIsOk());
        attRev2.opSetActive(false);
        assertFalse("Revisor inactivo", attController.opAssignReviewers("q1", List.of("rev1", "rev2")).opIsOk());
        assertTrue(attAssignments.attItems.isEmpty());
        assertEquals(clsQuestionLifecycle.PENDING_REVIEW, attPending.opGetState());
    }

    @Test
    public void candidatosSonRevisoresActivosQueNoSonElAutor() {
        clsUser varReviewerAuthor = clsSamples.opUser("revAutor", enRole.REVIEWER);
        attUsers.opSave(varReviewerAuthor);
        clsQuestion varOwn = clsSamples.opValidQuestion().opSetId("q3").opSetAuthor(varReviewerAuthor);
        assertEquals(2, attController.opGetReviewerCandidates(varOwn).size());
        assertEquals(3, attController.opGetReviewerCandidates(attPending).size());
    }

    @Test
    public void revisorRepetidoSeAsignaUnaVez() {
        attController.opAssignReviewers("q1", List.of("rev1", "rev1"));
        assertEquals(1, attController.opGetAssignments("q1").size());
    }

    @Test
    public void sinObservadoresTambienFunciona() {
        clsReviewController varPlain = new clsReviewController(attQuestions, attUsers, attAssignments, attSession);
        assertTrue(varPlain.opAssignReviewers("q1", List.of("rev1")).opIsOk());
    }
}
