# Guion de sustentación — Segundo corte

**Duración objetivo: 15 minutos.** El protocolo asigna tiempos fijos a cada sección; no acelerar la reproducción ni excederlos. Todos los integrantes deben intervenir. Reemplacen los campos entre corchetes por nombres y evidencias reales antes de grabar.

> **Nota:** el escenario de escalabilidad describe metas propuestas; no presentarlas como resultados de una prueba de carga ejecutada si no se ha realizado y documentado esa medición.

## 00:00–01:00 — Historias de usuario de la iteración 2

**Presenta:** [integrante 1]

**En pantalla:** `EntregablesSegundoCorte2026.2-IngSoft2.pdf.md`, sección “Requisitos funcionales”; después, mostrar las historias y criterios de aceptación incluidos en `Informe_Arquitectura_Segundo_Corte.md`.

**Guion:** “En esta iteración el autor puede crear preguntas Saber Pro completas y enviarlas a revisión; el administrador asigna entre uno y tres revisores; y cada revisor consulta sus asignaciones, registra su decisión y deja observaciones. El sistema valida los campos estructurales, muestra los estados con colores y notifica el resultado. En el informe relacionamos cada historia con sus criterios de aceptación y con las rutas de código que la implementan.”

## 01:00–02:00 — Escenario de calidad: escalabilidad

**Presenta:** [integrante 2]

**En pantalla:** escenario de calidad del informe. Señalar servicios independientes, RabbitMQ y la métrica propuesta.

**Guion:** “El escenario considera un aumento de solicitudes de creación y envío a revisión. Question Service y Validation Service atienden las solicitudes REST; el envío de revisión se publica en RabbitMQ y Lifecycle Service lo procesa de forma asíncrona. Proponemos medir 100 solicitudes concurrentes, latencia p95 inferior a dos segundos para aceptar el envío, cero operaciones perdidas y escalado independiente del consumidor. Son objetivos de aceptación; todavía no presentamos una medición de carga ejecutada.”

## 02:00–04:00 — Arquitectura y diseño: C4, UML y separación frontend/backend

**Presenta:** [integrante 3]

**En pantalla:** diagramas C4 del informe; después `../frontend/src/main/java/com/bancopreguntas/desktop/ApiClient.java` y `../backend/question-service/src/main/java/co/edu/unicauca/question/controller/QuestionController.java`.

**Guion:** “El frontend es una aplicación de escritorio Java Swing separada del backend. `ApiClient` consume REST y no comparte clases ni base de datos con los servicios. El backend se divide en seis microservicios: usuarios, validación, preguntas, ciclo de vida, revisión y notificaciones. REST se usa para consultas y operaciones que necesitan respuesta inmediata; RabbitMQ desacopla el envío a revisión y las notificaciones. El diagrama de secuencia muestra el recorrido desde el autor hasta Lifecycle y el resultado asíncrono. Cada servicio es dueño de sus datos; las bases H2 de esta versión son volátiles.”

## 04:00–06:00 — Patrones de diseño GoF

**Presenta:** [integrantes 1 y 2, un minuto cada uno]

**En pantalla:** tabla de patrones del informe y las clases indicadas en cada fila.

**Guion:** “En el código identificamos dos patrones creacionales: Builder construye preguntas paso a paso y Factory Method crea el evaluador concreto según el tipo de solicitud. En los estructurales usamos Adapter para convertir reactivos externos, Facade para unificar las operaciones del banco y Decorator para enriquecer presentaciones. En los de comportamiento usamos State para controlar el ciclo de vida y Template Method para compartir el algoritmo de evaluación. En `EvaluationService` se elige el creador y cada subclase de `QuestionEvaluatorCreator` implementa su fábrica de evaluadores.”

## 06:00–07:00 — Bounded contexts

**Presenta:** [integrante 3]

**En pantalla:** diagrama de contextos del informe y la Figura 1 de `EntregablesSegundoCorte2026.2-IngSoft2_Figura1.png`.

**Guion:** “A partir de la figura de referencia delimitamos los contextos presentes en el proyecto: usuarios y roles; preguntas; validación; ciclo de vida; revisión; y notificaciones. Las áreas de simulacros, seguimiento académico y trazabilidad que aparecen en la figura no se implementan en esta versión. Los contextos de este alcance se comunican mediante REST o eventos, sin compartir entidades de persistencia.”

## 07:00–08:00 — Pruebas unitarias automatizadas

**Presenta:** [integrante 1]

**En pantalla:** terminal en `backend`, ejecutar `mvn test`; mostrar resultados y luego las pruebas de `question-service`, `review-service`, `validation-service` y `lifecycle-service`.

