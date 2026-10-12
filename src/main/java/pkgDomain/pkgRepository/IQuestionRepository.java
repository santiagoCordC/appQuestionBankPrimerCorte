package pkgDomain.pkgRepository;

import java.util.List;
import pkgDomain.pkgEntities.clsQuestion;

/**
 * Acceso a las preguntas.
 */
public interface IQuestionRepository {

    /** Inserta una pregunta nueva. */
    boolean opSave(clsQuestion prmQuestion);

    /** Guarda el contenido y el estado de una pregunta existente. */
    boolean opUpdate(clsQuestion prmQuestion);

    clsQuestion opFindById(String prmId);

    List<clsQuestion> opFindAll();
}
