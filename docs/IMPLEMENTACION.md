# Implementación del primer corte

Material de apoyo para el documento de arquitectura y la sustentación.

## Historias de usuario y criterios de aceptación

### Registro de usuarios (soporte)
- Desde la pantalla de inicio cualquiera puede crear una cuenta de **Autor de preguntas** o **Revisor** (nunca de Administrador).
- Nombre obligatorio; usuario de 3 a 20 caracteres (letras, números, punto, guion bajo) y único; correo válido; contraseña de mínimo 8 caracteres con mayúscula, número y carácter especial, confirmada.
- La contraseña se guarda cifrada con BCrypt. Todos los errores se muestran a la vez (tubería de registro).

### HU01 — Crear preguntas (Autor de preguntas)
- La pregunta tiene: contexto, pregunta directa, cuatro opciones (A-D: la respuesta correcta y tres distractores), respuesta correcta, justificación, bibliografía, competencia, tema, subtema y nivel de dificultad.
- La pregunta tiene un **tipo** (plugin del microkernel): *Selección múltiple* o *Pregunta tipo caso*.
- Al grabar, la pregunta pasa por la tubería de validación estructural y se muestran **todos** los errores a la vez:
  - todos los campos son obligatorios; competencia y dificultad salen de un catálogo (`clsFilterRequired`, `clsFilterInList`);
  - el contexto tiene una longitud mínima (`clsFilterMinLength`);
  - hay una única pregunta directa: un solo signo "?" (`clsFilterSingleQuestion`);
  - las cuatro opciones están diligenciadas y son distintas (`clsFilterFourOptions`);
  - hay una única respuesta correcta y es A, B, C o D (`clsFilterSingleRightAnswer`);
  - ninguna opción dice "Todas/Ninguna de las anteriores" (`clsFilterForbiddenExpressions`);
  - las opciones tienen longitud máxima, longitudes parecidas y la misma estructura (`clsFilterOptionFormat`);
  - si es tipo caso, el plugin agrega su propio filtro: contexto de mínimo 150 caracteres.
- La pregunta queda en estado **Borrador**. Solo su autor la puede editar y solo mientras esté en Borrador.

### HU02 — Enviar a revisión (Autor de preguntas)
- Solo el autor puede pasar su pregunta de **Borrador** a **Pendiente de revisión**; antes se vuelve a validar.
- Después de enviarla ya no se puede editar.
- Los estados se muestran con colores: Borrador (gris), Pendiente de revisión (naranja), En revisión (azul).

### HU03 — Listar mis preguntas (Autor de preguntas)
- El autor solo ve sus propias preguntas, de la modificada más recientemente a la más antigua.
- Filtros: texto (sin importar tildes ni mayúsculas, varias palabras), estado, competencia y dificultad.
- Paginación configurable (`pagina.tamano` en `app.properties`) con botones Anterior / Siguiente.

### HU04 — Asignar revisores (Administrador)
- El administrador ve las preguntas en **Pendiente de revisión**.
- Debe seleccionar **al menos un** revisor activo; el autor nunca revisa su propia pregunta.
- La pregunta pasa a **En revisión** y cada revisor recibe un correo con los datos de la pregunta.

## Patrones de diseño y estilos implementados

