package pkgDomain.pkgRepository;

import java.util.List;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgEntities.enRole;

/**
 * Acceso a los usuarios. La lógica depende de esta interfaz y no de SQLite
 * (principio de inversión de dependencias); en las pruebas se usa una
 * implementación en memoria.
 */
public interface IUserRepository {

    boolean opSave(clsUser prmUser);

    clsUser opFindById(String prmId);

    clsUser opFindByNickName(String prmNickName);

    List<clsUser> opFindByRole(enRole prmRole);

    int opCount();
}
