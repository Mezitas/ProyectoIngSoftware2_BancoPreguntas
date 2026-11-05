# Taller 09 – Microservicios Saber PRO (REST + RabbitMQ)

Laboratorio de Ingeniería de Software II · Universidad del Cauca · Grupo 1
Joseph David Trujillo Gómez · Juan Pablo Hernández

Evolución del Banco de Preguntas Saber PRO (Taller 8) a 6 microservicios Spring Boot 3.3 / Java 17
con comunicación **síncrona (REST)** y **asíncrona (RabbitMQ)**.

| Servicio | Puerto | Responsabilidad | Comunicación |
|---|---|---|---|
| user-service | 8081 | Autenticación y roles | REST (`POST /auth/login`, `GET /users/{id}`) |
| validation-service | 8082 | Valida estructura de la pregunta | REST (`POST /validate`) |
| question-service | 8083 | Contenido de la pregunta y flujo | REST + comando RabbitMQ |
| lifecycle-service | 8084 | **Única autoridad del estado** (patrón State del Taller 8) | Listener RabbitMQ + REST |
| review-service | 8085 | Asignaciones y resultados de revisión | REST + evento RabbitMQ |
| notification-service | 8086 | Notificaciones sin bloquear el flujo | Listener RabbitMQ |

## Requisitos
JDK 17+ (recomendado 17 o 21), Maven 3.8+, Docker (para RabbitMQ), Postman.

## Ejecución
```bash
docker compose up -d          # RabbitMQ  -> consola http://localhost:15672 (guest/guest)
# un terminal por servicio (o start-all.bat en Windows / ./start-all.sh en Linux/macOS)
cd user-service && mvn spring-boot:run
cd validation-service && mvn spring-boot:run
cd question-service && mvn spring-boot:run
cd lifecycle-service && mvn spring-boot:run
cd review-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```
Compilar todo: `mvn clean package` (raíz) · Pruebas unitarias: `mvn test`.

Importar en Postman `postman/SaberPro_Microservices.postman_collection.json` y
`postman/SaberPro_Local.postman_environment.json`; elegir el entorno *SaberPro Local* y ejecutar la
colección completa con **Collection Runner** (con servicios recién iniciados: las BD H2 son en memoria).

## Usuarios de prueba
`docente1 / Docente123*` · `admin1 / Admin123*` · `revisor1`, `revisor2 / Revisor123*` · `estudiante1 / Estudiante123*`

## Flujo
1. `POST /auth/login` → 200 / 401.
2. `POST /questions` → Question llama a User (rol) y Validation (reglas) → 201 DRAFT o 400.
3. `POST /questions/{id}/submit` → publica `SubmitQuestionForReview` → **202 Accepted** + `operationId`.
4. Lifecycle consume el comando, valida DRAFT→PENDING_REVIEW y publica el resultado;
   consultar `GET /operations/{operationId}` → `COMPLETED` / `FAILED`.
5. `POST /reviews/assign` (admin) → verifica pregunta y revisor, pide IN_REVIEW a Lifecycle,
   registra la asignación (con compensación si falla) y publica `ReviewerAssigned`.
6. Notification consume el evento y simula el envío: `GET /notifications?recipientId=3`.
7. `POST /reviews/result` → APPROVED / REJECTED (con motivo). `PUT /questions/{id}` corrige una rechazada.

## Ajustes funcionales para los entregables

Se conserva el contenido y las instrucciones anteriores. Estos ajustes completan el flujo del segundo corte:

