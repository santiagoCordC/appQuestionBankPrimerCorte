package pkgServices.pkgPipeline.pkgFilters;

import java.util.function.Function;
import pkgServices.pkgPipeline.IFilter;
import pkgServices.pkgPipeline.clsPipeData;
import pkgUtil.clsText;

/**
 * Filtro genérico: el campo indicado es obligatorio. Se reutiliza en la
 * tubería de preguntas y en la de usuarios.
 */
public class clsFilterRequired<T> implements IFilter<T> {

    private final Function<T, String> attField;
    private final String attMessage;

    public clsFilterRequired(Function<T, String> prmField, String prmMessage) {
        attField = prmField;
        attMessage = prmMessage;
    }

    @Override
    public void opExecute(clsPipeData<T> prmData) {
        if (clsText.opIsBlank(attField.apply(prmData.opGetValue()))) {
            prmData.opAddError(attMessage);
        }
    }
}
