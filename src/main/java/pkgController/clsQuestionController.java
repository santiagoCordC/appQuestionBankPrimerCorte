package pkgController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgEntities.enRole;
import pkgDomain.pkgRepository.IQuestionRepository;
import pkgDomain.pkgRepository.clsPage;
import pkgDomain.pkgRepository.clsSearchCriteria;
import pkgDomain.pkgLifecycle.clsQuestionLifecycle;
import pkgDomain.pkgLifecycle.clsQuestionState;
import pkgServices.clsConfig;
import pkgServices.pkgMicrokernel.pkgKernel.IQuestionPlugin;
import pkgServices.pkgMicrokernel.pkgKernel.clsQuestionMicrokernel;
import pkgServices.pkgPipeline.clsPipelineFactory;

/**
 * Casos de uso del autor de preguntas:
 * <ul>
 * <li>HU01: crear (y editar en Borrador) preguntas con validación estructural.</li>
 * <li>HU02: enviar una pregunta de Borrador a Pendiente de revisión.</li>
 * <li>HU03: listar sus preguntas con filtros y paginación.</li>
 * </ul>
 * La validación estructural pasa la pregunta por una tubería de filtros
 * ({@link clsPipelineFactory}); los tipos de pregunta vienen de los plugins
 * del microkernel. Recibe sus dependencias por el constructor, así en las pruebas se le pasan
 * repositorios en memoria.
 */
