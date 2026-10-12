package pkgController;

import java.util.List;

/**
 * Resultado de un caso de uso: si salió bien, el mensaje para el usuario,
 * los errores (si los hubo) y el valor producido.
 *
 * @param <T> tipo del valor devuelto.
 */
public class clsResult<T> {

    private final boolean attOk;
    private final String attMessage;
    private final List<String> attErrors;
    private final T attValue;

    private clsResult(boolean prmOk, String prmMessage, List<String> prmErrors, T prmValue) {
        attOk = prmOk;
        attMessage = prmMessage == null ? "" : prmMessage;
        attErrors = prmErrors == null ? List.of() : List.copyOf(prmErrors);
        attValue = prmValue;
    }

    public static <T> clsResult<T> opOk(T prmValue, String prmMessage) {
        return new clsResult<>(true, prmMessage, null, prmValue);
    }

    public static <T> clsResult<T> opFail(String prmMessage) {
        return new clsResult<>(false, prmMessage, null, null);
    }

    public static <T> clsResult<T> opFail(String prmMessage, List<String> prmErrors) {
        return new clsResult<>(false, prmMessage, prmErrors, null);
    }

    public boolean opIsOk() {
        return attOk;
    }

    public String opGetMessage() {
        return attMessage;
    }

    public List<String> opGetErrors() {
        return attErrors;
    }

    public T opGetValue() {
        return attValue;
    }

    /** Mensaje y errores en un solo texto, para mostrarlo en un cuadro de diálogo. */
    public String opGetFullMessage() {
        StringBuilder varText = new StringBuilder(attMessage);
        for (String varError : attErrors) {
            varText.append("\n • ").append(varError);
        }
        return varText.toString();
    }
}
