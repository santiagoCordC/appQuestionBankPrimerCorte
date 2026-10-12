package pkgDomain.pkgLifecycle;

import java.util.List;

/**
 * Catálogo de los estados del ciclo de vida. Los estados no guardan datos
 * propios, así que basta una instancia de cada uno.
 * <pre>
 * Borrador ──(autor la envía)──► Pendiente de revisión ──(admin asigna revisores)──► En revisión
 * </pre>
 */
public final class clsQuestionLifecycle {

    public static final clsQuestionState DRAFT = new clsStateDraft();
    public static final clsQuestionState PENDING_REVIEW = new clsStatePendingReview();
    public static final clsQuestionState IN_REVIEW = new clsStateInReview();

    private static final List<clsQuestionState> ALL = List.of(DRAFT, PENDING_REVIEW, IN_REVIEW);

    private clsQuestionLifecycle() {
    }

    public static List<clsQuestionState> opGetAll() {
        return ALL;
    }

    /** Estado a partir del código guardado en la base de datos. */
    public static clsQuestionState opFromCode(String prmCode) {
        for (clsQuestionState varState : ALL) {
            if (varState.opGetCode().equals(prmCode)) {
                return varState;
            }
        }
        throw new IllegalArgumentException("Estado desconocido: " + prmCode);
    }
}
