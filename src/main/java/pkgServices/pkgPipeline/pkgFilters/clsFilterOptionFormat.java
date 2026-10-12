package pkgServices.pkgPipeline.pkgFilters;

import java.util.List;
import pkgDomain.pkgEntities.clsQuestion;
import pkgServices.pkgPipeline.IFilter;
import pkgServices.pkgPipeline.clsPipeData;

/**
 * Longitud y estructura de las opciones:
 * <ul>
 * <li>ninguna opción supera la longitud máxima;</li>
 * <li>longitudes parecidas: la más larga no mide más del triple de la más
 * corta (las opciones de menos de 25 caracteres no se revisan, para no
 * castigar respuestas como "2" y "128");</li>
 * <li>estructura paralela: todas empiezan con mayúscula (o todas con
 * minúscula) y todas terminan en punto (o ninguna).</li>
 * </ul>
 */
public class clsFilterOptionFormat implements IFilter<clsQuestion> {

    private static final int BALANCE_FROM = 25;
    private static final int BALANCE_FACTOR = 3;

    private final int attMaxLength;

    public clsFilterOptionFormat(int prmMaxLength) {
        attMaxLength = prmMaxLength;
    }

    @Override
    public void opExecute(clsPipeData<clsQuestion> prmData) {
        List<String> varOptions = prmData.opGetValue().opGetOptions();
        if (varOptions.stream().anyMatch(String::isEmpty)) {
            return; // de las opciones vacías se encarga clsFilterFourOptions
        }
        int varMin = Integer.MAX_VALUE;
        int varMax = 0;
        int varUpper = 0;
        int varLower = 0;
        int varWithPeriod = 0;
        for (String varOption : varOptions) {
            varMin = Math.min(varMin, varOption.length());
            varMax = Math.max(varMax, varOption.length());
            char varFirst = varOption.charAt(0);
            if (Character.isLetter(varFirst)) {
                if (Character.isUpperCase(varFirst)) {
                    varUpper++;
                } else {
                    varLower++;
                }
            }
            if (varOption.endsWith(".")) {
                varWithPeriod++;
            }
        }
        if (varMax > attMaxLength) {
            prmData.opAddError("Cada opción debe tener máximo " + attMaxLength + " caracteres.");
        }
        if (varMax >= BALANCE_FROM && varMax > BALANCE_FACTOR * varMin) {
            prmData.opAddError("Las opciones deben tener longitudes parecidas (la más larga no puede medir más del triple de la más corta).");
        }
        if ((varUpper > 0 && varLower > 0) || (varWithPeriod > 0 && varWithPeriod < 4)) {
            prmData.opAddError("Las opciones deben tener la misma estructura: todas inician con mayúscula (o todas con minúscula) "
                    + "y todas terminan en punto (o ninguna).");
        }
    }
}
