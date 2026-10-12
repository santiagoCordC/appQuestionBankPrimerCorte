package pkgServices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;
import org.junit.Test;
import pkgDomain.pkgEntities.clsQuestion;
import pkgServices.pkgMicrokernel.pkgKernel.IQuestionPlugin;
import pkgServices.pkgMicrokernel.pkgKernel.clsQuestionMicrokernel;
import pkgServices.pkgPipeline.IFilter;

/**
 * Pruebas del microkernel de tipos de pregunta.
 */
public class uTestMicrokernel {

    @Test
    public void cargaLosPluginsDePluginsProperties() {
        clsQuestionMicrokernel varKernel = clsQuestionMicrokernel.opLoadDefault();
        assertTrue(varKernel.opHasPlugin("SELECCION_MULTIPLE"));
        assertTrue(varKernel.opHasPlugin("TIPO_CASO"));
        assertEquals("Pregunta tipo caso", varKernel.opGetName("TIPO_CASO"));
    }

    @Test
    public void ignoraPluginsQueNoExistenONoCumplenElContrato() {
        Properties varP = new Properties();
        varP.setProperty("A_BUENO", "pkgServices.pkgMicrokernel.pkgPlugins.clsMultipleChoicePlugin");
        varP.setProperty("B_NO_EXISTE", "pkgServices.ClaseQueNoExiste");
        varP.setProperty("C_NO_ES_PLUGIN", "java.lang.Object");
        clsQuestionMicrokernel varKernel = new clsQuestionMicrokernel();
        varKernel.opLoad(varP);
        assertEquals(List.of("SELECCION_MULTIPLE"), varKernel.opGetCodes());
        assertNull(varKernel.opGetPlugin("B_NO_EXISTE"));
        assertFalse(varKernel.opHasPlugin(null));
        assertEquals("OTRO", varKernel.opGetName("OTRO"));
    }

    @Test
    public void sePuedeRegistrarUnTipoNuevoSinTocarElNucleo() {
        IQuestionPlugin varNew = new IQuestionPlugin() {
            @Override public String opGetCode() { return "VERDADERO_FALSO"; }
            @Override public String opGetName() { return "Verdadero o falso"; }
            @Override public String opGetDescription() { return "Prueba"; }
            @Override public List<IFilter<clsQuestion>> opGetFilters(clsConfig prmConfig) { return List.of(); }
        };
        clsQuestionMicrokernel varKernel = new clsQuestionMicrokernel();
        varKernel.opRegister(varNew);
        assertEquals(1, varKernel.opGetPlugins().size());
        assertEquals("Verdadero o falso", varKernel.opGetName("VERDADERO_FALSO"));
    }
}
