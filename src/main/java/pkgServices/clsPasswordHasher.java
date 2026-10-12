package pkgServices;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Cifrado de contraseñas con BCrypt (nunca se guardan en texto plano).
 */
public final class clsPasswordHasher {

    private clsPasswordHasher() {
    }

    public static String opHash(String prmPlainPassword) {
        return BCrypt.hashpw(prmPlainPassword, BCrypt.gensalt(10));
    }

    public static boolean opCheck(String prmPlainPassword, String prmHash) {
        if (prmPlainPassword == null || prmHash == null || !prmHash.startsWith("$2a$")) {
            return false;
        }
        try {
            return BCrypt.checkpw(prmPlainPassword, prmHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
