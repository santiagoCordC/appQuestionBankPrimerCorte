package pkgServices.pkgMicrokernel.pkgPlugins;

import java.util.List;
import pkgDomain.pkgEntities.clsQuestion;
import pkgServices.clsConfig;
import pkgServices.pkgMicrokernel.pkgKernel.IQuestionPlugin;
import pkgServices.pkgPipeline.IFilter;

/**
 * Plugin: selección múltiple con única respuesta (el tipo básico Saber Pro).
 * No agrega reglas a las generales.
 */
public class clsMultipleChoicePlugin implements IQuestionPlugin {

    @Override
    public String opGetCode() {
        return "SELECCION_MULTIPLE";
    }

    @Override
    public String opGetName() {
        return "Selección múltiple";
    }

    @Override
    public String opGetDescription() {
        return "Pregunta de selección múltiple con única respuesta.";
    }

    @Override
    public List<IFilter<clsQuestion>> opGetFilters(clsConfig prmConfig) {
        return List.of();
    }
}
