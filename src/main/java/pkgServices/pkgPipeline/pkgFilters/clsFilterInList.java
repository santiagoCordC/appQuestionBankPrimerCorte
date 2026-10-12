package pkgServices.pkgPipeline.pkgFilters;

import java.util.List;
import java.util.function.Function;
import pkgServices.pkgPipeline.IFilter;
import pkgServices.pkgPipeline.clsPipeData;

/**
 * Filtro genérico: el valor del campo debe pertenecer a una lista
 * (competencias, dificultades, tipos de pregunta, roles permitidos...).
 */
public class clsFilterInList<T> implements IFilter<T> {

    private final Function<T, String> attField;
    private final List<String> attAllowed;
    private final String attMessage;

    public clsFilterInList(Function<T, String> prmField, List<String> prmAllowed, String prmMessage) {
        attField = prmField;
        attAllowed = List.copyOf(prmAllowed);
        attMessage = prmMessage;
    }

    @Override
    public void opExecute(clsPipeData<T> prmData) {
        if (!attAllowed.contains(attField.apply(prmData.opGetValue()))) {
            prmData.opAddError(attMessage);
        }
    }
}
