package pkgDomain.pkgEntities;

import java.util.Arrays;
import java.util.List;
import pkgDomain.pkgLifecycle.clsQuestionLifecycle;
import pkgDomain.pkgLifecycle.clsQuestionState;

/**
 * Pregunta de selección múltiple con única respuesta (HU01), con los campos
 * del Diseño Centrado en Evidencia: contexto, pregunta directa, cuatro
 * opciones (A-D), respuesta correcta, justificación, bibliografía,
 * competencia, tema, subtema y nivel de dificultad.
 * <p>
 * El tipo de pregunta (selección múltiple, caso...) lo aporta un plugin del
 * microkernel. El estado es un objeto {@link clsQuestionState} (patrón State):
 * la pregunta delega en él las acciones que dependen del estado.
 * <p>
 * Los setters devuelven la misma pregunta para poder encadenarlos:
 * {@code new clsQuestion().opSetContext("...").opSetStatement("...")}.
 */
public class clsQuestion {

    public static final List<String> LETTERS = List.of("A", "B", "C", "D");

    private String attId;
    private clsUser attAuthor;
    private String attType = "";
    private clsQuestionState attState = clsQuestionLifecycle.DRAFT;

    private String attContext = "";
    private String attStatement = "";
    private final String[] attOptions = {"", "", "", ""};
    private String attRightAnswer = "";
    private String attJustification = "";
    private String attBibliography = "";
    private String attCompetence = "";
    private String attTopic = "";
    private String attSubtopic = "";
    private String attDifficulty = "";

    private String attCreatedAt = "";
    private String attUpdatedAt = "";

    /* ===================== Reglas de negocio ===================== */

    /** Solo se modifica mientras está en Borrador (lo decide el estado). */
    public boolean opIsEditable() {
        return attState.opIsEditable();
    }

    public boolean opIsAuthor(clsUser prmUser) {
        return prmUser != null && attAuthor != null && attAuthor.opGetId().equals(prmUser.opGetId());
    }

    /** HU02: Borrador → Pendiente de revisión. El estado actual decide si se puede. */
    public void opSubmitForReview() {
        attState.opSubmitForReview(this);
    }

    /** HU04: Pendiente de revisión → En revisión. El estado actual decide si se puede. */
    public void opStartReview() {
        attState.opStartReview(this);
    }

    /** Copia el contenido (no el id, el autor ni el estado) de otra pregunta. */
    public void opCopyContentFrom(clsQuestion prmOther) {
        attType = prmOther.attType;
        attContext = prmOther.attContext;
        attStatement = prmOther.attStatement;
        for (int varI = 0; varI < 4; varI++) {
            attOptions[varI] = prmOther.attOptions[varI];
        }
        attRightAnswer = prmOther.attRightAnswer;
        attJustification = prmOther.attJustification;
        attBibliography = prmOther.attBibliography;
        attCompetence = prmOther.attCompetence;
        attTopic = prmOther.attTopic;
        attSubtopic = prmOther.attSubtopic;
        attDifficulty = prmOther.attDifficulty;
    }

    /* ===================== Getters ===================== */

    public String opGetId() { return attId; }
    public clsUser opGetAuthor() { return attAuthor; }
    public clsQuestionState opGetState() { return attState; }
    /** Código del tipo de pregunta (plugin del microkernel). */
    public String opGetType() { return attType; }
    public String opGetContext() { return attContext; }
    public String opGetStatement() { return attStatement; }
    public List<String> opGetOptions() { return Arrays.asList(attOptions.clone()); }
    public String opGetOption(String prmLetter) {
        int varIndex = LETTERS.indexOf(prmLetter);
        return varIndex < 0 ? "" : attOptions[varIndex];
    }
    /** Letra de la respuesta correcta: "A", "B", "C" o "D". */
    public String opGetRightAnswer() { return attRightAnswer; }
    public String opGetJustification() { return attJustification; }
    public String opGetBibliography() { return attBibliography; }
    public String opGetCompetence() { return attCompetence; }
    public String opGetTopic() { return attTopic; }
    public String opGetSubtopic() { return attSubtopic; }
    public String opGetDifficulty() { return attDifficulty; }
    public String opGetCreatedAt() { return attCreatedAt; }
    public String opGetUpdatedAt() { return attUpdatedAt; }

    public String opGetAuthorName() {
        return attAuthor == null ? "" : attAuthor.opGetNickName();
    }

    /* ===================== Setters encadenables ===================== */

    public clsQuestion opSetId(String prmValue) { attId = prmValue; return this; }
    public clsQuestion opSetAuthor(clsUser prmValue) { attAuthor = prmValue; return this; }
    public clsQuestion opSetState(clsQuestionState prmValue) { attState = prmValue; return this; }
    public clsQuestion opSetType(String prmValue) { attType = opClean(prmValue); return this; }
    public clsQuestion opSetContext(String prmValue) { attContext = opClean(prmValue); return this; }
    public clsQuestion opSetStatement(String prmValue) { attStatement = opClean(prmValue); return this; }
    public clsQuestion opSetOptions(String prmA, String prmB, String prmC, String prmD) {
        attOptions[0] = opClean(prmA);
        attOptions[1] = opClean(prmB);
        attOptions[2] = opClean(prmC);
        attOptions[3] = opClean(prmD);
        return this;
    }
    public clsQuestion opSetRightAnswer(String prmValue) { attRightAnswer = opClean(prmValue); return this; }
    public clsQuestion opSetJustification(String prmValue) { attJustification = opClean(prmValue); return this; }
    public clsQuestion opSetBibliography(String prmValue) { attBibliography = opClean(prmValue); return this; }
    public clsQuestion opSetCompetence(String prmValue) { attCompetence = opClean(prmValue); return this; }
    public clsQuestion opSetTopic(String prmValue) { attTopic = opClean(prmValue); return this; }
    public clsQuestion opSetSubtopic(String prmValue) { attSubtopic = opClean(prmValue); return this; }
    public clsQuestion opSetDifficulty(String prmValue) { attDifficulty = opClean(prmValue); return this; }
    public clsQuestion opSetCreatedAt(String prmValue) { attCreatedAt = opClean(prmValue); return this; }
    public clsQuestion opSetUpdatedAt(String prmValue) { attUpdatedAt = opClean(prmValue); return this; }

    private static String opClean(String prmValue) {
        return prmValue == null ? "" : prmValue.trim();
    }

    @Override
    public String toString() {
        return attStatement;
    }
}
