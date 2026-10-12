package pkgTestDoubles;

import java.util.Properties;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgEntities.enRole;
import pkgServices.clsConfig;
import pkgServices.pkgMicrokernel.pkgKernel.clsQuestionMicrokernel;
import pkgServices.pkgMicrokernel.pkgPlugins.clsCaseQuestionPlugin;
import pkgServices.pkgMicrokernel.pkgPlugins.clsMultipleChoicePlugin;

/**
 * Datos de prueba reutilizables.
 */
public final class clsSamples {

    private clsSamples() {
    }

    /** Pregunta que cumple todas las reglas de la validación estructural. */
    public static clsQuestion opValidQuestion() {
        return new clsQuestion()
                .opSetType("SELECCION_MULTIPLE")
                .opSetContext("Un equipo desarrolla una aplicación de escritorio por capas.")
                .opSetStatement("¿Qué patrón separa la vista del modelo?")
                .opSetOptions("MVC", "Singleton", "Observer", "Builder")
                .opSetRightAnswer("A")
                .opSetJustification("MVC separa modelo, vista y controlador.")
                .opSetBibliography("Gamma et al. (1994). Design Patterns.")
                .opSetCompetence("Diseño de software")
                .opSetTopic("Arquitectura")
                .opSetSubtopic("Estilos")
                .opSetDifficulty("Media");
    }

    public static clsUser opUser(String prmId, enRole prmRole) {
        return new clsUser(prmId, "Usuario " + prmId, prmId, prmId + "@correo.com", "hash", prmRole, true);
    }

    /** Microkernel con los dos plugins registrados a mano (sin reflexión). */
    public static clsQuestionMicrokernel opKernel() {
        clsQuestionMicrokernel varKernel = new clsQuestionMicrokernel();
        varKernel.opRegister(new clsMultipleChoicePlugin());
        varKernel.opRegister(new clsCaseQuestionPlugin());
        return varKernel;
    }

    /** Configuración fija para que las pruebas no dependan de app.properties. */
    public static clsConfig opConfig() {
        Properties varP = new Properties();
        varP.setProperty("competencias", "Diseño de software;Lectura crítica");
        varP.setProperty("dificultades", "Baja;Media;Alta");
        varP.setProperty("expresiones.prohibidas", "todas las anteriores;ninguna de las anteriores");
        varP.setProperty("contexto.longitud.minima", "10");
        varP.setProperty("opcion.longitud.maxima", "100");
        varP.setProperty("pagina.tamano", "3");
        varP.setProperty("caso.contexto.minimo", "80");
        return new clsConfig(varP);
    }
}
