package pkgServices.pkgMicrokernel.pkgKernel;

import java.util.List;
import pkgDomain.pkgEntities.clsQuestion;
import pkgServices.clsConfig;
import pkgServices.pkgPipeline.IFilter;

/**
 * Contrato que cumple cada plugin del microkernel: un tipo de pregunta.
 * Un plugin dice cómo se llama el tipo y qué filtros extra de validación
 * necesita; el núcleo los conecta a la tubería de validación.
 */
public interface IQuestionPlugin {

    /** Código que se guarda en la base de datos (p. ej. "SELECCION_MULTIPLE"). */
    String opGetCode();

    /** Nombre que ve el usuario. */
    String opGetName();

    /** Explicación corta para el autor. */
    String opGetDescription();

    /** Filtros de validación propios de este tipo (lista vacía si no tiene). */
    List<IFilter<clsQuestion>> opGetFilters(clsConfig prmConfig);
}
