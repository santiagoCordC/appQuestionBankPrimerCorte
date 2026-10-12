package pkgServices.pkgPipeline.pkgFilters;

import java.util.function.Function;
import pkgServices.pkgPipeline.IFilter;
import pkgServices.pkgPipeline.clsPipeData;

/**
 * Filtro genérico: el campo debe tener al menos N caracteres.
 */
public class clsFilterMinLength<T> implements IFilter<T> {

    private final Function<T, String> attField;
    private final int attMin;
    private final String attMessage;

    public clsFilterMinLength(Function<T, String> prmField, int prmMin, String prmMessage) {
        attField = prmField;
        attMin = prmMin;
        attMessage = prmMessage;
    }

    @Override
    public void opExecute(clsPipeData<T> prmData) {
        String varValue = attField.apply(prmData.opGetValue());
        if (varValue == null || varValue.trim().length() < attMin) {
            prmData.opAddError(attMessage);
        }
    }
}
