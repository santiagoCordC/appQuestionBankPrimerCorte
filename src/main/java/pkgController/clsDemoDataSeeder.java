package pkgController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgLifecycle.clsQuestionLifecycle;
import pkgDomain.pkgEntities.enRole;
import pkgDomain.pkgRepository.IQuestionRepository;
import pkgDomain.pkgRepository.IUserRepository;
import pkgServices.clsConfig;
import pkgServices.clsPasswordHasher;

/**
 * Datos iniciales. Siempre garantiza un administrador; si la base está vacía
 * y demo.datos=true, crea autores, revisores y preguntas de ejemplo.
 */
public class clsDemoDataSeeder {

    public static final String ADMIN_PASSWORD = "Admin123*";
    public static final String DEMO_PASSWORD = "Clave123*";

    private final IUserRepository attUsers;
    private final IQuestionRepository attQuestions;
    private final clsConfig attConfig;

    public clsDemoDataSeeder(IUserRepository prmUsers, IQuestionRepository prmQuestions, clsConfig prmConfig) {
        attUsers = prmUsers;
        attQuestions = prmQuestions;
        attConfig = prmConfig;
    }

    public void opSeed() {
        boolean varEmpty = attUsers.opCount() == 0;
        if (attUsers.opFindByNickName("admin") == null) {
            opCreateUser("Administrador del sistema", "admin", attConfig.opGet("demo.correo.admin", "admin@example.com"),
                    enRole.ADMIN, ADMIN_PASSWORD);
        }
        if (!varEmpty || !attConfig.opIsDemoEnabled()) {
            return;
        }
        clsUser varAuthor1 = opCreateUser("Ana Autora", "autor1", attConfig.opGet("demo.correo.autor1", "autor1@example.com"),
                enRole.AUTHOR, DEMO_PASSWORD);
        clsUser varAuthor2 = opCreateUser("Andrés Autor", "autor2", attConfig.opGet("demo.correo.autor2", "autor2@example.com"),
                enRole.AUTHOR, DEMO_PASSWORD);
        opCreateUser("Rubén Revisor", "revisor1", attConfig.opGet("demo.correo.revisor1", "revisor1@example.com"),
                enRole.REVIEWER, DEMO_PASSWORD);
        opCreateUser("Rosa Revisora", "revisor2", attConfig.opGet("demo.correo.revisor2", "revisor2@example.com"),
                enRole.REVIEWER, DEMO_PASSWORD);

        List<clsQuestion> varQuestions = opGetDemoQuestions();
        LocalDateTime varTime = LocalDateTime.now().withNano(0);
        for (int varI = 0; varI < varQuestions.size(); varI++) {
            // Las 8 primeras son de autor1 y las 2 últimas de autor2; cada tercera queda pendiente de revisión
            String varDate = varTime.minusMinutes(varI * 10L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            varQuestions.get(varI)
                    .opSetId(UUID.randomUUID().toString())
                    .opSetAuthor(varI < 8 ? varAuthor1 : varAuthor2)
                    .opSetState(varI % 3 == 2 ? clsQuestionLifecycle.PENDING_REVIEW : clsQuestionLifecycle.DRAFT)
                    .opSetCreatedAt(varDate)
                    .opSetUpdatedAt(varDate);
            attQuestions.opSave(varQuestions.get(varI));
        }
    }

    private clsUser opCreateUser(String prmName, String prmNick, String prmEmail, enRole prmRole, String prmPassword) {
        clsUser varUser = new clsUser(UUID.randomUUID().toString(), prmName, prmNick, prmEmail,
                clsPasswordHasher.opHash(prmPassword), prmRole, true);
        attUsers.opSave(varUser);
        return varUser;
    }

    /** Diez preguntas válidas (cumplen la validación estructural). */
    public static List<clsQuestion> opGetDemoQuestions() {
        List<clsQuestion> varList = new ArrayList<>();
        varList.add(opQ("Diseño de software", "Patrones de diseño", "Patrones de comportamiento", "Media",
                "Un sistema de banco de preguntas maneja estados como Borrador, Pendiente de revisión y En revisión. Cada estado "
                + "permite acciones distintas y el equipo quiere evitar condicionales extensos que revisen el estado.",
                "¿Qué patrón de diseño permite encapsular el comportamiento de cada estado en una clase separada?",
                "State", "Singleton", "Adapter", "Composite", "A",
                "State encapsula cada estado en una clase con su propio comportamiento y el objeto delega en su estado "
                + "actual, lo que elimina los condicionales sobre el estado.",
                "Gamma, E., Helm, R., Johnson, R. y Vlissides, J. (1994). Design Patterns. Addison-Wesley."));
        varList.add(opQ("Diseño de software", "Principios de diseño", "SOLID", "Media",
                "En una aplicación por capas, la clase que implementa los casos de uso crea directamente una instancia de la clase "
                + "que accede a la base de datos SQLite. Cambiar a PostgreSQL obliga a modificar la lógica de negocio.",
                "¿Qué principio SOLID se está incumpliendo en esta situación?",
                "Inversión de dependencias", "Segregación de interfaces", "Sustitución de Liskov", "Abierto/cerrado", "A",
                "El principio de inversión de dependencias indica que los módulos de alto nivel deben depender de abstracciones y "
                + "no de implementaciones concretas.",
                "Martin, R. C. (2017). Clean Architecture. Prentice Hall."));
        varList.add(opQ("Diseño de software", "Patrones de diseño", "Patrones de comportamiento", "Baja",
                "Cuando el administrador asigna un revisor a una pregunta, el sistema debe enviar un correo y, en el futuro, "
                + "también una notificación dentro de la aplicación, sin cambiar el código que hace la asignación.",
                "¿Qué patrón permite que varios objetos reaccionen a la asignación sin acoplarlos a quien la realiza?",
                "Factory Method", "Observer", "Decorator", "Prototype", "B",
                "En Observer el sujeto mantiene una lista de observadores y los notifica cuando ocurre un evento, sin conocer "
                + "sus clases concretas.",
                "Gamma, E., Helm, R., Johnson, R. y Vlissides, J. (1994). Design Patterns. Addison-Wesley."));
        varList.add(opQ("Razonamiento cuantitativo", "Aritmética", "Porcentajes", "Baja",
                "En un simulacro de 40 preguntas, un estudiante respondió correctamente 26 preguntas y dejó 4 sin responder.",
                "¿Qué porcentaje de las preguntas del simulacro respondió correctamente?",
                "65 %", "70 %", "72,2 %", "60 %", "A",
                "El porcentaje se calcula sobre el total de preguntas: 26/40 = 0,65, es decir, 65 %.",
                "ICFES. Guía de orientación Saber Pro: módulos de competencias genéricas."));
        varList.add(opQ("Razonamiento cuantitativo", "Probabilidad", "Probabilidad clásica", "Baja",
                "Un banco tiene 10 preguntas de Diseño de software, de las cuales 3 son de dificultad alta. "
                + "Un simulacro escoge al azar una de esas 10 preguntas.",
                "¿Cuál es la probabilidad de que la pregunta escogida no sea de dificultad alta?",
                "0,3", "0,7", "0,5", "0,03", "B",
                "Hay 7 preguntas que no son de dificultad alta entre 10 posibles: 7/10 = 0,7.",
                "Walpole, R. E. et al. (2012). Probabilidad y estadística para ingeniería y ciencias. Pearson."));
        varList.add(opQ("Pensamiento científico: matemáticas y estadística", "Estadística descriptiva",
                "Medidas de tendencia central", "Baja",
                "Los tiempos, en minutos, que tardaron cinco estudiantes en terminar un simulacro fueron: 42, 35, 58, 40 y 45.",
                "¿Cuál es la mediana de los tiempos registrados?",
                "44", "40", "42", "45", "C",
                "Al ordenar los datos (35, 40, 42, 45, 58) el valor central es 42. El valor 44 es la media, no la mediana.",
                "Walpole, R. E. et al. (2012). Probabilidad y estadística para ingeniería y ciencias. Pearson."));
        varList.add(opQ("Lectura crítica", "Comprensión de textos", "Idea principal", "Media",
                "«La revisión por pares no garantiza que una pregunta sea perfecta, pero reduce de forma notable los errores "
                + "conceptuales y las ambigüedades, porque obliga a que otras personas la analicen con criterios comunes.»",
                "¿Cuál es la idea principal del fragmento?",
                "La revisión por pares garantiza preguntas sin errores",
                "Los autores no deben revisar sus propias preguntas",
                "Las ambigüedades son el error más frecuente",
                "La revisión por pares mejora la calidad aunque no la asegura", "D",
                "El texto afirma que la revisión no garantiza la perfección pero reduce errores y ambigüedades.",
                "ICFES. Guía de orientación Saber Pro: módulo de Lectura crítica."));
        varList.add(opQ("Inglés", "Comprensión lectora", "Información explícita", "Baja",
                "Read the message: «The new question bank will be available next Monday. Reviewers must complete their "
                + "evaluations before Friday so that approved questions can be published on time.»",
                "When must reviewers finish their evaluations?",
                "Before Friday", "Next Monday", "After publication", "On Saturday", "A",
                "El mensaje indica que los revisores deben completar sus evaluaciones «before Friday».",
                "ICFES. Guía de orientación Saber Pro: módulo de Inglés."));
        varList.add(opQ("Competencias ciudadanas", "Participación", "Manejo de conflictos", "Media",
                "En la universidad se propone que los estudiantes con bajos resultados en los simulacros no puedan inscribir "
                + "electivas. Algunos estudiantes afirman que la medida afecta su derecho a la educación.",
                "¿Cuál de las siguientes acciones es la más adecuada para resolver el desacuerdo?",
                "Aplicar la medida sin consultar a nadie",
                "Abrir un diálogo entre estudiantes y directivas",
                "Suspender los simulacros de todo el semestre",
                "Pedir a los estudiantes que no opinen", "B",
                "El diálogo entre las partes permite considerar los argumentos y derechos de todos los involucrados.",
                "Ministerio de Educación Nacional (2004). Estándares básicos de competencias ciudadanas."));
        varList.add(opQ("Formulación de proyectos de ingeniería", "Gestión de requisitos", "Criterios de aceptación", "Media",
                "Un equipo debe entregar un prototipo funcional en tres cortes. En el primer corte implementó el registro de "
                + "preguntas, pero no definió criterios de aceptación y el cliente rechazó parte del trabajo.",
                "¿Qué debió hacer el equipo para reducir este riesgo desde la planeación?",
                "Aumentar las funcionalidades del primer corte",
                "Postergar las pruebas hasta el último corte",
                "Definir criterios de aceptación con el cliente",
                "Cambiar el lenguaje de programación", "C",
                "Los criterios de aceptación acordados con el cliente definen cuándo una historia está terminada.",
                "Project Management Institute (2021). Guía del PMBOK (7.ª ed.). PMI."));
        // Las dos últimas plantean un caso: usan el plugin "Pregunta tipo caso"
        varList.get(8).opSetType("TIPO_CASO");
        varList.get(9).opSetType("TIPO_CASO");
        return varList;
    }

    private static clsQuestion opQ(String prmCompetence, String prmTopic, String prmSubtopic, String prmDifficulty,
            String prmContext, String prmStatement, String prmA, String prmB, String prmC, String prmD,
            String prmRight, String prmJustification, String prmBibliography) {
        return new clsQuestion().opSetCompetence(prmCompetence).opSetTopic(prmTopic).opSetSubtopic(prmSubtopic)
                .opSetDifficulty(prmDifficulty).opSetContext(prmContext).opSetStatement(prmStatement)
                .opSetOptions(prmA, prmB, prmC, prmD).opSetRightAnswer(prmRight)
                .opSetJustification(prmJustification).opSetBibliography(prmBibliography)
                .opSetType("SELECCION_MULTIPLE");
    }
}
