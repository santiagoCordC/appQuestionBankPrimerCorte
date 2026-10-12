package pkgDomain.pkgRepository;

import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgLifecycle.clsQuestionState;
import pkgUtil.clsText;

/**
 * Criterios de búsqueda de preguntas (HU03). Un criterio vacío (o null) no
 * filtra.
 */
public class clsSearchCriteria {

    private String attText = "";
    private clsQuestionState attState;
    private String attType = "";
    private String attCompetence = "";
    private String attDifficulty = "";
    private String attAuthorId = "";

    public clsSearchCriteria opText(String prmValue) { attText = clsText.opNormalize(prmValue); return this; }
    public clsSearchCriteria opState(clsQuestionState prmValue) { attState = prmValue; return this; }
    public clsSearchCriteria opType(String prmValue) { attType = opClean(prmValue); return this; }
    public clsSearchCriteria opCompetence(String prmValue) { attCompetence = opClean(prmValue); return this; }
    public clsSearchCriteria opDifficulty(String prmValue) { attDifficulty = opClean(prmValue); return this; }
    public clsSearchCriteria opAuthor(String prmAuthorId) { attAuthorId = opClean(prmAuthorId); return this; }

    public boolean opMatches(clsQuestion prmQuestion) {
        if (prmQuestion == null) {
            return false;
        }
        if (attState != null && attState != prmQuestion.opGetState()) {
            return false;
        }
        if (!opSame(attType, prmQuestion.opGetType()) || !opSame(attCompetence, prmQuestion.opGetCompetence())
                || !opSame(attDifficulty, prmQuestion.opGetDifficulty())) {
            return false;
        }
        if (!attAuthorId.isEmpty() && (prmQuestion.opGetAuthor() == null
                || !attAuthorId.equals(prmQuestion.opGetAuthor().opGetId()))) {
            return false;
        }
        return opContainsText(prmQuestion);
    }

    private static boolean opSame(String prmCriterion, String prmValue) {
        return prmCriterion.isEmpty() || prmCriterion.equalsIgnoreCase(prmValue);
    }

    /** Todas las palabras buscadas deben aparecer (sin importar tildes ni mayúsculas). */
    private boolean opContainsText(clsQuestion prmQuestion) {
        if (attText.isEmpty()) {
            return true;
        }
        String varWhere = clsText.opNormalize(String.join(" ", prmQuestion.opGetStatement(),
                prmQuestion.opGetContext(), prmQuestion.opGetTopic(), prmQuestion.opGetSubtopic()));
        for (String varWord : attText.split(" ")) {
            if (!varWhere.contains(varWord)) {
                return false;
            }
        }
        return true;
    }

    private static String opClean(String prmValue) {
        return prmValue == null ? "" : prmValue.trim();
    }
}