- `GET /questions` consulta el estado de cada pregunta en Lifecycle Service antes de responder. Si una pregunta persistida todavía no tiene registro de ciclo de vida, la registra idempotentemente en `DRAFT`; si Lifecycle no responde, devuelve `UNAVAILABLE` como señal real de indisponibilidad, no como estado permanente. Crear una pregunta registra el estado inicial `DRAFT`, enviarla a revisión transiciona a `PENDING_REVIEW`, y la asignación del revisor a `IN_REVIEW`.
- Las preguntas persisten `bibliography` y `difficulty` en Question Service; ambos datos se incluyen en las respuestas REST. `difficulty` se normaliza a `BASIC`, `MEDIUM` o `HIGH`. Validation Service exige bibliografía y uno de esos niveles, además del contenido estructural existente. Hibernate actualiza el esquema H2 en memoria al arrancar (`ddl-auto: update`); al reiniciar los servicios las bases H2 siguen perdiéndose, como se indica en las decisiones de diseño.
- `POST /reviews/assign` admite hasta tres revisores distintos para una pregunta. La primera asignación solicita el cambio a `IN_REVIEW`; las siguientes mantienen ese estado y publican su propia notificación. Se rechazan revisores duplicados, el autor como revisor y una cuarta asignación activa.
- `GET /reviews/reviewer/{reviewerId}` lista las asignaciones activas de un revisor. `GET /reviews/question/{questionId}/results` expone las evaluaciones y sus observaciones para el autor.
- Cuando una pregunta tiene varios revisores, conserva `IN_REVIEW` hasta que todos registren su evaluación. El resultado final es `REJECTED` si algún revisor rechazó; en caso contrario, `APPROVED`. Una vez finalizada, se publican notificaciones de resultado para el autor, administradores que asignaron revisores y revisores participantes.

Recompila y reinicia los servicios afectados (`question-service`, `validation-service`, `review-service` y `notification-service`) para cargar los cambios. Como las bases son H2 en memoria, al reiniciar los datos de prueba existentes se pierden y hay que volver a crear las preguntas/asignaciones. La dificultad y bibliografía se validan también al importar reactivos externos; la adaptación los marca con una referencia basada en el identificador de origen y nivel `MEDIUM`, ya que el proveedor de ejemplo no incluye esos campos.

## Decisiones de diseño
- Database per Service (H2 en memoria por servicio; cambiar a PostgreSQL = cambiar `application.yml`).
- Question Service no guarda estado; lo consulta a Lifecycle.
- Colas durables declaradas idénticas en productor y consumidor; consumidores idempotentes.
- Sin JWT en esta versión (corte 3): `Authorization` se acepta pero no se evalúa.
- Variables por servicio: `SERVER_PORT`, `RABBIT_HOST`, `USER_SERVICE_URL`, `LIFECYCLE_SERVICE_URL`, `EMAIL_DELAY_MS`...


## Patrones de Diseño Integrados (Fusión Taller 08 + Taller 09)

| Patrón | Servicio | Paquete / Clases | Función | Exposición / Endpoints |
|---|---|---|---|---|
| **State** (con `REJECTED`) | `lifecycle-service` | `state/` | Controla las transiciones de estado de las preguntas | REST + RabbitMQ |
| **Builder & Director** | `question-service` | `builder/`, `domain/model/` | Construye y valida objetos complejos de pregunta de 4 opciones | Usado en creación, edición e importación |
| **Adapter** | `question-service` | `adapter/` | Adapta fuentes externas (JSON y cadenas crudas) al modelo de preguntas | `POST /questions/import?authorId={id}` |
| **Facade** | `question-service` | `facade/QuestionBankFacade` | Punto de entrada unificado para operaciones del banco de preguntas | Toda la API en `/questions` |
| **Decorator** | `question-service` & `validation-service` | `decorator/` | Enriquece preguntas con alta dificultad y retroalimentación técnica | `GET /questions/{id}/presentation` |
| **Template Method** | `validation-service` | `templatemethod/` | Plantilla de evaluación para Selección Múltiple, V/F y Casos de Estudio | `POST /evaluate` |

### Nuevos Endpoints de Patrones
- `POST /questions/import?authorId=1` → Importa reactivos externos (JSON + formato crudo), omitiendo duplicados e inválidos.
- `GET /questions/{id}/presentation?altaDificultad=true&retroalimentacion=...` → Entrega la presentación enriquecida de la pregunta (Decorator).
- `POST /evaluate` → Evalúa respuestas utilizando Template Method según el tipo de pregunta.
