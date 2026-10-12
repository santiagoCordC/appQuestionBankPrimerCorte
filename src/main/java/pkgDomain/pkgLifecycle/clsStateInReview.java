package pkgDomain.pkgLifecycle;

/**
 * Estado "En revisión": tiene revisores asignados. Las transiciones a
 * Aprobada / Rechazada se agregan en el siguiente corte (nuevos estados y
 * métodos aquí, sin tocar la pregunta ni los controladores).
 */
public class clsStateInReview extends clsQuestionState {

    @Override
    public String opGetCode() {
        return "IN_REVIEW";
    }

    @Override
    public String opGetName() {
        return "En revisión";
    }
}
