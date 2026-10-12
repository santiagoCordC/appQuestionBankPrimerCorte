package pkgController;

import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgEntities.enRole;

/**
 * Usuario que inició sesión. La comparten los controladores para saber
 * quién está haciendo cada acción y con qué rol.
 */
public class clsSession {

    private clsUser attUser;

    public void opStart(clsUser prmUser) {
        attUser = prmUser;
    }

    public void opEnd() {
        attUser = null;
    }

    public clsUser opGetUser() {
        return attUser;
    }

    public boolean opIsActive() {
        return attUser != null;
    }

    public boolean opHasRole(enRole prmRole) {
        return attUser != null && attUser.opHasRole(prmRole);
    }
}
