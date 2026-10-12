package pkgController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgLifecycle.clsQuestionLifecycle;
import pkgDomain.pkgEntities.enRole;
import pkgServices.clsPasswordHasher;
import pkgDomain.pkgEntities.clsUserData;
import pkgServices.pkgMicrokernel.pkgKernel.clsQuestionMicrokernel;
import pkgServices.pkgPipeline.clsPipelineFactory;
import pkgTestDoubles.clsMemoryRepositories;
import pkgTestDoubles.clsSamples;

/**
 * Pruebas del inicio de sesión y de los datos de demostración.
 */
public class uTestAuthAndDemo {

    @Test
    public void loginCorrectoIncorrectoEInactivo() {
        clsMemoryRepositories.Users varUsers = new clsMemoryRepositories.Users();
        String varHash = clsPasswordHasher.opHash("Clave123*");
        varUsers.opSave(new clsUser("1", "Ana", "autor1", "a@x.com", varHash, enRole.AUTHOR, true));
        varUsers.opSave(new clsUser("2", "Inés", "inactivo", "i@x.com", varHash, enRole.AUTHOR, false));
        clsSession varSession = new clsSession();
        clsAuthController varAuth = new clsAuthController(varUsers, varSession);

        assertFalse(varAuth.opLogin("", "").opIsOk());
        assertFalse(varAuth.opLogin("autor1", "mala").opIsOk());
        assertFalse(varAuth.opLogin("nadie", "Clave123*").opIsOk());
        assertFalse(varAuth.opLogin("inactivo", "Clave123*").opIsOk());
        assertFalse(varSession.opIsActive());

        assertTrue(varAuth.opLogin("AUTOR1", "Clave123*").opIsOk());
        assertTrue(varSession.opHasRole(enRole.AUTHOR));
        varAuth.opLogout();
        assertNull(varSession.opGetUser());
    }

    @Test
    public void registroCreaUsuarioQuePuedeIniciarSesion() {
        clsMemoryRepositories.Users varUsers = new clsMemoryRepositories.Users();
        clsSession varSession = new clsSession();
        clsAuthController varAuth = new clsAuthController(varUsers, varSession);
        clsResult<clsUser> varResult = varAuth.opRegister(new clsUserData("Rubén Revisor", "Revisor9", "rev9@correo.com",
                "Clave123*", "Clave123*", enRole.REVIEWER));
        assertTrue(varResult.opGetFullMessage(), varResult.opIsOk());
        assertEquals("revisor9", varResult.opGetValue().opGetNickName());
        assertTrue("La contraseña se guarda cifrada", varResult.opGetValue().opGetPasswordHash().startsWith("$2a$"));
        assertTrue(varAuth.opLogin("revisor9", "Clave123*").opIsOk());
        assertTrue(varSession.opHasRole(enRole.REVIEWER));
    }

    @Test
    public void registroRechazaDatosInvalidosYUsuarioRepetido() {
        clsMemoryRepositories.Users varUsers = new clsMemoryRepositories.Users();
        clsAuthController varAuth = new clsAuthController(varUsers, new clsSession());
        clsResult<clsUser> varBad = varAuth.opRegister(new clsUserData("", "x", "malo", "123", "456", enRole.ADMIN));
        assertFalse(varBad.opIsOk());
        assertEquals(6, varBad.opGetErrors().size());
        assertTrue(varAuth.opRegister(new clsUserData("Ana", "ana", "a@b.co", "Clave123*", "Clave123*", enRole.AUTHOR)).opIsOk());
        assertFalse(varAuth.opRegister(new clsUserData("Ana 2", "ANA", "a2@b.co", "Clave123*", "Clave123*", enRole.AUTHOR)).opIsOk());
        assertEquals(1, varUsers.opCount());
    }

    @Test
    public void lasPreguntasDeDemostracionSonValidas() {
        clsPipelineFactory varFactory = new clsPipelineFactory(pkgServices.clsConfig.opLoadDefault(),
                clsQuestionMicrokernel.opLoadDefault());
        for (clsQuestion varQ : clsDemoDataSeeder.opGetDemoQuestions()) {
            assertEquals(varQ.opGetStatement(), java.util.List.of(),
                    varFactory.opCreateQuestionPipeline(varQ.opGetType()).opRun(varQ).opGetErrors());
        }
    }

    @Test
    public void sembradorCreaUsuariosYPreguntasSoloUnaVez() {
        clsMemoryRepositories.Users varUsers = new clsMemoryRepositories.Users();
        clsMemoryRepositories.Questions varQuestions = new clsMemoryRepositories.Questions();
        clsDemoDataSeeder varSeeder = new clsDemoDataSeeder(varUsers, varQuestions, clsSamples.opConfig());
        varSeeder.opSeed();
        assertEquals(5, varUsers.opCount());
        assertEquals(10, varQuestions.attItems.size());
        assertEquals(2, varUsers.opFindByRole(enRole.REVIEWER).size());
        assertTrue(varQuestions.attItems.stream().anyMatch(varQ -> varQ.opGetState() == clsQuestionLifecycle.PENDING_REVIEW));
        varSeeder.opSeed();
        assertEquals("No duplica", 10, varQuestions.attItems.size());
    }
}
