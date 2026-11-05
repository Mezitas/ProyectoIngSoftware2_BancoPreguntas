# Documento 1 — `faltante.docx`

## 1. No hay frontend

Todo el proyecto es backend. El protocolo exige mostrar "el frontend y el backend separados y comunicados por una API REST", y 5 minutos del video son para mostrar el software funcionando. Esto también afecta directamente el cumplimiento de los requisitos funcionales (30%). Sin interfaz no se cumplen cosas como "los estados se deben visualizar con colores", y en el video solo podrían mostrar Postman.

## 2. Swagger / OpenAPI

No hay `springdoc` en ningún `pom.xml` ni anotaciones. Es un punto explícito del video (1 min). Se arregla agregando la dependencia `springdoc-openapi-starter-webmvc-ui` a cada servicio. Si tienen un gateway o un frontend que consuma varios servicios, conviene dejar claro cuál Swagger muestran.

## 3. Requisitos funcionales incompletos

|Historia de usuario|Estado|
|---|---|
|Crear pregunta|**Parcial.** La entidad y la validación no incluyen **bibliografía** ni **nivel de dificultad**, que sí piden las HU.|
|Pasar de borrador a pendiente|Cubierta (`POST /questions/{id}/submit` con RabbitMQ). Falta la visualización con colores, que depende del frontend.|
|Listar mis preguntas|**Falta.** `GET /questions` devuelve todas las preguntas, sin filtro por autor, **sin paginación** y **sin filtros** (competencia, tema, estado, etc.).|
|Asignar de 1 a 3 revisores|**Contradice la HU.** `ReviewService.assign` lanza conflicto si la pregunta ya tiene un revisor, así que solo se puede asignar uno. Además, tras la primera asignación la pregunta pasa a `IN_REVIEW`, y el servicio exige `PENDING_REVIEW`, por lo que un segundo revisor es imposible.|
|Email al asignar|**Simulado.** `EmailService` solo hace `Thread.sleep` y un log. Para el corte conviene un envío real (Spring Mail con Mailtrap o Gmail SMTP) o decir claramente que es simulado.|
|Revisor lista sus asignadas|**Falta.** No hay endpoint para consultar por `reviewerId`. Solo existen `/reviews/question/{id}` y `/reviews/assignments/{id}`.|
|Observaciones visibles para el autor|**Falta.** Los comentarios se guardan en `ReviewEntity`, pero ningún endpoint los expone.|
|Notificar a los implicados al aprobar o rechazar|**Falta.** Solo se notifica al revisor cuando es asignado. El autor y el admin no reciben nada al aprobar o rechazar.|

## 4. Patrones GoF: falta un creacional

La rúbrica pide 2 creacionales, 2 estructurales y 2 de comportamiento. Lo que tienen:

- **Creacional:** Builder. Solo hay uno. `QuestionEvaluatorFactory` parece una fábrica simple, que no es un patrón GoF. Podrían convertirla en **Factory Method** o **Abstract Factory**, o agregar **Singleton** o **Prototype** donde tenga sentido.
    
- **Estructural:** Adapter, Facade, Decorator (cumple, incluso sobra uno).
    
- **Comportamiento:** State, Template Method (cumple).
    

## 5. Pruebas unitarias (20%)

La rúbrica pide pruebas a _todas_ las clases del dominio (entidades y servicios). Hoy hay 46 tests aprox., distribuidos así:

- `review-service`: **0 pruebas**, y es el servicio con más lógica de negocio (asignación, compensación, resultados).
    
- `notification-service`: **0 pruebas**.
    
- `question-service`: no hay pruebas de `QuestionService` ni `QuestionBankFacade`, solo de Builder, Decorator y Adapter.
    
- `user-service`: solo `PasswordHasher`; falta `UserService`.
    
- `lifecycle-service`: solo `LifecycleContextTest`; falta `LifecycleService` y `CommandProcessor`.
    

## 6. Documento de arquitectura (20%)

En el zip no hay ningún documento, solo un README. Les falta todo esto:

- Portada e introducción.
    
- Historias de usuario con criterios de aceptación (las de la 2ª iteración).
    
- Pantallazo de planificación del Sprint 2 (Jira/Trello).
    
- **Escenario de calidad de escalabilidad** (contexto, estímulo, respuesta, medición, resultado esperado).
    
- Arquitectura con **modelo C4** (contexto, contenedores, componentes) y UML.
    
- **Diagrama de bounded contexts**.
    
- Listado de los 6 patrones con cómo y dónde se aplicaron (el README ya tiene una buena base).
    
- URL del video y del repositorio (que ya mencionaron que les falta).
    

## 7. Detalles menores

- El README solo menciona a 2 integrantes. El video debe mostrar commits de **todos**; revisen que todos hayan hecho commits.
    
- El README dice "Taller 09" y "Grupo 1". Ajústenlo al nombre del proyecto del segundo corte.
    
- Falta el tablero de tareas del sprint, que se muestra en el video y va en el documento.
    
- Las bases de datos son H2 en memoria y se pierden al reiniciar. Es válido por el patrón Database per Service, pero conviene aclararlo en el documento.