package pkgDomain.pkgEntities;

/**
 * Usuario del sistema. El correo se usa para notificar a los revisores (HU04).
 */
public class clsUser {

    private final String attId;
    private final String attFullName;
    private final String attNickName;
    private final String attEmail;
    private final String attPasswordHash;
    private final enRole attRole;
    private boolean attActive;

    public clsUser(String prmId, String prmFullName, String prmNickName, String prmEmail,
            String prmPasswordHash, enRole prmRole, boolean prmActive) {
        attId = prmId;
        attFullName = prmFullName;
        attNickName = prmNickName;
        attEmail = prmEmail;
        attPasswordHash = prmPasswordHash;
        attRole = prmRole;
        attActive = prmActive;
    }

    public String opGetId() {
        return attId;
    }

    public String opGetFullName() {
        return attFullName;
    }

    public String opGetNickName() {
        return attNickName;
    }

    public String opGetEmail() {
        return attEmail;
    }

    public String opGetPasswordHash() {
        return attPasswordHash;
    }

    public enRole opGetRole() {
        return attRole;
    }

    public boolean opIsActive() {
        return attActive;
    }

    public void opSetActive(boolean prmActive) {
        attActive = prmActive;
    }

    public boolean opHasRole(enRole prmRole) {
        return attRole == prmRole;
    }

    @Override
    public String toString() {
        return attFullName + " (" + attNickName + ")";
    }
}
