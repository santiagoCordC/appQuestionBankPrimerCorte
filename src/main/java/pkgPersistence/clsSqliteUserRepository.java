package pkgPersistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgEntities.enRole;
import pkgDomain.pkgRepository.IUserRepository;

/**
 * Usuarios guardados en SQLite.
 */
public class clsSqliteUserRepository implements IUserRepository {

    private static final String SELECT = "SELECT id, full_name, nickname, email, password_hash, role, active FROM tbl_user ";

    private final clsConnectionSQL attDb = clsConnectionSQL.opGetInstance();

    @Override
    public boolean opSave(clsUser prmUser) {
        String varSql = "INSERT INTO tbl_user (id, full_name, nickname, email, password_hash, role, active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection varCon = attDb.opGetConnection(); PreparedStatement varPs = varCon.prepareStatement(varSql)) {
            varPs.setString(1, prmUser.opGetId());
            varPs.setString(2, prmUser.opGetFullName());
            varPs.setString(3, prmUser.opGetNickName());
            varPs.setString(4, prmUser.opGetEmail());
            varPs.setString(5, prmUser.opGetPasswordHash());
            varPs.setString(6, prmUser.opGetRole().name());
            varPs.setInt(7, prmUser.opIsActive() ? 1 : 0);
            return varPs.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar el usuario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public clsUser opFindById(String prmId) {
        List<clsUser> varUsers = opQuery(SELECT + "WHERE id = ?", prmId);
        return varUsers.isEmpty() ? null : varUsers.get(0);
    }

    @Override
    public clsUser opFindByNickName(String prmNickName) {
        List<clsUser> varUsers = opQuery(SELECT + "WHERE lower(nickname) = lower(?)",
                prmNickName == null ? "" : prmNickName.trim());
        return varUsers.isEmpty() ? null : varUsers.get(0);
    }

    @Override
    public List<clsUser> opFindByRole(enRole prmRole) {
        return opQuery(SELECT + "WHERE role = ? ORDER BY full_name", prmRole.name());
    }

    @Override
    public int opCount() {
        return opQuery(SELECT).size();
    }

    private List<clsUser> opQuery(String prmSql, String... prmParams) {
        List<clsUser> varResult = new ArrayList<>();
        try (Connection varCon = attDb.opGetConnection(); PreparedStatement varPs = varCon.prepareStatement(prmSql)) {
            for (int varI = 0; varI < prmParams.length; varI++) {
                varPs.setString(varI + 1, prmParams[varI]);
            }
            try (ResultSet varRs = varPs.executeQuery()) {
                while (varRs.next()) {
                    varResult.add(new clsUser(varRs.getString("id"), varRs.getString("full_name"),
                            varRs.getString("nickname"), varRs.getString("email"), varRs.getString("password_hash"),
                            enRole.valueOf(varRs.getString("role")), varRs.getInt("active") == 1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar usuarios: " + e.getMessage());
        }
        return varResult;
    }
}