| Patrón | Contexto del problema | Cómo se aplicó |
|---|---|---|
| **State** (GoF, comportamiento) | Lo que se puede hacer con una pregunta depende de su estado (solo en Borrador se edita y se envía; solo en Pendiente se asignan revisores) y en los siguientes cortes llegan más estados. | `clsQuestionState` define las acciones (`opSubmitForReview`, `opStartReview`, `opIsEditable`...). `clsStateDraft`, `clsStatePendingReview` y `clsStateInReview` sobrescriben solo lo que permiten y cambian el estado de la pregunta. `clsQuestion` (contexto) delega en su estado. `clsQuestionLifecycle` guarda una instancia de cada estado. |
| **Observer** (GoF, comportamiento) | Al asignar revisores hay que enviar un correo; en el futuro también notificaciones en la app o auditoría. | `clsReviewController` es el sujeto (`opAddObserver`, `opNotify`); `IAssignmentObserver` es el observador; `clsEmailNotifier` es el observador concreto que usa `IEmailSender` (`clsSmtpEmailSender`). |
| **Singleton** (GoF, creacional) | Todos los repositorios deben usar la misma base de datos. | `clsConnectionSQL.opGetInstance()` devuelve siempre la misma instancia, que conoce la ruta de la base y crea las tablas. |
| **Tuberías y filtros** (estilo arquitectónico) | La validación estructural tiene muchas reglas independientes y el registro de usuarios también necesita validar varios campos. | `clsPipeline<T>` pasa un `clsPipeData<T>` (dato + errores) por una lista de `IFilter<T>`. El primer filtro transforma (normaliza espacios / minúsculas) y los siguientes revisan y anotan errores. Hay filtros genéricos reutilizados en ambas tuberías (`clsFilterRequired`, `clsFilterMinLength`, `clsFilterInList`, `clsFilterRegex`). `clsPipelineFactory` arma la tubería de preguntas y la de registro. |
| **Microkernel** (estilo arquitectónico) | El banco tendrá varios tipos de pregunta y cada uno puede tener reglas propias. | `clsQuestionMicrokernel` (núcleo) carga por reflexión los plugins declarados en `plugins.properties`. Cada plugin implementa `IQuestionPlugin` (código, nombre, descripción y filtros extra). La fábrica de tuberías conecta los filtros del plugin al final de la tubería. Plugins: `clsMultipleChoicePlugin`, `clsCaseQuestionPlugin`. |
| **MVC** (micro-patrón de arquitectura) | Separar la interfaz de la lógica. | Vista: `pkgPresentation`. Controlador: `pkgController`. Modelo: `pkgDomain` + `pkgServices`. |

## Principios SOLID

| Principio | Dónde se ve |
|---|---|
| Responsabilidad única | Cada filtro de la tubería y cada estado es una clase; `clsQuestionController` (autor) y `clsReviewController` (administrador) están separados; el correo lo envía `clsSmtpEmailSender`, no el controlador. |
| Abierto/cerrado | Filtros, plugins (tipos de pregunta), estados y observadores nuevos se agregan sin modificar el código existente. |
| Sustitución de Liskov | Cualquier `IQuestionRepository` (SQLite o en memoria) funciona igual para el controlador; cualquier subclase de `clsQuestionState` sirve como estado de la pregunta. |
| Segregación de interfaces | Interfaces pequeñas: `IFilter` (1 método), `IAssignmentObserver` (1), `IEmailSender` (1), un repositorio por entidad. |
| Inversión de dependencias | Los controladores dependen de las interfaces `IUserRepository`, `IQuestionRepository`, `IAssignmentRepository` (definidas en el dominio) y reciben las implementaciones por el constructor; `App` decide cuáles usar. |

## Escenario de calidad de modificabilidad (propuesta)

| Elemento | Valor |
|---|---|
| Fuente | Equipo de desarrollo |
| Estímulo | Se pide agregar un tipo de pregunta nuevo (p. ej. "pregunta con imagen", que exige la ruta de la imagen). |
| Artefacto | Microkernel de tipos de pregunta y tubería de validación (`pkgServices`) |
| Entorno | Tiempo de desarrollo |
| Respuesta | Se crea una clase que implementa `IQuestionPlugin` (con su filtro propio) y se agrega una línea en `plugins.properties`; el núcleo, la tubería, los controladores y las vistas no se modifican. |
| Medida | 1–2 clases nuevas + 1 línea de configuración; menos de 2 horas incluyendo sus pruebas unitarias. |

## Lo que se dejó para los siguientes cortes

Formato de evaluación de los revisores, aprobación/rechazo, publicación, simulacros, estadísticas,
auditoría y administración de usuarios (activar/desactivar, crear administradores).