**Guion:** “Las pruebas cubren reglas del servicio de preguntas, construcción de preguntas, validación, transiciones de estado y asignación y consenso de revisión. Aquí se observa el resultado real de `mvn test`. La cobertura todavía no demuestra una prueba unitaria de cada clase de dominio y servicio, por lo que esa parte debe leerse junto con las limitaciones señaladas en el informe.”

## 08:00–09:00 — Repositorio y tablero del Sprint 2

**Presenta:** todos los integrantes; cada uno muestra brevemente su participación

**En pantalla:** repositorio `https://github.com/Mezitas/ProyectoIngSoftware2_BancoPreguntas`, historial de commits filtrado por autor y captura real del tablero Sprint 2 (Jira o Trello).

**Guion:** “Este es el repositorio de la iteración. Mostramos los commits atribuibles a cada integrante y el tablero del Sprint 2 con tareas y estados. Las capturas incluidas en el informe se reemplazan por capturas actuales del tablero.”

## 09:00–10:00 — API REST documentada con Swagger

**Presenta:** [integrante 2]

**En pantalla:** abrir `http://localhost:8083/swagger-ui/index.html` (Question Service), expandir una operación REST y probarla; opcionalmente mostrar `http://localhost:8083/v3/api-docs` para el JSON OpenAPI. Los otros servicios publican la misma interfaz en su propio puerto, de `8081` a `8086`.

**Guion:** “Cada microservicio integra springdoc-openapi y publica su propia especificación OpenAPI y Swagger UI. Aquí vemos los endpoints y contratos del Question Service y ejecutamos una solicitud de ejemplo. No existe un API Gateway ni una documentación centralizada; cada servicio documenta su API REST.”

## 10:00–15:00 — Software funcionando y evidencia de código

**Presenta:** todos los integrantes, distribuyendo la demostración

**10:00–11:00 — Inicio de sesión y roles.** Iniciar con `docente1 / Docente123*`; mostrar los datos del usuario y las pestañas de autor. En `../backend/user-service/src/main/java/co/edu/unicauca/user/config/DataSeeder.java` están las cuentas de prueba; `../backend/user-service/src/main/java/co/edu/unicauca/user/controller/AuthController.java` expone el login.

**11:00–12:00 — Crear, validar y listar.** Crear una pregunta con contexto, enunciado, cuatro opciones, clave, justificación, bibliografía, competencia, tema, subtema y dificultad. Mostrar el estado `DRAFT`, los filtros y la paginación. Evidencia: `../frontend/src/main/java/com/bancopreguntas/desktop/BancoPreguntasApp.java`; `../backend/question-service/src/main/java/co/edu/unicauca/question/service/QuestionService.java`; `../backend/validation-service/src/main/java/co/edu/unicauca/validation/service/ValidationService.java`.

**12:00–13:00 — Envío a revisión.** Seleccionar el borrador, pulsar “Enviar a revisión”, consultar la operación y actualizar el listado hasta ver `PENDING_REVIEW`. Evidencia: `../backend/question-service/src/main/java/co/edu/unicauca/question/messaging/CommandPublisher.java` y `../backend/lifecycle-service/src/main/java/co/edu/unicauca/lifecycle/service/CommandProcessor.java`.

**13:00–14:00 — Asignación y evaluación.** Entrar como `admin1 / Admin123*`, asignar revisores 3 y/o 4; iniciar sesión como `revisor1 / Revisor123*` o `revisor2 / Revisor123*`, evaluar y escribir observaciones. Evidencia: `../backend/review-service/src/main/java/co/edu/unicauca/review/service/ReviewService.java`; la interfaz muestra asignaciones en “Mis revisiones”.

**14:00–15:00 — Estado final y notificaciones.** Mostrar consenso, `APPROVED` o `REJECTED`, observaciones y notificaciones. Aclarar que el servicio de correo de ejemplo simula el envío y lo registra en el log; no envía correo real. Evidencia: `../backend/notification-service/src/main/java/co/edu/unicauca/notification/messaging/NotificationListener.java`, `../backend/notification-service/src/main/java/co/edu/unicauca/notification/service/EmailService.java` y `../frontend/src/main/java/com/bancopreguntas/desktop/BancoPreguntasApp.java`.

## Lista de preparación para grabar

- Reemplazar nombres, distribuir la participación y ensayar para mantener los 15 minutos.
- Capturar el tablero real del Sprint 2 y comprobar que se ve la contribución de todos.
- Iniciar los servicios que se mostrarán y verificar que Swagger UI y `/v3/api-docs` respondan.
- Mostrar Factory Method en `QuestionEvaluatorCreator` y sus creadores concretos; ejecutar su prueba unitaria.
- Preparar servicios, RabbitMQ y usuarios; las bases H2 de ejemplo se reinician en memoria.
- Completar el URL público de YouTube en el informe después de publicar el video.
