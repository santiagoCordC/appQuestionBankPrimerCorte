package pkgServices.pkgPipeline.pkgFilters;

import java.util.ArrayList;
import java.util.List;
import pkgDomain.pkgEntities.clsQuestion;
import pkgServices.pkgPipeline.IFilter;
import pkgServices.pkgPipeline.clsPipeData;
import pkgUtil.clsText;

/**
 * Ninguna opción puede usar expresiones como "Todas las anteriores" o
 * "Ninguna de las anteriores" (lista configurable en app.properties).
 */
public class clsFilterForbiddenExpressions implements IFilter<clsQuestion> {

    private final List<String> attExpressions = new ArrayList<>();

    public clsFilterForbiddenExpressions(List<String> prmExpressions) {
        for (String varExpression : prmExpressions) {
            attExpressions.add(clsText.opNormalize(varExpression));
        }
    }

    @Override
    public void opExecute(clsPipeData<clsQuestion> prmData) {
        for (String varOption : prmData.opGetValue().opGetOptions()) {
            String varNormalized = clsText.opNormalize(varOption);
            for (String varExpression : attExpressions) {
                if (!varExpression.isEmpty() && varNormalized.contains(varExpression)) {
                    prmData.opAddError("Las opciones no pueden usar expresiones como \"Todas las anteriores\" "
                            + "o \"Ninguna de las anteriores\".");
                    return;
                }
            }
        }
    }
}