public class clsQuestionController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IQuestionRepository attQuestions;
    private final clsQuestionMicrokernel attKernel;
    private final clsPipelineFactory attPipelines;
    private final clsConfig attConfig;
    private final clsSession attSession;

    public clsQuestionController(IQuestionRepository prmQuestions, clsQuestionMicrokernel prmKernel,
            clsConfig prmConfig, clsSession prmSession) {
        attQuestions = prmQuestions;
        attKernel = prmKernel;
        attPipelines = new clsPipelineFactory(prmConfig, prmKernel);
        attConfig = prmConfig;
        attSession = prmSession;
    }

    /* ===================== Catálogos para la vista ===================== */

    public List<String> opGetCompetences() {
        return attConfig.opGetCompetences();
    }

    public List<String> opGetDifficulties() {
        return attConfig.opGetDifficulties();
    }

    /** Tipos de pregunta = plugins cargados por el microkernel. */
    public List<IQuestionPlugin> opGetTypes() {
        return attKernel.opGetPlugins();
    }

    public String opGetTypeName(String prmCode) {
        return attKernel.opGetName(prmCode);
    }

    public List<clsQuestionState> opGetStates() {
        return clsQuestionLifecycle.opGetAll();
    }

    public int opGetPageSize() {
        return attConfig.opGetPageSize();
    }

    /* ===================== HU01: crear y editar ===================== */

    /**
     * Validación estructural: pasa la pregunta por la tubería de filtros de
     * su tipo y devuelve todos los errores a la vez (lista vacía = válida).
     */
    public List<String> opValidate(clsQuestion prmForm) {
        return attPipelines.opCreateQuestionPipeline(prmForm.opGetType()).opRun(prmForm).opGetErrors();
    }

    public clsResult<clsQuestion> opCreate(clsQuestion prmForm) {
        if (!attSession.opHasRole(enRole.AUTHOR)) {
            return clsResult.opFail("Solo un autor de preguntas puede crear preguntas.");
        }
        List<String> varErrors = opValidate(prmForm);
        if (!varErrors.isEmpty()) {
            return clsResult.opFail("La pregunta no cumple la validación estructural:", varErrors);
        }
        String varNow = opNow();
        clsQuestion varQuestion = new clsQuestion()
                .opSetId(UUID.randomUUID().toString())
                .opSetAuthor(attSession.opGetUser())
                .opSetState(clsQuestionLifecycle.DRAFT)
                .opSetCreatedAt(varNow)
                .opSetUpdatedAt(varNow);
        varQuestion.opCopyContentFrom(prmForm);
        if (!attQuestions.opSave(varQuestion)) {
            return clsResult.opFail("No se pudo guardar la pregunta en la base de datos.");
        }
        return clsResult.opOk(varQuestion, "Pregunta guardada en estado Borrador.");
    }

    /** Solo el autor puede editar su pregunta y solo mientras esté en Borrador. */
    public boolean opCanEdit(clsQuestion prmQuestion) {
        return prmQuestion != null && prmQuestion.opIsEditable() && prmQuestion.opIsAuthor(attSession.opGetUser());
    }

    public clsResult<clsQuestion> opUpdate(String prmQuestionId, clsQuestion prmForm) {
        clsQuestion varQuestion = attQuestions.opFindById(prmQuestionId);
        if (varQuestion == null) {
            return clsResult.opFail("La pregunta no existe.");
        }
        if (!opCanEdit(varQuestion)) {
            return clsResult.opFail("Solo el autor puede modificar la pregunta y solo mientras esté en Borrador.");
        }
        List<String> varErrors = opValidate(prmForm);
        if (!varErrors.isEmpty()) {
            return clsResult.opFail("La pregunta no cumple la validación estructural:", varErrors);
        }
        varQuestion.opCopyContentFrom(prmForm);
        varQuestion.opSetUpdatedAt(opNow());
        if (!attQuestions.opUpdate(varQuestion)) {
            return clsResult.opFail("No se pudo actualizar la pregunta en la base de datos.");
        }
        return clsResult.opOk(varQuestion, "Pregunta actualizada.");
    }

    /* ===================== HU02: enviar a revisión ===================== */

    public clsResult<clsQuestion> opSubmitForReview(String prmQuestionId) {
        clsQuestion varQuestion = attQuestions.opFindById(prmQuestionId);
        if (varQuestion == null) {
            return clsResult.opFail("La pregunta no existe.");
        }
        if (!varQuestion.opIsAuthor(attSession.opGetUser())) {
            return clsResult.opFail("Solo el autor puede enviar su pregunta a revisión.");
        }
        if (!varQuestion.opGetState().opCanSubmitForReview()) {
            return clsResult.opFail("Solo se envían a revisión preguntas en Borrador (estado actual: "
                    + varQuestion.opGetState() + ").");
        }
        List<String> varErrors = opValidate(varQuestion);
        if (!varErrors.isEmpty()) {
            return clsResult.opFail("Corrija la pregunta antes de enviarla a revisión:", varErrors);
        }
        varQuestion.opSubmitForReview();
        varQuestion.opSetUpdatedAt(opNow());
        if (!attQuestions.opUpdate(varQuestion)) {
            return clsResult.opFail("No se pudo guardar el cambio de estado.");
        }
        return clsResult.opOk(varQuestion, "La pregunta quedó \"" + varQuestion.opGetState()
                + "\". El administrador le asignará revisores.");
    }

    /* ===================== HU03: listar mis preguntas ===================== */

    /**
     * Preguntas del autor que inició sesión que cumplen el filtro, de la
     * modificada más recientemente a la más antigua, cortadas en páginas.
     */
    public clsResult<clsPage<clsQuestion>> opListMine(clsSearchCriteria prmFilter, int prmPage) {
        clsUser varUser = attSession.opGetUser();
        if (varUser == null) {
            return clsResult.opFail("Debe iniciar sesión.");
        }
        clsSearchCriteria varFilter = (prmFilter == null ? new clsSearchCriteria() : prmFilter).opAuthor(varUser.opGetId());
        List<clsQuestion> varMine = attQuestions.opFindAll().stream()
                .filter(varFilter::opMatches)
                .sorted(Comparator.comparing(clsQuestion::opGetUpdatedAt).reversed())
                .collect(Collectors.toList());
        return clsResult.opOk(clsPage.opOf(varMine, prmPage, opGetPageSize()), "");
    }

    static String opNow() {
        return LocalDateTime.now().format(DATE_FORMAT);
    }
}
