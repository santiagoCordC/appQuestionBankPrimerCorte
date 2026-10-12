package pkgServices.pkgPipeline.pkgFilters;

import java.util.List;
import pkgDomain.pkgEntities.clsQuestion;
import pkgServices.pkgPipeline.IFilter;
import pkgServices.pkgPipeline.clsPipeData;

/**
 * Primer filtro de la tubería de preguntas (transformación): quita los
 * espacios repetidos de la pregunta directa, las opciones, el tema y el
 * subtema, para que los filtros siguientes comparen textos limpios.
 */
public class clsFilterNormalizeQuestion implements IFilter<clsQuestion> {

    @Override
    public void opExecute(clsPipeData<clsQuestion> prmData) {
        clsQuestion varQ = prmData.opGetValue();
        List<String> varOptions = varQ.opGetOptions();
        varQ.opSetStatement(opClean(varQ.opGetStatement()))
                .opSetTopic(opClean(varQ.opGetTopic()))
                .opSetSubtopic(opClean(varQ.opGetSubtopic()))
                .opSetOptions(opClean(varOptions.get(0)), opClean(varOptions.get(1)),
                        opClean(varOptions.get(2)), opClean(varOptions.get(3)));
    }

    private static String opClean(String prmValue) {
        return prmValue == null ? "" : prmValue.replaceAll("[ \\t]+", " ").trim();
    }
}
