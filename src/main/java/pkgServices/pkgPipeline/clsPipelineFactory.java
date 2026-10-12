package pkgServices.pkgPipeline;

import java.util.List;
import java.util.stream.Collectors;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsUserData;
import pkgDomain.pkgEntities.enRole;
import pkgServices.clsConfig;
import pkgServices.pkgMicrokernel.pkgKernel.IQuestionPlugin;
import pkgServices.pkgMicrokernel.pkgKernel.clsQuestionMicrokernel;
import pkgServices.pkgPipeline.pkgFilters.clsFilterForbiddenExpressions;
import pkgServices.pkgPipeline.pkgFilters.clsFilterFourOptions;
import pkgServices.pkgPipeline.pkgFilters.clsFilterInList;
import pkgServices.pkgPipeline.pkgFilters.clsFilterMinLength;
import pkgServices.pkgPipeline.pkgFilters.clsFilterNormalizeQuestion;
import pkgServices.pkgPipeline.pkgFilters.clsFilterNormalizeUser;
import pkgServices.pkgPipeline.pkgFilters.clsFilterOptionFormat;
import pkgServices.pkgPipeline.pkgFilters.clsFilterPasswordsMatch;
import pkgServices.pkgPipeline.pkgFilters.clsFilterRegex;
import pkgServices.pkgPipeline.pkgFilters.clsFilterRequired;
import pkgServices.pkgPipeline.pkgFilters.clsFilterSingleQuestion;
import pkgServices.pkgPipeline.pkgFilters.clsFilterSingleRightAnswer;

/**
 * Arma las tuberías de la aplicación. Aquí queda en un solo lugar el orden
 * de los filtros:
 * <ul>
 * <li>Tubería de preguntas (validación estructural, HU01): normalizar →
 * campos obligatorios → catálogos → contexto → pregunta directa → opciones →
 * respuesta → expresiones prohibidas → formato → filtros del plugin del tipo.</li>
 * <li>Tubería de registro de usuarios: normalizar → obligatorios → formatos
 * → contraseña → rol.</li>
 * </ul>
 */
public class clsPipelineFactory {

    public static final String NICKNAME_REGEX = "^[a-z0-9._]{3,20}$";
    public static final String EMAIL_REGEX = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";
    /** Mínimo 8 caracteres, al menos una mayúscula, un número y un carácter especial. */
    public static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$";

    private final clsConfig attConfig;
    private final clsQuestionMicrokernel attKernel;

    public clsPipelineFactory(clsConfig prmConfig, clsQuestionMicrokernel prmKernel) {
        attConfig = prmConfig;
        attKernel = prmKernel;
    }

    /** Tubería de validación estructural para el tipo de pregunta indicado. */
    public clsPipeline<clsQuestion> opCreateQuestionPipeline(String prmType) {
        int varMinContext = attConfig.opGetContextMinLength();
        clsPipeline<clsQuestion> varPipeline = new clsPipeline<clsQuestion>()
                .opAdd(new clsFilterNormalizeQuestion())
                .opAdd(new clsFilterInList<>(clsQuestion::opGetType, attKernel.opGetCodes(), "Seleccione un tipo de pregunta válido."))
                .opAdd(new clsFilterRequired<>(clsQuestion::opGetStatement, "La pregunta directa es obligatoria."))
                .opAdd(new clsFilterRequired<>(clsQuestion::opGetJustification, "La justificación de la respuesta es obligatoria."))
                .opAdd(new clsFilterRequired<>(clsQuestion::opGetBibliography, "La bibliografía es obligatoria."))
                .opAdd(new clsFilterRequired<>(clsQuestion::opGetTopic, "El tema es obligatorio."))
                .opAdd(new clsFilterRequired<>(clsQuestion::opGetSubtopic, "El subtema es obligatorio."))
                .opAdd(new clsFilterInList<>(clsQuestion::opGetCompetence, attConfig.opGetCompetences(), "Seleccione una competencia válida."))
                .opAdd(new clsFilterInList<>(clsQuestion::opGetDifficulty, attConfig.opGetDifficulties(), "Seleccione un nivel de dificultad válido."))
                .opAdd(new clsFilterMinLength<>(clsQuestion::opGetContext, varMinContext,
                        "La pregunta debe tener un contexto (mínimo " + varMinContext + " caracteres)."))
                .opAdd(new clsFilterSingleQuestion())
                .opAdd(new clsFilterFourOptions())
                .opAdd(new clsFilterSingleRightAnswer())
                .opAdd(new clsFilterForbiddenExpressions(attConfig.opGetForbiddenExpressions()))
                .opAdd(new clsFilterOptionFormat(attConfig.opGetOptionMaxLength()));
        IQuestionPlugin varPlugin = attKernel.opGetPlugin(prmType);
        if (varPlugin != null) {
            varPipeline.opAddAll(varPlugin.opGetFilters(attConfig));
        }
        return varPipeline;
    }

    /** Tubería de validación del formulario de registro. */
    public static clsPipeline<clsUserData> opCreateUserPipeline(List<enRole> prmAllowedRoles) {
        List<String> varRoles = prmAllowedRoles.stream().map(enRole::name).collect(Collectors.toList());
        return new clsPipeline<clsUserData>()
                .opAdd(new clsFilterNormalizeUser())
                .opAdd(new clsFilterRequired<>(clsUserData::opGetFullName, "El nombre completo es obligatorio."))
                .opAdd(new clsFilterRegex<>(clsUserData::opGetNickName, NICKNAME_REGEX,
                        "El usuario debe tener entre 3 y 20 caracteres: letras, números, punto o guion bajo."))
                .opAdd(new clsFilterRegex<>(clsUserData::opGetEmail, EMAIL_REGEX, "Escriba un correo electrónico válido."))
                .opAdd(new clsFilterRegex<>(clsUserData::opGetPassword, PASSWORD_REGEX,
                        "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial."))
                .opAdd(new clsFilterPasswordsMatch())
                .opAdd(new clsFilterInList<>(clsUserData::opGetRoleName, varRoles, "Seleccione un rol válido."));
    }
}
