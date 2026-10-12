package pkgServices.pkgPipeline.pkgFilters;

import java.util.HashSet;
import java.util.Set;
import pkgDomain.pkgEntities.clsQuestion;
import pkgServices.pkgPipeline.IFilter;
import pkgServices.pkgPipeline.clsPipeData;
import pkgUtil.clsText;

/**
 * Deben diligenciarse las cuatro opciones (A-D) y deben ser distintas entre
 * sí (sin importar mayúsculas ni tildes).
 */
public class clsFilterFourOptions implements IFilter<clsQuestion> {

    @Override
    public void opExecute(clsPipeData<clsQuestion> prmData) {
        Set<String> varDistinct = new HashSet<>();
        for (String varOption : prmData.opGetValue().opGetOptions()) {
            if (clsText.opIsBlank(varOption)) {
                prmData.opAddError("Deben diligenciarse las cuatro opciones (A, B, C y D).");
                return;
            }
            varDistinct.add(clsText.opNormalize(varOption));
        }
        if (varDistinct.size() < 4) {
            prmData.opAddError("Las cuatro opciones deben ser diferentes entre sí.");
        }
    }
}
