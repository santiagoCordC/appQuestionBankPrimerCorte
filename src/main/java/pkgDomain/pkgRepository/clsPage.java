package pkgDomain.pkgRepository;

import java.util.List;

/**
 * Una página de resultados (HU03: paginación del listado).
 *
 * @param <T> tipo de los elementos.
 */
public class clsPage<T> {

    private final List<T> attItems;
    private final int attNumber;
    private final int attTotalPages;
    private final int attTotalItems;

    private clsPage(List<T> prmItems, int prmNumber, int prmTotalPages, int prmTotalItems) {
        attItems = prmItems;
        attNumber = prmNumber;
        attTotalPages = prmTotalPages;
        attTotalItems = prmTotalItems;
    }

    /**
     * Corta la lista completa y devuelve la página pedida (empieza en 1).
     * Si el número está fuera de rango se ajusta a la primera o la última.
     */
    public static <T> clsPage<T> opOf(List<T> prmAll, int prmNumber, int prmSize) {
        int varSize = Math.max(1, prmSize);
        int varTotalPages = Math.max(1, (prmAll.size() + varSize - 1) / varSize);
        int varNumber = Math.min(Math.max(1, prmNumber), varTotalPages);
        int varFrom = (varNumber - 1) * varSize;
        int varTo = Math.min(varFrom + varSize, prmAll.size());
        return new clsPage<>(List.copyOf(prmAll.subList(varFrom, varTo)), varNumber, varTotalPages, prmAll.size());
    }

    public List<T> opGetItems() {
        return attItems;
    }

    public int opGetNumber() {
        return attNumber;
    }

    public int opGetTotalPages() {
        return attTotalPages;
    }

    public int opGetTotalItems() {
        return attTotalItems;
    }

    public boolean opHasPrevious() {
        return attNumber > 1;
    }

    public boolean opHasNext() {
        return attNumber < attTotalPages;
    }
}
