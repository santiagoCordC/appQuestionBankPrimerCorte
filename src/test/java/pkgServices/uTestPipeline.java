package pkgServices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsUserData;
import pkgDomain.pkgEntities.enRole;
import pkgServices.pkgPipeline.clsPipeData;
import pkgServices.pkgPipeline.clsPipeline;
import pkgServices.pkgPipeline.clsPipelineFactory;
import pkgServices.pkgPipeline.pkgFilters.clsFilterNormalizeQuestion;
import pkgServices.pkgPipeline.pkgFilters.clsFilterRequired;
import pkgServices.pkgPipeline.pkgFilters.clsFilterSingleQuestion;
import pkgTestDoubles.clsSamples;

/**
 * Pruebas de Tuberías y Filtros: la tubería de validación estructural de
 * preguntas y la de registro de usuarios.
 */
public class uTestPipeline {

    private final clsPipelineFactory attFactory = new clsPipelineFactory(clsSamples.opConfig(), clsSamples.opKernel());

    private List<String> opErrors(clsQuestion prmQuestion) {
        return attFactory.opCreateQuestionPipeline(prmQuestion.opGetType()).opRun(prmQuestion).opGetErrors();
    }

    private boolean opFailsWith(clsQuestion prmQuestion, String prmFragment) {
        return opErrors(prmQuestion).stream().anyMatch(varE -> varE.contains(prmFragment));
    }

    /* ---------- Tubería genérica ---------- */

    @Test
    public void laTuberiaPasaElDatoPorTodosLosFiltrosEnOrden() {
        StringBuilder varTrace = new StringBuilder();
        clsPipeline<String> varPipeline = new clsPipeline<String>()
                .opAdd(varD -> varTrace.append("1"))
                .opAdd(varD -> varTrace.append("2"))
                .opAdd(new clsFilterRequired<>(varS -> varS, "Vacío"));
        clsPipeData<String> varResult = varPipeline.opRun("");
        assertEquals("12", varTrace.toString());
        assertEquals(List.of("Vacío"), varResult.opGetErrors());
        assertFalse(varResult.opIsValid());
        assertTrue(varPipeline.opRun("hola").opIsValid());
        assertEquals(3, varPipeline.opSize());
    }

    @Test
    public void elFiltroDeNormalizacionTransformaElDato() {
        clsQuestion varQ = clsSamples.opValidQuestion().opSetStatement("¿Qué   patrón\tsepara la vista?")
                .opSetOptions("MVC", "Singleton  de  GoF", "Observer", "Builder");
        new clsPipeline<clsQuestion>().opAdd(new clsFilterNormalizeQuestion()).opRun(varQ);
        assertEquals("¿Qué patrón separa la vista?", varQ.opGetStatement());
        assertEquals("Singleton de GoF", varQ.opGetOption("B"));
    }

    /* ---------- Validación estructural de preguntas ---------- */

    @Test
    public void preguntaCompletaEsValida() {
        assertEquals(List.of(), opErrors(clsSamples.opValidQuestion()));
    }

