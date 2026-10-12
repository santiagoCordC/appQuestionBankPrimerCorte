package pkgServices.pkgMicrokernel.pkgKernel;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

/**
 * Estilo Microkernel: el núcleo conoce solo la interfaz
 * {@link IQuestionPlugin}. Los tipos de pregunta son plugins que se
 * declaran en plugins.properties y se cargan por reflexión al arrancar.
 * Agregar un tipo nuevo = crear la clase del plugin y una línea en
 * plugins.properties; el núcleo y el resto de la aplicación no cambian.
 */
public class clsQuestionMicrokernel {

    private final Map<String, IQuestionPlugin> attPlugins = new LinkedHashMap<>();

    /** Núcleo con los plugins declarados en /plugins.properties. */
    public static clsQuestionMicrokernel opLoadDefault() {
        Properties varProperties = new Properties();
        try (InputStream varInput = clsQuestionMicrokernel.class.getResourceAsStream("/plugins.properties")) {
            if (varInput != null) {
                varProperties.load(new InputStreamReader(varInput, StandardCharsets.UTF_8));
            } else {
                System.err.println("No se encontró plugins.properties.");
            }
        } catch (Exception e) {
            System.err.println("Error al leer plugins.properties: " + e.getMessage());
        }
        clsQuestionMicrokernel varKernel = new clsQuestionMicrokernel();
        varKernel.opLoad(varProperties);
        return varKernel;
    }

    /**
     * Carga los plugins por reflexión. Formato: CODIGO=nombre.completo.DeLaClase.
     * Un plugin que no exista o no cumpla el contrato se ignora sin detener la aplicación.
     */
    public void opLoad(Properties prmProperties) {
        for (String varKey : new TreeSet<>(prmProperties.stringPropertyNames())) {
            String varClassName = prmProperties.getProperty(varKey).trim();
            try {
                Object varInstance = Class.forName(varClassName).getDeclaredConstructor().newInstance();
                if (varInstance instanceof IQuestionPlugin) {
                    opRegister((IQuestionPlugin) varInstance);
                } else {
                    System.err.println(varClassName + " no implementa IQuestionPlugin.");
                }
            } catch (ReflectiveOperationException | LinkageError e) {
                System.err.println("No se pudo cargar el plugin " + varKey + ": " + e);
            }
        }
    }

    public void opRegister(IQuestionPlugin prmPlugin) {
        attPlugins.put(prmPlugin.opGetCode(), prmPlugin);
    }

    public IQuestionPlugin opGetPlugin(String prmCode) {
        return prmCode == null ? null : attPlugins.get(prmCode);
    }

    public boolean opHasPlugin(String prmCode) {
        return opGetPlugin(prmCode) != null;
    }

    public List<IQuestionPlugin> opGetPlugins() {
        return new ArrayList<>(attPlugins.values());
    }

    public List<String> opGetCodes() {
        return new ArrayList<>(attPlugins.keySet());
    }

    /** Nombre visible de un tipo (o el código si no hay plugin). */
    public String opGetName(String prmCode) {
        IQuestionPlugin varPlugin = opGetPlugin(prmCode);
        return varPlugin == null ? (prmCode == null ? "" : prmCode) : varPlugin.opGetName();
    }
}
