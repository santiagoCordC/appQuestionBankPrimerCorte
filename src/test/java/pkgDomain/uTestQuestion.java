package pkgDomain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgLifecycle.clsQuestionLifecycle;
import pkgDomain.pkgEntities.enRole;
import pkgTestDoubles.clsSamples;

/**
 * Pruebas de las entidades: pregunta, estados y usuario.
 */
public class uTestQuestion {

    @Test
    public void unaPreguntaNuevaEstaEnBorradorYEsEditable() {
        clsQuestion varQ = new clsQuestion();
        assertEquals(clsQuestionLifecycle.DRAFT, varQ.opGetState());
        assertTrue(varQ.opIsEditable());
    }

    @Test
    public void flujoBorradorPendienteEnRevision() {
        clsQuestion varQ = clsSamples.opValidQuestion();
        varQ.opSubmitForReview();
        assertEquals(clsQuestionLifecycle.PENDING_REVIEW, varQ.opGetState());
        assertFalse("Fuera de Borrador no se edita", varQ.opIsEditable());
        varQ.opStartReview();
        assertEquals(clsQuestionLifecycle.IN_REVIEW, varQ.opGetState());
    }

    @Test(expected = IllegalStateException.class)
    public void noSePuedeEnviarDosVecesARevision() {
        clsQuestion varQ = clsSamples.opValidQuestion();
        varQ.opSubmitForReview();
        varQ.opSubmitForReview();
    }

    @Test(expected = IllegalStateException.class)
    public void noSeAsignanRevisoresAUnBorrador() {
        clsSamples.opValidQuestion().opStartReview();
    }

    @Test
    public void cadaEstadoDecideQueSePuedeHacer() {
        assertTrue(clsQuestionLifecycle.DRAFT.opIsEditable());
        assertTrue(clsQuestionLifecycle.DRAFT.opCanSubmitForReview());
        assertFalse(clsQuestionLifecycle.DRAFT.opCanAssignReviewers());
        assertFalse(clsQuestionLifecycle.PENDING_REVIEW.opIsEditable());
        assertFalse(clsQuestionLifecycle.PENDING_REVIEW.opCanSubmitForReview());
        assertTrue(clsQuestionLifecycle.PENDING_REVIEW.opCanAssignReviewers());
        assertFalse(clsQuestionLifecycle.IN_REVIEW.opIsEditable());
        assertFalse(clsQuestionLifecycle.IN_REVIEW.opCanAssignReviewers());
        assertEquals("Pendiente de revisión", clsQuestionLifecycle.PENDING_REVIEW.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void enRevisionNoSeVuelveAEnviar() {
        clsQuestionLifecycle.IN_REVIEW.opSubmitForReview(clsSamples.opValidQuestion());
    }

    @Test
    public void codigosDeEstadoParaLaBaseDeDatos() {
        for (pkgDomain.pkgLifecycle.clsQuestionState varState : clsQuestionLifecycle.opGetAll()) {
            assertEquals(varState, clsQuestionLifecycle.opFromCode(varState.opGetCode()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void codigoDeEstadoDesconocido() {
        clsQuestionLifecycle.opFromCode("APROBADA_X");
    }

    @Test
    public void reconoceASuAutor() {
        clsUser varAutor = clsSamples.opUser("a1", enRole.AUTHOR);
        clsQuestion varQ = clsSamples.opValidQuestion().opSetAuthor(varAutor);
        assertTrue(varQ.opIsAuthor(clsSamples.opUser("a1", enRole.AUTHOR)));
        assertFalse(varQ.opIsAuthor(clsSamples.opUser("a2", enRole.AUTHOR)));
        assertFalse(varQ.opIsAuthor(null));
        assertEquals("a1", varQ.opGetAuthorName());
        assertEquals("SELECCION_MULTIPLE", varQ.opGetType());
    }

    @Test
    public void copiarContenidoNoCambiaIdAutorNiEstado() {
        clsQuestion varOriginal = new clsQuestion().opSetId("q1").opSetState(clsQuestionLifecycle.DRAFT);
        clsQuestion varForm = clsSamples.opValidQuestion().opSetId("otro").opSetState(clsQuestionLifecycle.IN_REVIEW);
        varOriginal.opCopyContentFrom(varForm);
        assertEquals("q1", varOriginal.opGetId());
        assertEquals(clsQuestionLifecycle.DRAFT, varOriginal.opGetState());
        assertEquals(varForm.opGetStatement(), varOriginal.opGetStatement());
        assertEquals(varForm.opGetOptions(), varOriginal.opGetOptions());
        assertEquals("MVC", varOriginal.opGetOption("A"));
    }

    @Test
    public void losSettersQuitanEspaciosYNull() {
        clsQuestion varQ = new clsQuestion().opSetTopic("  Tema  ").opSetSubtopic(null);
        assertEquals("Tema", varQ.opGetTopic());
        assertEquals("", varQ.opGetSubtopic());
    }

    @Test
    public void usuarioRolYEstado() {
        clsUser varUser = clsSamples.opUser("r1", enRole.REVIEWER);
        assertTrue(varUser.opHasRole(enRole.REVIEWER));
        assertFalse(varUser.opHasRole(enRole.ADMIN));
        assertTrue(varUser.opIsActive());
        varUser.opSetActive(false);
        assertFalse(varUser.opIsActive());
    }
}
