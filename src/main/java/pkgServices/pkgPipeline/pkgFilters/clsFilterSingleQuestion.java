package pkgServices.pkgPipeline.pkgFilters;

import pkgDomain.pkgEntities.clsQuestion;
import pkgServices.pkgPipeline.IFilter;
import pkgServices.pkgPipeline.clsPipeData;

/**
 * Debe haber una única pregunta directa: el enunciado tiene exactamente un
 * signo de cierre "?".
 */
public class clsFilterSingleQuestion implements IFilter<clsQuestion> {

    @Override
    public void opExecute(clsPipeData<clsQuestion> prmData) {
        long varMarks = prmData.opGetValue().opGetStatement().chars().filter(varC -> varC == '?').count();
        if (varMarks != 1) {
            prmData.opAddError("Debe haber una única pregunta directa (exactamente un signo \"?\").");
        }
    }
}
