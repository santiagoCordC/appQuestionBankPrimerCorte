package pkgDomain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgLifecycle.clsQuestionLifecycle;
import pkgDomain.pkgEntities.enRole;
import pkgDomain.pkgRepository.clsPage;
import pkgDomain.pkgRepository.clsSearchCriteria;
import pkgTestDoubles.clsSamples;

/**
 * Pruebas de los filtros y la paginación del listado (HU03).
 */
public class uTestFilterAndPage {

    private final clsQuestion attQ = clsSamples.opValidQuestion().opSetAuthor(clsSamples.opUser("a1", enRole.AUTHOR));

    @Test
    public void filtroVacioAceptaTodo() {
        assertTrue(new clsSearchCriteria().opMatches(attQ));
        assertFalse(new clsSearchCriteria().opMatches(null));
    }

    @Test
    public void textoSinTildesNiMayusculasYVariasPalabras() {
        assertTrue(new clsSearchCriteria().opText("PATRON vista").opMatches(attQ));
        assertTrue(new clsSearchCriteria().opText("arquitectura").opMatches(attQ));
        assertFalse(new clsSearchCriteria().opText("patrón base de datos").opMatches(attQ));
    }

    @Test
    public void filtraPorEstadoCompetenciaDificultadYAutor() {
        assertTrue(new clsSearchCriteria().opState(clsQuestionLifecycle.DRAFT).opMatches(attQ));
        assertFalse(new clsSearchCriteria().opState(clsQuestionLifecycle.PENDING_REVIEW).opMatches(attQ));
        assertTrue(new clsSearchCriteria().opCompetence("diseño de software").opMatches(attQ));
        assertFalse(new clsSearchCriteria().opCompetence("Lectura crítica").opMatches(attQ));
        assertFalse(new clsSearchCriteria().opDifficulty("Alta").opMatches(attQ));
        assertTrue(new clsSearchCriteria().opAuthor("a1").opMatches(attQ));
        assertFalse(new clsSearchCriteria().opAuthor("a2").opMatches(attQ));
    }

    @Test
    public void paginaCortaLaLista() {
        List<Integer> varAll = List.of(1, 2, 3, 4, 5, 6, 7);
        clsPage<Integer> varPage2 = clsPage.opOf(varAll, 2, 3);
        assertEquals(List.of(4, 5, 6), varPage2.opGetItems());
        assertEquals(3, varPage2.opGetTotalPages());
        assertEquals(7, varPage2.opGetTotalItems());
        assertTrue(varPage2.opHasPrevious());
        assertTrue(varPage2.opHasNext());
        clsPage<Integer> varLast = clsPage.opOf(varAll, 3, 3);
        assertEquals(List.of(7), varLast.opGetItems());
        assertFalse(varLast.opHasNext());
    }

    @Test
    public void paginaFueraDeRangoSeAjusta() {
        assertEquals(3, clsPage.opOf(List.of(1, 2, 3, 4, 5, 6, 7), 99, 3).opGetNumber());
        assertEquals(1, clsPage.opOf(List.of(1, 2), 0, 3).opGetNumber());
        clsPage<Integer> varEmpty = clsPage.opOf(List.<Integer>of(), 1, 3);
        assertEquals(1, varEmpty.opGetTotalPages());
        assertTrue(varEmpty.opGetItems().isEmpty());
    }
}
