package pkgServices.pkgMicrokernel.pkgPlugins;

import java.util.List;
import pkgDomain.pkgEntities.clsQuestion;
import pkgServices.clsConfig;
import pkgServices.pkgMicrokernel.pkgKernel.IQuestionPlugin;
import pkgServices.pkgPipeline.IFilter;
import pkgServices.pkgPipeline.pkgFilters.clsFilterMinLength;

/**
 * Plugin: pregunta tipo caso. El contexto debe describir una situación
 * completa, así que agrega a la tubería un filtro de longitud mínima del
 * contexto (configurable con caso.contexto.minimo).
 */
public class clsCaseQuestionPlugin implements IQuestionPlugin {

    @Override
    public String opGetCode() {
        return "TIPO_CASO";
    }

    @Override
    public String opGetName() {
        return "Pregunta tipo caso";
    }

    @Override
    public String opGetDescription() {
        return "El contexto plantea un caso o situación real (mínimo de caracteres configurable).";
    }

    @Override
    public List<IFilter<clsQuestion>> opGetFilters(clsConfig prmConfig) {
        int varMin = prmConfig.opInt("caso.contexto.minimo", 150);
        return List.of(new clsFilterMinLength<>(clsQuestion::opGetContext, varMin,
                "Una pregunta tipo caso necesita un contexto de al menos " + varMin + " caracteres que describa el caso."));
    }
}
