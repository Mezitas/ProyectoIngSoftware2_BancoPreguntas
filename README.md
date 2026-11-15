# Frontend

La carpeta `frontend` contiene una aplicación de escritorio Java 17 con Swing. No es una página web: no usa navegador, Node.js, Vite ni un servidor para entregar la interfaz. El cliente se ejecuta como proceso independiente y consume las APIs REST de los microservicios mediante `java.net.http.HttpClient`; no importa clases ni comparte base de datos con el backend. Al ser una aplicación de escritorio, no requiere configuración CORS.

## Entrega del tercer corte: arquitectura y autenticación

El backend conserva seis microservicios independientes y se ejecuta junto con Keycloak y RabbitMQ usando Docker Compose. Keycloak es el proveedor de identidad: emite JWT, y cada API valida firma, emisor y expiración antes de aceptar solicitudes. Los permisos se derivan de roles del realm (`DOCENTE`, `AUTOR`, `ADMIN`, `REVISOR`, `ESTUDIANTE`). El cliente de escritorio inicia sesión por User Service; éste delega la autenticación a Keycloak y devuelve el perfil local y el access token. El cliente adjunta el token como `Authorization: Bearer ...` a las llamadas posteriores.

El microservicio `lifecycle-service` aplica DDD en el núcleo de las reglas de ciclo de vida. `domain/QuestionLifecycle` es el agregado que protege las transiciones; `port/` declara las salidas requeridas por el caso de uso y `adapter/persistence/` convierte entre el dominio y JPA. `service/LifecycleService` coordina registro, transiciones e historial. Las reglas y el agregado se prueban sin iniciar Spring ni conectarse a una base de datos.

### Ejecutar el backend completo en Windows

Requisitos: Docker Desktop con Docker Compose v2. No es necesario instalar Java ni Maven para levantar los contenedores: las imágenes compilan los servicios. Para modificar y probar código Java, instala JDK 17+ y Maven 3.8+.

```powershell
Set-Location .\backend
docker compose up --build -d
docker compose ps
```

La primera ejecución descarga las imágenes y puede tardar varios minutos. Espera a que los servicios estén `Up` antes de abrir el cliente de escritorio. Alternativamente, desde `backend`, ejecuta `.\start-all.bat`. Para detener el sistema sin perder la información usa `docker compose down`; las bases H2 persisten en volúmenes con nombre. **No uses `docker compose down -v` si quieres conservar los datos.**

Direcciones: Keycloak `http://localhost:8180`, RabbitMQ `http://localhost:15672` y APIs REST en los puertos `8081`–`8086`. La consola de RabbitMQ usa las credenciales locales `saberpro / SaberProRabbit123!`; el administrador de Keycloak usa `admin / AdminKeycloak123!`. Son credenciales de demostración, no deben utilizarse en producción.

El realm `saberpro` se importa desde `backend/keycloak/realm-saberpro.json` con cuentas de demostración: `docente1 / Docente123*`, `admin1 / Admin123*`, `revisor1` y `revisor2 / Revisor123*`, `estudiante1 / Estudiante123*`. Los usuarios del realm deben tener también un perfil en User Service para usar las funciones del banco. Cambia las contraseñas y secretos de desarrollo antes de exponer el sistema fuera del equipo local.

## Ejecución en Windows

Con JDK 17+ y Maven 3.8+:

```powershell
cd frontend
mvn.cmd clean package
java -jar target\banco-preguntas-desktop.jar
```

También se puede iniciar directamente desde `frontend` con `mvn.cmd exec:java`. La primera compilación descarga Jackson y el plugin de empaquetado. El JAR generado incluye sus dependencias.

Por defecto, el cliente se conecta a `localhost` en los puertos del ejemplo. Si los servicios usan otras direcciones, se pueden configurar antes de iniciar la aplicación:

```powershell
$env:BANCO_API_USERS_URL = "http://localhost:8081"
$env:BANCO_API_QUESTIONS_URL = "http://localhost:8083"
$env:BANCO_API_REVIEWS_URL = "http://localhost:8085"
$env:BANCO_API_NOTIFICATIONS_URL = "http://localhost:8086"
java -jar target\banco-preguntas-desktop.jar
```

