package pkgController;

import java.util.List;
import java.util.UUID;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgEntities.clsUserData;
import pkgDomain.pkgEntities.enRole;
import pkgDomain.pkgRepository.IUserRepository;
import pkgServices.clsPasswordHasher;
import pkgServices.pkgPipeline.clsPipeData;
import pkgServices.pkgPipeline.clsPipeline;
import pkgServices.pkgPipeline.clsPipelineFactory;
import pkgUtil.clsText;

/**
 * Registro de usuarios, inicio y cierre de sesión.
 */
public class clsAuthController {

    /**
     * Roles que una persona puede escoger al registrarse. Las cuentas de
     * administrador no se crean desde el formulario público.
     */
    public static final List<enRole> SELF_REGISTER_ROLES = List.of(enRole.AUTHOR, enRole.REVIEWER);

    private final IUserRepository attUsers;
    private final clsSession attSession;
    private final clsPipeline<clsUserData> attRegisterPipeline = clsPipelineFactory.opCreateUserPipeline(SELF_REGISTER_ROLES);

    public clsAuthController(IUserRepository prmUsers, clsSession prmSession) {
        attUsers = prmUsers;
        attSession = prmSession;
    }

    /* ===================== Registro ===================== */

    public List<enRole> opGetRegisterRoles() {
        return SELF_REGISTER_ROLES;
    }

    /**
     * Pasa los datos por la tubería de validación; si todo está bien, crea
     * el usuario con la contraseña cifrada (BCrypt).
     */
    public clsResult<clsUser> opRegister(clsUserData prmData) {
        clsPipeData<clsUserData> varResult = attRegisterPipeline.opRun(prmData);
        if (!varResult.opIsValid()) {
            return clsResult.opFail("Revise los datos del registro:", varResult.opGetErrors());
        }
        if (attUsers.opFindByNickName(prmData.opGetNickName()) != null) {
            return clsResult.opFail("El usuario \"" + prmData.opGetNickName() + "\" ya está registrado.");
        }
        clsUser varUser = new clsUser(UUID.randomUUID().toString(), prmData.opGetFullName(), prmData.opGetNickName(),
                prmData.opGetEmail(), clsPasswordHasher.opHash(prmData.opGetPassword()), prmData.opGetRole(), true);
        if (!attUsers.opSave(varUser)) {
            return clsResult.opFail("No se pudo guardar el usuario en la base de datos.");
        }
        return clsResult.opOk(varUser, "Usuario \"" + varUser.opGetNickName() + "\" registrado como "
                + varUser.opGetRole() + ". Ya puede iniciar sesión.");
    }

    /* ===================== Sesión ===================== */

    public clsResult<clsUser> opLogin(String prmNickName, String prmPassword) {
        if (clsText.opIsBlank(prmNickName) || clsText.opIsBlank(prmPassword)) {
            return clsResult.opFail("Ingrese usuario y contraseña.");
        }
        clsUser varUser = attUsers.opFindByNickName(prmNickName);
        if (varUser == null || !clsPasswordHasher.opCheck(prmPassword, varUser.opGetPasswordHash())) {
            return clsResult.opFail("Usuario o contraseña incorrectos.");
        }
        if (!varUser.opIsActive()) {
            return clsResult.opFail("Su usuario está inactivo. Comuníquese con el administrador.");
        }
        attSession.opStart(varUser);
        return clsResult.opOk(varUser, "Bienvenido(a), " + varUser.opGetFullName() + ".");
    }

    public void opLogout() {
        attSession.opEnd();
    }

    public clsSession opGetSession() {
        return attSession;
    }
}
