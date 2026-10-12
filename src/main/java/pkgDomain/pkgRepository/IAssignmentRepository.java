package pkgDomain.pkgRepository;

import java.util.List;
import pkgDomain.pkgEntities.clsReviewAssignment;

/**
 * Acceso a las asignaciones de revisores.
 */
public interface IAssignmentRepository {

    boolean opSave(clsReviewAssignment prmAssignment);

    List<clsReviewAssignment> opFindByQuestion(String prmQuestionId);
}