    @Test
    public void camposObligatoriosYTipo() {
        assertTrue(opErrors(new clsQuestion()).size() >= 9);
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetType("INVENTADO"), "tipo de pregunta"));
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetJustification(""), "justificación"));
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetBibliography(""), "bibliografía"));
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetSubtopic(" "), "subtema"));
    }

    @Test
    public void competenciaYDificultadDelCatalogo() {
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetCompetence("Astrología"), "competencia"));
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetDifficulty("Extrema"), "dificultad"));
    }

    @Test
    public void contextoConLongitudMinima() {
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetContext("Corto"), "contexto"));
    }

    @Test
    public void unaUnicaPreguntaDirecta() {
        clsPipeline<clsQuestion> varOnly = new clsPipeline<clsQuestion>().opAdd(new clsFilterSingleQuestion());
        assertTrue(varOnly.opRun(clsSamples.opValidQuestion()).opIsValid());
        assertFalse(varOnly.opRun(clsSamples.opValidQuestion().opSetStatement("Seleccione la correcta.")).opIsValid());
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetStatement("¿Qué es MVC? ¿Y MVP?"), "única pregunta"));
    }

    @Test
    public void cuatroOpcionesDistintas() {
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetOptions("MVC", "Singleton", "Observer", ""), "cuatro opciones"));
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetOptions("MVC", "Singleton", "mvc", "Builder"), "diferentes"));
    }

    @Test
    public void unaUnicaRespuestaCorrecta() {
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetRightAnswer(""), "respuesta correcta"));
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetRightAnswer("E"), "respuesta correcta"));
    }

    @Test
    public void sinTodasNiNingunaDeLasAnteriores() {
        assertTrue(opFailsWith(clsSamples.opValidQuestion()
                .opSetOptions("MVC", "Singleton", "Observer", "Todas las Anteriores"), "anteriores"));
        assertTrue(opFailsWith(clsSamples.opValidQuestion()
                .opSetOptions("MVC", "Singleton", "Observer", "NINGUNA de  las anteriores"), "anteriores"));
    }

    @Test
    public void longitudYEstructuraDeLasOpciones() {
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetOptions("MVC", "Singleton", "Observer", "x".repeat(101)), "máximo"));
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetOptions("Corta", "Breve", "Mini",
                "Una opción muchísimo más larga que las otras tres"), "longitudes parecidas"));
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetOptions("MVC", "singleton", "Observer", "Builder"), "estructura"));
        assertTrue(opFailsWith(clsSamples.opValidQuestion().opSetOptions("MVC.", "Singleton", "Observer", "Builder"), "estructura"));
        assertEquals(List.of(), opErrors(clsSamples.opValidQuestion().opSetOptions("2", "128", "64", "1024")));
    }

    @Test
    public void muestraTodosLosErroresALaVez() {
        clsQuestion varQ = clsSamples.opValidQuestion().opSetContext("").opSetRightAnswer("").opSetTopic("");
        assertEquals(3, opErrors(varQ).size());
    }

    @Test
    public void elPluginTipoCasoAgregaSuFiltroALaTuberia() {
        clsQuestion varCase = clsSamples.opValidQuestion().opSetType("TIPO_CASO");
        assertTrue("Contexto corto para un caso", opFailsWith(varCase, "tipo caso"));
        varCase.opSetContext("Una empresa de software tiene una aplicación de escritorio por capas y necesita "
                + "separar la interfaz gráfica de la lógica de negocio.");
        assertEquals(List.of(), opErrors(varCase));
        assertTrue(attFactory.opCreateQuestionPipeline("TIPO_CASO").opSize()
                > attFactory.opCreateQuestionPipeline("SELECCION_MULTIPLE").opSize());
    }

    /* ---------- Registro de usuarios ---------- */

    private final clsPipeline<clsUserData> attUserPipeline =
            clsPipelineFactory.opCreateUserPipeline(List.of(enRole.AUTHOR, enRole.REVIEWER));

    private clsUserData opUser(String prmNick, String prmEmail, String prmPass, String prmConfirm, enRole prmRole) {
        return new clsUserData("  Ana   Autora ", prmNick, prmEmail, prmPass, prmConfirm, prmRole);
    }

    @Test
    public void registroValidoYNormalizado() {
        clsUserData varData = opUser(" Ana.Autora ", " ANA@Correo.com ", "Clave123*", "Clave123*", enRole.AUTHOR);
        assertEquals(List.of(), attUserPipeline.opRun(varData).opGetErrors());
        assertEquals("ana.autora", varData.opGetNickName());
        assertEquals("ana@correo.com", varData.opGetEmail());
        assertEquals("Ana Autora", varData.opGetFullName());
    }

    @Test
    public void registroConErrores() {
        assertEquals(1, attUserPipeline.opRun(opUser("a", "ana@correo.com", "Clave123*", "Clave123*", enRole.AUTHOR)).opGetErrors().size());
        assertEquals(1, attUserPipeline.opRun(opUser("ana", "correo-malo", "Clave123*", "Clave123*", enRole.AUTHOR)).opGetErrors().size());
        assertEquals("Contraseña débil", 1, attUserPipeline.opRun(opUser("ana", "a@b.co", "clave", "clave", enRole.AUTHOR)).opGetErrors().size());
        assertEquals("No coinciden", 1, attUserPipeline.opRun(opUser("ana", "a@b.co", "Clave123*", "Clave124*", enRole.AUTHOR)).opGetErrors().size());
        assertEquals("Admin no se autorregistra", 1, attUserPipeline.opRun(opUser("ana", "a@b.co", "Clave123*", "Clave123*", enRole.ADMIN)).opGetErrors().size());
        assertEquals(1, attUserPipeline.opRun(opUser("ana", "a@b.co", "Clave123*", "Clave123*", null)).opGetErrors().size());
    }
}
