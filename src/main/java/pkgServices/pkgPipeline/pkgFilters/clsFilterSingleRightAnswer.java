package pkgServices.pkgPipeline.pkgFilters;

import pkgDomain.pkgEntities.clsQuestion;
import pkgServices.pkgPipeline.IFilter;
import pkgServices.pkgPipeline.clsPipeData;

/**
 * Debe marcarse una única respuesta correcta: A, B, C o D.
 */
public class clsFilterSingleRightAnswer implements IFilter<clsQuestion> {

    @Override
    public void opExecute(clsPipeData<clsQuestion> prmData) {
        if (!clsQuestion.LETTERS.contains(prmData.opGetValue().opGetRightAnswer())) {
            prmData.opAddError("Marque una única respuesta correcta (A, B, C o D).");
        }
    }
}
