package pkgServices.pkgPipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo que viaja por la tubería: el dato que se está procesando y los errores
 * que los filtros le van agregando.
 *
 * @param <T> tipo del dato (pregunta, datos de registro...).
 */
public class clsPipeData<T> {

    private final T attValue;
    private final List<String> attErrors = new ArrayList<>();

    public clsPipeData(T prmValue) {
        attValue = prmValue;
    }

    public T opGetValue() {
        return attValue;
    }

    public void opAddError(String prmError) {
        attErrors.add(prmError);
    }

    public List<String> opGetErrors() {
        return List.copyOf(attErrors);
    }

    public boolean opIsValid() {
        return attErrors.isEmpty();
    }
}
