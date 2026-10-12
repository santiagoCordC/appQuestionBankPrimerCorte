package pkgDomain.pkgEntities;

/**
 * Datos que diligencia una persona en el formulario de registro. Viajan por
 * la tubería de validación de usuarios antes de crear el {@link clsUser}.
 */
public class clsUserData {

    private String attFullName = "";
    private String attNickName = "";
    private String attEmail = "";
    private String attPassword = "";
    private String attPasswordConfirm = "";
    private enRole attRole;

    public clsUserData(String prmFullName, String prmNickName, String prmEmail, String prmPassword,
            String prmPasswordConfirm, enRole prmRole) {
        attFullName = opNotNull(prmFullName);
        attNickName = opNotNull(prmNickName);
        attEmail = opNotNull(prmEmail);
        attPassword = opNotNull(prmPassword);
        attPasswordConfirm = opNotNull(prmPasswordConfirm);
        attRole = prmRole;
    }

    public String opGetFullName() { return attFullName; }
    public String opGetNickName() { return attNickName; }
    public String opGetEmail() { return attEmail; }
    public String opGetPassword() { return attPassword; }
    public String opGetPasswordConfirm() { return attPasswordConfirm; }
    public enRole opGetRole() { return attRole; }
    public String opGetRoleName() { return attRole == null ? "" : attRole.name(); }

    /* Los filtros de normalización de la tubería usan estos setters. */
    public void opSetFullName(String prmValue) { attFullName = opNotNull(prmValue); }
    public void opSetNickName(String prmValue) { attNickName = opNotNull(prmValue); }
    public void opSetEmail(String prmValue) { attEmail = opNotNull(prmValue); }

    private static String opNotNull(String prmValue) {
        return prmValue == null ? "" : prmValue;
    }
}
