package pkgDomain.pkgLifecycle;

import pkgDomain.pkgEntities.clsQuestion;

/**
 * Estado "Pendiente de revisión": ya no se edita; espera a que el
 * administrador le asigne revisores.
 */
public class clsStatePendingReview extends clsQuestionState {

    @Override
    public String opGetCode() {
        return "PENDING_REVIEW";
    }

    @Override
    public String opGetName() {
        return "Pendiente de revisión";
    }

    @Override
    public boolean opCanAssignReviewers() {
        return true;
    }

    @Override
    public void opStartReview(clsQuestion prmQuestion) {
        prmQuestion.opSetState(clsQuestionLifecycle.IN_REVIEW);
    }
}
