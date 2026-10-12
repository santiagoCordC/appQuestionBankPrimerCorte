package pkgUtil;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Utilidades de texto.
 */
public final class clsText {

    private clsText() {
    }

    /** Minúsculas, sin tildes y sin espacios repetidos: "Lógica  " → "logica". */
    public static String opNormalize(String prmValue) {
        if (prmValue == null) {
            return "";
        }
        String varText = Normalizer.normalize(prmValue, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return varText.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    public static boolean opIsBlank(String prmValue) {
        return prmValue == null || prmValue.isBlank();
    }
}
