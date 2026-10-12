# appQuestionBank — Primer corte

Aplicación de escritorio (Java Swing + SQLite) para alimentar el banco de preguntas Saber Pro.
Arquitectura monolítica en 3 capas con el micro-patrón MVC.

## Historias de usuario implementadas

| HU | Historia | Dónde |
|---|---|---|
| — | Registro de usuarios (Autor de preguntas o Revisor) e inicio de sesión. | `uiRegisterUser`, `uiLogin` → `clsAuthController` → tubería de registro |
| HU01 | Como autor, crear preguntas de selección múltiple con única respuesta (contexto, pregunta directa, 4 opciones, respuesta correcta, justificación, bibliografía, competencia, tema, subtema, dificultad). Al grabar se aplica la validación estructural. | `dlgQuestionEditor` → `clsQuestionController.opCreate` → tubería de validación (`clsPipelineFactory`) |
| HU02 | Como autor, cambiar mis preguntas de "Borrador" a "Pendiente de revisión". Los estados se ven con colores. | `pnlMyQuestions` (botón *Enviar a revisión*) → `clsQuestionController.opSubmitForReview`; colores en `clsUiKit.opStateColor` |
| HU03 | Como autor, listar mis preguntas con paginación y filtros. | `pnlMyQuestions` → `clsQuestionController.opListMine` → `clsSearchCriteria` + `clsPage` |
| HU04 | Como administrador, asignar al menos un revisor a las preguntas "Pendiente de revisión"; el sistema envía un email a cada revisor. | `pnlAssignReviewers` → `clsReviewController.opAssignReviewers` → `clsEmailNotifier` |

Flujo de estados del primer corte:

```
Borrador ──(autor: Enviar a revisión)──► Pendiente de revisión ──(admin: Asignar revisores)──► En revisión
  gris                                        naranja                                            azul
```

## Ejecutar

Abra la carpeta en VS Code (Extension Pack for Java) y ejecute `pkgApp.App`, o desde la terminal
(Java 17+), en la raíz del proyecto:

```bash
# Linux / macOS (en Windows cambie ":" por ";")
javac -encoding UTF-8 -d bin -cp "lib/*" $(find src/main/java -name "*.java")
cp -r src/main/resources/* bin/
java -cp "bin:lib/*" pkgApp.App
```

La primera vez se crea `data/banco_preguntas.db` con estos usuarios de prueba:

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `Admin123*` | Administrador |
| `autor1`, `autor2` | `Clave123*` | Autor de preguntas |
| `revisor1`, `revisor2` | `Clave123*` | Revisor |

También puede crear cuentas nuevas con el botón **Registrarse** de la pantalla de inicio
(solo Autor de preguntas o Revisor; el administrador no se autorregistra).

Para empezar de cero borre `data/banco_preguntas.db`.

### Correo (HU04)

Por defecto los correos se guardan en `data/correos_enviados.txt` (no se necesita internet para la demo).
Para enviarlos de verdad, en `src/main/resources/app.properties` configure `correo.smtp.host`,
`correo.usuario` y `correo.clave` (con Gmail use una *contraseña de aplicación*), y cambie
`demo.correo.revisor1` / `demo.correo.revisor2` por correos reales **antes** de crear la base de datos.
No suba su contraseña al repositorio.

## Arquitectura

```
┌──────────────────────── Presentación (Vista) ────────────────────────┐
│ pkgPresentation: uiLogin, uiRegisterUser, uiMainMenu, pnlMyQuestions,│
│                  dlgQuestionEditor, pnlAssignReviewers, clsUiKit     │
└──────────────────────────────┬───────────────────────────────────────┘
┌──────────────────────── Lógica de negocio ───────────────────────────┐
│ pkgController (Controlador): clsAuthController, clsQuestionController,│
│                              clsReviewController, clsSession         │
│ pkgServices: tuberías y filtros, microkernel (tipos de pregunta),    │
│              notificación (Observer), configuración                  │
│ pkgDomain (Modelo): entidades, ciclo de vida (State), repositorios   │
└──────────────────────────────┬───────────────────────────────────────┘
┌──────────────────────── Acceso a datos ──────────────────────────────┐
│ pkgPersistence: clsConnectionSQL (Singleton), repositorios SQLite    │
└──────────────────────────────────────────────────────────────────────┘
```

`pkgApp.App` arma las piezas: es el único lugar que conoce las clases concretas (SQLite, SMTP, plugins).

## Patrones y estilos

| Patrón / estilo | Clases | Para qué |
|---|---|---|
| **State** (GoF) | `clsQuestionState`, `clsStateDraft`, `clsStatePendingReview`, `clsStateInReview`, `clsQuestionLifecycle` | Cada estado decide qué se puede hacer con la pregunta y a qué estado pasa; la pregunta solo delega. |
| **Observer** (GoF) | `IAssignmentObserver`, `clsEmailNotifier`, `clsReviewController` (sujeto) | Al asignar un revisor se notifica a los observadores; el controlador no sabe que existe el correo. |
| **Singleton** (GoF) | `clsConnectionSQL` | Una sola instancia que conoce la base de datos y entrega conexiones. |
| **Tuberías y filtros** | `clsPipeline`, `IFilter`, `pkgFilters.*`, `clsPipelineFactory` | La pregunta (o el formulario de registro) pasa por una cadena de filtros: primero se normaliza el texto y luego cada filtro anota sus errores. |
| **Microkernel** | `clsQuestionMicrokernel`, `IQuestionPlugin`, `pkgPlugins.*`, `plugins.properties` | Los tipos de pregunta son plugins cargados por reflexión; cada uno puede agregar filtros a la tubería. |
| **MVC** | `pkgPresentation` / `pkgController` / `pkgDomain` + `pkgServices` | Separa la interfaz de la lógica. |

Más detalle (principios SOLID y criterios de aceptación) en [`docs/IMPLEMENTACION.md`](docs/IMPLEMENTACION.md).

## Pruebas

`src/test/java` tiene 66 pruebas JUnit 4 de entidades, estados, tuberías, microkernel, servicios y controladores.
Los controladores se prueban con repositorios en memoria (`pkgTestDoubles`), sin base de datos.
Se ejecutan desde la pestaña *Testing* de VS Code o con:

```bash
javac -encoding UTF-8 -d bin -cp "lib/*:lib/test/*" $(find src -name "*.java")
cp -r src/main/resources/* bin/
java -cp "bin:lib/*:lib/test/*" org.junit.runner.JUnitCore pkgDomain.uTestQuestion pkgDomain.uTestFilterAndPage \
  pkgServices.uTestPipeline pkgServices.uTestMicrokernel pkgServices.uTestServices \
  pkgController.uTestQuestionController pkgController.uTestReviewController pkgController.uTestAuthAndDemo
```
