package pkgPersistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsReviewAssignment;
import pkgDomain.pkgRepository.IAssignmentRepository;
import pkgDomain.pkgRepository.IQuestionRepository;
import pkgDomain.pkgRepository.IUserRepository;

/**
 * Asignaciones de revisores guardadas en SQLite.
 */
public class clsSqliteAssignmentRepository implements IAssignmentRepository {

    private final clsConnectionSQL attDb = clsConnectionSQL.opGetInstance();
    private final IUserRepository attUsers;
    private final IQuestionRepository attQuestions;

    public clsSqliteAssignmentRepository(IUserRepository prmUsers, IQuestionRepository prmQuestions) {
        attUsers = prmUsers;
        attQuestions = prmQuestions;
    }

    @Override
    public boolean opSave(clsReviewAssignment prmA) {
        String varSql = "INSERT INTO tbl_review_assignment (id, id_question, id_reviewer, id_assigned_by, assigned_at) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection varCon = attDb.opGetConnection(); PreparedStatement varPs = varCon.prepareStatement(varSql)) {
            varPs.setString(1, prmA.opGetId());
            varPs.setString(2, prmA.opGetQuestion().opGetId());
            varPs.setString(3, prmA.opGetReviewer().opGetId());
            varPs.setString(4, prmA.opGetAssignedBy() == null ? null : prmA.opGetAssignedBy().opGetId());
            varPs.setString(5, prmA.opGetAssignedAt());
            return varPs.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar la asignación: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<clsReviewAssignment> opFindByQuestion(String prmQuestionId) {
        List<clsReviewAssignment> varResult = new ArrayList<>();
        clsQuestion varQuestion = attQuestions.opFindById(prmQuestionId);
        String varSql = "SELECT * FROM tbl_review_assignment WHERE id_question = ? ORDER BY assigned_at";
        try (Connection varCon = attDb.opGetConnection(); PreparedStatement varPs = varCon.prepareStatement(varSql)) {
            varPs.setString(1, prmQuestionId);
            try (ResultSet varRs = varPs.executeQuery()) {
                while (varRs.next()) {
                    varResult.add(new clsReviewAssignment(varRs.getString("id"), varQuestion,
                            attUsers.opFindById(varRs.getString("id_reviewer")),
                            attUsers.opFindById(varRs.getString("id_assigned_by")),
                            varRs.getString("assigned_at")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar asignaciones: " + e.getMessage());
        }
        return varResult;
    }
}
