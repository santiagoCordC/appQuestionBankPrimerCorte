package pkgDomain.pkgLifecycle;

import pkgDomain.pkgEntities.clsQuestion;

/**
 * Estado "Borrador": la pregunta se puede editar y enviar a revisión.
 */
public class clsStateDraft extends clsQuestionState {

    @Override
    public String opGetCode() {
        return "DRAFT";
    }

    @Override
    public String opGetName() {
        return "Borrador";
    }

    @Override
    public boolean opIsEditable() {
        return true;
    }

    @Override
    public boolean opCanSubmitForReview() {
        return true;
    }

    @Override
    public void opSubmitForReview(clsQuestion prmQuestion) {
        prmQuestion.opSetState(clsQuestionLifecycle.PENDING_REVIEW);
    }
}
