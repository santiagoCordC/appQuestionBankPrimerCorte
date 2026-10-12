package pkgServices;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Parámetros configurables leídos de app.properties (competencias,
 * dificultades, expresiones prohibidas, tamaño de página, correo...).
 * Agregar una competencia solo exige editar ese archivo.
 */
public class clsConfig {

    private final Properties attProperties;

    public clsConfig(Properties prmProperties) {
        attProperties = prmProperties == null ? new Properties() : prmProperties;
    }

    /** Lee /app.properties del classpath en UTF-8. */
    public static clsConfig opLoadDefault() {
        Properties varProperties = new Properties();
        try (InputStream varInput = clsConfig.class.getResourceAsStream("/app.properties")) {
            if (varInput != null) {
                varProperties.load(new InputStreamReader(varInput, StandardCharsets.UTF_8));
            } else {
                System.err.println("No se encontró app.properties; se usan valores por defecto.");
            }
        } catch (Exception e) {
            System.err.println("Error al leer app.properties: " + e.getMessage());
        }
        return new clsConfig(varProperties);
    }

    public List<String> opGetCompetences() {
        return opList("competencias", "Lectura crítica;Razonamiento cuantitativo;Diseño de software");
    }

    public List<String> opGetDifficulties() {
        return opList("dificultades", "Baja;Media;Alta");
    }

    public List<String> opGetForbiddenExpressions() {
        return opList("expresiones.prohibidas", "todas las anteriores;ninguna de las anteriores");
    }

    public int opGetContextMinLength() {
        return opInt("contexto.longitud.minima", 10);
    }

    public int opGetOptionMaxLength() {
        return opInt("opcion.longitud.maxima", 400);
    }

    public int opGetPageSize() {
        return opInt("pagina.tamano", 5);
    }

    public boolean opIsDemoEnabled() {
        return Boolean.parseBoolean(opGet("demo.datos", "true"));
    }

    public String opGet(String prmKey, String prmDefault) {
        return attProperties.getProperty(prmKey, prmDefault).trim();
    }

    public int opInt(String prmKey, int prmDefault) {
        try {
            return Integer.parseInt(opGet(prmKey, ""));
        } catch (NumberFormatException e) {
            return prmDefault;
        }
    }

    private List<String> opList(String prmKey, String prmDefault) {
        List<String> varValues = new ArrayList<>();
        for (String varPart : opGet(prmKey, prmDefault).split(";")) {
            if (!varPart.isBlank()) {
                varValues.add(varPart.trim());
            }
        }
        return varValues;
    }
}
