package pkgServices.pkgPipeline.pkgFilters;

import java.util.function.Function;
import java.util.regex.Pattern;
import pkgServices.pkgPipeline.IFilter;
import pkgServices.pkgPipeline.clsPipeData;

/**
 * Filtro genérico: el campo debe cumplir una expresión regular (formato del
 * correo, fortaleza de la contraseña...).
 */
public class clsFilterRegex<T> implements IFilter<T> {

    private final Function<T, String> attField;
    private final Pattern attPattern;
    private final String attMessage;

    public clsFilterRegex(Function<T, String> prmField, String prmRegex, String prmMessage) {
        attField = prmField;
        attPattern = Pattern.compile(prmRegex);
        attMessage = prmMessage;
    }

    @Override
    public void opExecute(clsPipeData<T> prmData) {
        String varValue = attField.apply(prmData.opGetValue());
        if (varValue == null || !attPattern.matcher(varValue).matches()) {
            prmData.opAddError(attMessage);
        }
    }
}
