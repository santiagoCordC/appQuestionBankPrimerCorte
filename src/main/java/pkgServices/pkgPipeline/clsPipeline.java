package pkgServices.pkgPipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Tubería: pasa el dato por cada filtro, en orden. Primero van los filtros
 * que transforman (normalizan el texto) y luego los que validan. Se pueden
 * agregar, quitar o reordenar filtros sin tocar los demás.
 *
 * @param <T> tipo del dato que procesa.
 */
public class clsPipeline<T> {

    private final List<IFilter<T>> attFilters = new ArrayList<>();

    /** Conecta un filtro al final de la tubería. */
    public clsPipeline<T> opAdd(IFilter<T> prmFilter) {
        attFilters.add(prmFilter);
        return this;
    }

    public clsPipeline<T> opAddAll(List<IFilter<T>> prmFilters) {
        attFilters.addAll(prmFilters);
        return this;
    }

    /** Hace pasar el dato por toda la tubería y devuelve el resultado con los errores encontrados. */
    public clsPipeData<T> opRun(T prmValue) {
        clsPipeData<T> varData = new clsPipeData<>(prmValue);
        for (IFilter<T> varFilter : attFilters) {
            varFilter.opExecute(varData);
        }
        return varData;
    }

    public int opSize() {
        return attFilters.size();
    }
}
