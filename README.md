# Frontend

La carpeta `frontend` contiene una aplicación de escritorio Java 17 con Swing. No es una página web: no usa navegador, Node.js, Vite ni un servidor para entregar la interfaz. El cliente se ejecuta como proceso independiente y consume las APIs REST de los microservicios mediante `java.net.http.HttpClient`; no importa clases ni comparte base de datos con el backend. Al ser una aplicación de escritorio, no requiere configuración CORS.

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

**Importante:** `docker compose up -d` por sí solo no inicia el backend REST; el `docker-compose.yml` del ejemplo únicamente crea RabbitMQ. Si puedes iniciar sesión pero no cargar preguntas, el servicio de usuarios responde, pero revisa que `question-service` también esté iniciado.

Para arrancar el backend en Windows, desde la raíz del repositorio ejecuta:

```powershell
$backend = Resolve-Path ".\backend"
Set-Location $backend
docker compose up -d
.\start-all.bat
```

El script abre una terminal por microservicio; mantenlas abiertas y espera a que Maven termine de iniciar. RabbitMQ en `http://localhost:15672` no es el backend de la aplicación. Verifica las APIs REST en `http://localhost:8081`, `http://localhost:8083`, `http://localhost:8085` y `http://localhost:8086`. La aplicación debe iniciarse en la misma máquina o configurarse con las variables de entorno si los servicios están en otra dirección.

La pantalla de inicio de sesión autentica contra `POST /auth/login`. Según los roles devueltos por el servicio, muestra las funciones de autor (`DOCENTE`/`AUTOR`), administrador (`ADMIN`) y revisor (`REVISOR`), además de notificaciones. Incluye creación y edición de preguntas con contexto, enunciado, cuatro opciones, respuesta, justificación, bibliografía, competencia, tema, subtema y dificultad; estados visualizados con colores; envío a revisión; listado con búsqueda, filtros y paginación; asignación de revisores; evaluación con aprobación/rechazo y observaciones; consulta de notificaciones.

Las llamadas REST se distribuyen directamente entre usuarios (`/auth/login`, puerto 8081), preguntas y operaciones asíncronas (puerto 8083), revisiones y asignaciones (puerto 8085) y notificaciones (puerto 8086). Validación y ciclo de vida son invocados por los servicios backend durante sus flujos; la aplicación de escritorio no se conecta a sus bases de datos.

El backend ahora devuelve el estado consultado al servicio de ciclo de vida en el listado, persiste bibliografía y dificultad, ofrece consultas de asignaciones por revisor y evaluaciones con sus observaciones, admite de uno a tres revisores y notifica el resultado final a los implicados. La búsqueda y paginación del listado de la aplicación de escritorio se aplican localmente sobre las preguntas devueltas por la API.

La Figura 1 también presenta los contextos de simulacros, seguimiento académico y trazabilidad. No hacen parte de los seis microservicios de referencia ni de sus APIs disponibles, así que no se exponen como funciones en esta interfaz.

# Backend

El backend está en `backend`; incluye los seis servicios Java/Spring Boot separados para usuarios (8081), validación (8082), preguntas (8083), ciclo de vida (8084), revisión (8085) y notificaciones (8086). El frontend no inicia ni administra estos servicios: deben estar activos y accesibles para poder iniciar sesión y usar las funciones correspondientes.

Consulta el README de esa carpeta para los requisitos y comandos de ejecución. En particular, inicia RabbitMQ con `docker compose up -d` desde esa carpeta y luego levanta los seis servicios; en Windows el ejemplo incluye `start-all.bat`. Las credenciales incluidas son `docente1 / Docente123*`, `admin1 / Admin123*` y `revisor1` o `revisor2 / Revisor123*`.
Cada microservicio publica su propia interfaz Swagger UI en `http://localhost:<puerto>/swagger-ui/index.html` y su especificación OpenAPI en `/v3/api-docs`; consulta la tabla de URLs en `backend/README.md`.

La separación es física en procesos y lógica en código: el cliente Java realiza llamadas HTTP a las APIs REST; los microservicios conservan la lógica de negocio, persistencia, validación y mensajería. No es necesario instalar o ejecutar un servidor frontend. Sí se necesita ejecutar el backend (y RabbitMQ para sus operaciones asíncronas) en una máquina accesible desde la aplicación.

Los documentos de sustentación del segundo corte están en [`Guion_Video_Segundo_Corte.md`](Guion_Video_Segundo_Corte.md) y [`Informe_Arquitectura_Segundo_Corte.md`](Informe_Arquitectura_Segundo_Corte.md). El guion señala los requisitos que deben completarse antes de presentar la demostración.