**Importante:** ahora `docker compose up --build -d` desde `backend` levanta Keycloak, RabbitMQ y los seis microservicios. Para iniciar el backend en Windows:

```powershell
Set-Location .\backend
.\start-all.bat
```

Verifica el estado con `docker compose ps` y los mensajes con `docker compose logs -f`. RabbitMQ y Keycloak son dependencias, no reemplazan a los seis servicios REST. Las direcciones del cliente de escritorio se pueden ajustar mediante las variables de entorno `BANCO_API_USERS_URL`, `BANCO_API_QUESTIONS_URL`, `BANCO_API_REVIEWS_URL` y `BANCO_API_NOTIFICATIONS_URL`.

La pantalla de inicio de sesión autentica contra `POST /auth/login`. Según los roles devueltos por el servicio, muestra las funciones de autor (`DOCENTE`/`AUTOR`), administrador (`ADMIN`) y revisor (`REVISOR`), además de notificaciones. Incluye creación y edición de preguntas con contexto, enunciado, cuatro opciones, respuesta, justificación, bibliografía, competencia, tema, subtema y dificultad; estados visualizados con colores; envío a revisión; listado con búsqueda, filtros y paginación; asignación de revisores; evaluación con aprobación/rechazo y observaciones; consulta de notificaciones.

Las llamadas REST se distribuyen directamente entre usuarios (`/auth/login`, puerto 8081), preguntas y operaciones asíncronas (puerto 8083), revisiones y asignaciones (puerto 8085) y notificaciones (puerto 8086). Validación y ciclo de vida son invocados por los servicios backend durante sus flujos; la aplicación de escritorio no se conecta a sus bases de datos. El JWT de Keycloak se adjunta a todas las llamadas de negocio y se elimina al cerrar sesión.

El backend ahora devuelve el estado consultado al servicio de ciclo de vida en el listado, persiste bibliografía y dificultad, ofrece consultas de asignaciones por revisor y evaluaciones con sus observaciones, admite de uno a tres revisores y notifica el resultado final a los implicados. La búsqueda y paginación del listado de la aplicación de escritorio se aplican localmente sobre las preguntas devueltas por la API.

La Figura 1 también presenta los contextos de simulacros, seguimiento académico y trazabilidad. No hacen parte de los seis microservicios de referencia ni de sus APIs disponibles, así que no se exponen como funciones en esta interfaz.

# Backend

El backend está en `backend`; incluye los seis servicios Java/Spring Boot separados para usuarios (8081), validación (8082), preguntas (8083), ciclo de vida (8084), revisión (8085) y notificaciones (8086), además de Keycloak y RabbitMQ. El frontend no inicia ni administra estos servicios: deben estar activos y accesibles para poder iniciar sesión y usar las funciones correspondientes.

Consulta [`backend/README.md`](backend/README.md) para instrucciones detalladas, autenticación desde Postman, roles, endpoints y pruebas. Las credenciales locales de demostración están declaradas en el realm de Keycloak.
Cada microservicio publica su propia interfaz Swagger UI en `http://localhost:<puerto>/swagger-ui/index.html` y su especificación OpenAPI en `/v3/api-docs`; consulta la tabla de URLs en `backend/README.md`.

La separación es física en procesos y lógica en código: el cliente Java realiza llamadas HTTP a las APIs REST; los microservicios conservan la lógica de negocio, persistencia, validación y mensajería. No es necesario instalar o ejecutar un servidor frontend. Sí se necesita ejecutar el backend (y RabbitMQ para sus operaciones asíncronas) en una máquina accesible desde la aplicación.

Los documentos de sustentación del segundo corte están en [`EntregableSegundoCorte/Guion_Video_Segundo_Corte.md`](EntregableSegundoCorte/Guion_Video_Segundo_Corte.md) y [`Informe_Arquitectura_Segundo_Corte.md`](Informe_Arquitectura_Segundo_Corte.md). El guion señala los requisitos que deben completarse antes de presentar la demostración.
