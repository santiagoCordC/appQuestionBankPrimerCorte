package pkgController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgLifecycle.clsQuestionLifecycle;
import pkgDomain.pkgEntities.enRole;
import pkgDomain.pkgRepository.clsPage;
import pkgDomain.pkgRepository.clsSearchCriteria;
import pkgTestDoubles.clsMemoryRepositories;
import pkgTestDoubles.clsSamples;

/**
 * Pruebas de HU01 (crear), HU02 (enviar a revisión) y HU03 (listar).
 */
public class uTestQuestionController {

    private clsMemoryRepositories.Questions attRepo;
    private clsSession attSession;
    private clsQuestionController attController;
    private final clsUser attAuthor = clsSamples.opUser("autor1", enRole.AUTHOR);
    private final clsUser attOtherAuthor = clsSamples.opUser("autor2", enRole.AUTHOR);

    @Before
    public void opSetUp() {
        attRepo = new clsMemoryRepositories.Questions();
        attSession = new clsSession();
        attController = new clsQuestionController(attRepo, clsSamples.opKernel(), clsSamples.opConfig(), attSession);
        attSession.opStart(attAuthor);
    }

    private clsQuestion opCreateValid() {
        return attController.opCreate(clsSamples.opValidQuestion()).opGetValue();
    }

    /* ---------- HU01 ---------- */

    @Test
    public void autorCreaPreguntaValidaEnBorrador() {
        clsResult<clsQuestion> varResult = attController.opCreate(clsSamples.opValidQuestion());
        assertTrue(varResult.opGetFullMessage(), varResult.opIsOk());
        clsQuestion varQ = varResult.opGetValue();
        assertNotNull(varQ.opGetId());
        assertEquals(clsQuestionLifecycle.DRAFT, varQ.opGetState());
        assertTrue(varQ.opIsAuthor(attAuthor));
        assertFalse(varQ.opGetCreatedAt().isEmpty());
        assertEquals(1, attRepo.attItems.size());
    }

    @Test
    public void alGrabarAplicaLaValidacionEstructural() {
        clsResult<clsQuestion> varResult = attController.opCreate(clsSamples.opValidQuestion().opSetContext(""));
        assertFalse(varResult.opIsOk());
        assertFalse(varResult.opGetErrors().isEmpty());
        assertTrue(attRepo.attItems.isEmpty());
    }

    @Test
    public void soloUnAutorCreaPreguntas() {
        attSession.opStart(clsSamples.opUser("adm", enRole.ADMIN));
        assertFalse(attController.opCreate(clsSamples.opValidQuestion()).opIsOk());
        attSession.opEnd();
        assertFalse(attController.opCreate(clsSamples.opValidQuestion()).opIsOk());
    }

    @Test
    public void informaSiLaBaseDeDatosFalla() {
        attRepo.attFail = true;
        assertFalse(attController.opCreate(clsSamples.opValidQuestion()).opIsOk());
    }

    @Test
    public void elAutorEditaSuBorrador() {
        clsQuestion varQ = opCreateValid();
        clsResult<clsQuestion> varResult = attController.opUpdate(varQ.opGetId(),
                clsSamples.opValidQuestion().opSetTopic("Patrones"));
        assertTrue(varResult.opGetFullMessage(), varResult.opIsOk());
        assertEquals("Patrones", attRepo.opFindById(varQ.opGetId()).opGetTopic());
    }

    @Test
    public void otroAutorNoPuedeEditar() {
        clsQuestion varQ = opCreateValid();
        attSession.opStart(attOtherAuthor);
        assertFalse(attController.opCanEdit(varQ));
        assertFalse(attController.opUpdate(varQ.opGetId(), clsSamples.opValidQuestion()).opIsOk());
    }

    /* ---------- HU02 ---------- */

    @Test
    public void enviaBorradorAPendienteDeRevision() {
        clsQuestion varQ = opCreateValid();
        clsResult<clsQuestion> varResult = attController.opSubmitForReview(varQ.opGetId());
        assertTrue(varResult.opGetFullMessage(), varResult.opIsOk());
        assertEquals(clsQuestionLifecycle.PENDING_REVIEW, attRepo.opFindById(varQ.opGetId()).opGetState());
        assertEquals("El cambio se guardó", clsQuestionLifecycle.PENDING_REVIEW,
                attRepo.attSavedStates.get(attRepo.attSavedStates.size() - 1));
        assertFalse("Ya no se puede editar", attController.opCanEdit(varQ));
        assertFalse(attController.opUpdate(varQ.opGetId(), clsSamples.opValidQuestion()).opIsOk());
    }

    @Test
    public void noSeEnviaDosVeces() {
        clsQuestion varQ = opCreateValid();
        attController.opSubmitForReview(varQ.opGetId());
        assertFalse(attController.opSubmitForReview(varQ.opGetId()).opIsOk());
    }

    @Test
    public void soloElAutorEnviaSuPregunta() {
        clsQuestion varQ = opCreateValid();
        attSession.opStart(attOtherAuthor);
        assertFalse(attController.opSubmitForReview(varQ.opGetId()).opIsOk());
        assertEquals(clsQuestionLifecycle.DRAFT, varQ.opGetState());
    }

    @Test
    public void preguntaInexistente() {
        assertFalse(attController.opSubmitForReview("no-existe").opIsOk());
        assertFalse(attController.opUpdate("no-existe", clsSamples.opValidQuestion()).opIsOk());
    }

    /* ---------- HU03 ---------- */

    @Test
    public void listaSoloMisPreguntasPaginadas() {
        for (int varI = 0; varI < 5; varI++) {
            opCreateValid();
        }
        attSession.opStart(attOtherAuthor);
        opCreateValid();
        attSession.opStart(attAuthor);

        clsPage<clsQuestion> varPage1 = attController.opListMine(new clsSearchCriteria(), 1).opGetValue();
        assertEquals("Solo las 5 de autor1", 5, varPage1.opGetTotalItems());
        assertEquals("Tamaño de página 3", 3, varPage1.opGetItems().size());
        assertEquals(2, varPage1.opGetTotalPages());
        clsPage<clsQuestion> varPage2 = attController.opListMine(null, 2).opGetValue();
        assertEquals(2, varPage2.opGetItems().size());
        assertTrue(varPage2.opGetItems().stream().allMatch(varQ -> varQ.opIsAuthor(attAuthor)));
    }

    @Test
    public void listaConFiltroDeEstado() {
        clsQuestion varQ = opCreateValid();
        opCreateValid();
        attController.opSubmitForReview(varQ.opGetId());
        clsPage<clsQuestion> varPage = attController.opListMine(
                new clsSearchCriteria().opState(clsQuestionLifecycle.PENDING_REVIEW), 1).opGetValue();
        assertEquals(1, varPage.opGetTotalItems());
        assertEquals(varQ.opGetId(), varPage.opGetItems().get(0).opGetId());
    }

    @Test
    public void sinSesionNoLista() {
        attSession.opEnd();
        assertFalse(attController.opListMine(new clsSearchCriteria(), 1).opIsOk());
    }
}
