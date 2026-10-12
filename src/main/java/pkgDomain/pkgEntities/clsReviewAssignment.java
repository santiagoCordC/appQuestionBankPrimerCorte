package pkgDomain.pkgEntities;

/**
 * Asignación de un revisor a una pregunta (HU04).
 */
public class clsReviewAssignment {

    private final String attId;
    private final clsQuestion attQuestion;
    private final clsUser attReviewer;
    private final clsUser attAssignedBy;
    private final String attAssignedAt;

    public clsReviewAssignment(String prmId, clsQuestion prmQuestion, clsUser prmReviewer,
            clsUser prmAssignedBy, String prmAssignedAt) {
        attId = prmId;
        attQuestion = prmQuestion;
        attReviewer = prmReviewer;
        attAssignedBy = prmAssignedBy;
        attAssignedAt = prmAssignedAt;
    }

    public String opGetId() {
        return attId;
    }

    public clsQuestion opGetQuestion() {
        return attQuestion;
    }

    public clsUser opGetReviewer() {
        return attReviewer;
    }

    public clsUser opGetAssignedBy() {
        return attAssignedBy;
    }

    public String opGetAssignedAt() {
        return attAssignedAt;
    }
}
