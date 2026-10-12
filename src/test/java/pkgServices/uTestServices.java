package pkgServices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.Test;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsReviewAssignment;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgEntities.enRole;
import pkgServices.pkgNotification.clsEmailNotifier;
import pkgTestDoubles.clsSamples;

/**
 * Pruebas de los servicios: configuración, contraseñas y notificador de correo.
 */
public class uTestServices {

    @Test
    public void configuracionLeeListasYUsaValoresPorDefecto() {
        clsConfig varConfig = clsSamples.opConfig();
        assertEquals(List.of("Diseño de software", "Lectura crítica"), varConfig.opGetCompetences());
        assertEquals(3, varConfig.opGetPageSize());
        clsConfig varEmpty = new clsConfig(new Properties());
        assertEquals(List.of("Baja", "Media", "Alta"), varEmpty.opGetDifficulties());
        assertEquals(5, varEmpty.opGetPageSize());
        assertEquals(465, varEmpty.opInt("correo.smtp.puerto", 465));
    }

    @Test
    public void contrasenasConBCrypt() {
        String varHash = clsPasswordHasher.opHash("Clave123*");
        assertNotEquals("Clave123*", varHash);
        assertTrue(clsPasswordHasher.opCheck("Clave123*", varHash));
        assertFalse(clsPasswordHasher.opCheck("otra", varHash));
        assertFalse(clsPasswordHasher.opCheck("Clave123*", "texto-plano"));
    }

    /** Remitente falso que guarda los correos en una lista. */
    private static class clsFakeSender implements pkgServices.pkgNotification.IEmailSender {
        final List<String[]> attSent = new ArrayList<>();

        @Override
        public boolean opSend(String prmTo, String prmSubject, String prmBody) {
            attSent.add(new String[]{prmTo, prmSubject, prmBody});
            return true;
        }
    }

    @Test
    public void notificadorEnviaCorreoAlRevisor() {
        clsFakeSender varSender = new clsFakeSender();
        clsUser varReviewer = clsSamples.opUser("rev", enRole.REVIEWER);
        clsQuestion varQ = clsSamples.opValidQuestion().opSetAuthor(clsSamples.opUser("aut", enRole.AUTHOR));
        new clsEmailNotifier(varSender).opOnReviewerAssigned(new clsReviewAssignment("x", varQ, varReviewer,
                clsSamples.opUser("adm", enRole.ADMIN), "2026-10-11 10:00:00"));
        assertEquals(1, varSender.attSent.size());
        assertEquals("rev@correo.com", varSender.attSent.get(0)[0]);
        assertTrue(varSender.attSent.get(0)[2].contains(varQ.opGetStatement()));
    }

    @Test
    public void noEnviaSiElRevisorNoTieneCorreo() {
        clsFakeSender varSender = new clsFakeSender();
        clsUser varSinCorreo = new clsUser("r", "Sin correo", "r", "", "h", enRole.REVIEWER, true);
        new clsEmailNotifier(varSender).opOnReviewerAssigned(new clsReviewAssignment("x", clsSamples.opValidQuestion(),
                varSinCorreo, clsSamples.opUser("adm", enRole.ADMIN), "2026-10-11 10:00:00"));
        assertTrue(varSender.attSent.isEmpty());
    }
}
