package pkgPersistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgLifecycle.clsQuestionLifecycle;
import pkgDomain.pkgRepository.IQuestionRepository;
import pkgDomain.pkgRepository.IUserRepository;

/**
 * Preguntas guardadas en SQLite. No hay DELETE: las preguntas no se borran.
 */
public class clsSqliteQuestionRepository implements IQuestionRepository {

    private final clsConnectionSQL attDb = clsConnectionSQL.opGetInstance();
    private final IUserRepository attUsers;

    /** @param prmUsers para enlazar cada pregunta con su autor. */
    public clsSqliteQuestionRepository(IUserRepository prmUsers) {
        attUsers = prmUsers;
    }

    @Override
    public boolean opSave(clsQuestion prmQ) {
        String varSql = "INSERT INTO tbl_question (state, type, context, statement, option_a, option_b, option_c, option_d, "
                + "right_answer, justification, bibliography, competence, topic, subtopic, difficulty, updated_at, "
                + "created_at, id_author, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return opWrite(varSql, prmQ, true);
    }

    @Override
    public boolean opUpdate(clsQuestion prmQ) {
        String varSql = "UPDATE tbl_question SET state = ?, type = ?, context = ?, statement = ?, option_a = ?, option_b = ?, "
                + "option_c = ?, option_d = ?, right_answer = ?, justification = ?, bibliography = ?, competence = ?, "
                + "topic = ?, subtopic = ?, difficulty = ?, updated_at = ? WHERE id = ?";
        return opWrite(varSql, prmQ, false);
    }

    private boolean opWrite(String prmSql, clsQuestion prmQ, boolean prmInsert) {
        try (Connection varCon = attDb.opGetConnection(); PreparedStatement varPs = varCon.prepareStatement(prmSql)) {
            List<String> varOptions = prmQ.opGetOptions();
            int varI = 1;
            varPs.setString(varI++, prmQ.opGetState().opGetCode());
            varPs.setString(varI++, prmQ.opGetType());
            varPs.setString(varI++, prmQ.opGetContext());
            varPs.setString(varI++, prmQ.opGetStatement());
            for (String varOption : varOptions) {
                varPs.setString(varI++, varOption);
            }
            varPs.setString(varI++, prmQ.opGetRightAnswer());
            varPs.setString(varI++, prmQ.opGetJustification());
            varPs.setString(varI++, prmQ.opGetBibliography());
            varPs.setString(varI++, prmQ.opGetCompetence());
            varPs.setString(varI++, prmQ.opGetTopic());
            varPs.setString(varI++, prmQ.opGetSubtopic());
            varPs.setString(varI++, prmQ.opGetDifficulty());
            varPs.setString(varI++, prmQ.opGetUpdatedAt());
            if (prmInsert) {
                varPs.setString(varI++, prmQ.opGetCreatedAt());
                varPs.setString(varI++, prmQ.opGetAuthor().opGetId());
            }
            varPs.setString(varI, prmQ.opGetId());
            return varPs.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar la pregunta: " + e.getMessage());
            return false;
        }
    }

    @Override
    public clsQuestion opFindById(String prmId) {
        List<clsQuestion> varResult = opQuery("SELECT * FROM tbl_question WHERE id = ?", prmId);
        return varResult.isEmpty() ? null : varResult.get(0);
    }

    @Override
    public List<clsQuestion> opFindAll() {
        return opQuery("SELECT * FROM tbl_question ORDER BY updated_at DESC");
    }

    private List<clsQuestion> opQuery(String prmSql, String... prmParams) {
        List<clsQuestion> varResult = new ArrayList<>();
        try (Connection varCon = attDb.opGetConnection(); PreparedStatement varPs = varCon.prepareStatement(prmSql)) {
            for (int varI = 0; varI < prmParams.length; varI++) {
                varPs.setString(varI + 1, prmParams[varI]);
            }
            try (ResultSet varRs = varPs.executeQuery()) {
                while (varRs.next()) {
                    varResult.add(new clsQuestion()
                            .opSetId(varRs.getString("id"))
                            .opSetAuthor(attUsers.opFindById(varRs.getString("id_author")))
                            .opSetState(clsQuestionLifecycle.opFromCode(varRs.getString("state")))
                            .opSetType(varRs.getString("type"))
                            .opSetContext(varRs.getString("context"))
                            .opSetStatement(varRs.getString("statement"))
                            .opSetOptions(varRs.getString("option_a"), varRs.getString("option_b"),
                                    varRs.getString("option_c"), varRs.getString("option_d"))
                            .opSetRightAnswer(varRs.getString("right_answer"))
                            .opSetJustification(varRs.getString("justification"))
                            .opSetBibliography(varRs.getString("bibliography"))
                            .opSetCompetence(varRs.getString("competence"))
                            .opSetTopic(varRs.getString("topic"))
                            .opSetSubtopic(varRs.getString("subtopic"))
                            .opSetDifficulty(varRs.getString("difficulty"))
                            .opSetCreatedAt(varRs.getString("created_at"))
                            .opSetUpdatedAt(varRs.getString("updated_at")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar preguntas: " + e.getMessage());
        }
        return varResult;
    }
}
