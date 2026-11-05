# Guion de sustentación — Entrega 1

## 1. Historias de usuario, criterios de aceptación, prototipos y test de usabilidad — 2 minutos

### HU01 — Crear preguntas de selección múltiple

**Requisito funcional:** el autor debe crear una pregunta Saber Pro con contexto, pregunta directa, cuatro distractores, respuesta correcta, justificación, bibliografía, competencia, tema, subtema y nivel de dificultad. Al guardar, la pregunta debe cumplir una validación estructural y quedar inicialmente en estado `Borrador`.

**Partes del código que evidencian el requisito:**

- `src/main/java/com/bancopreguntas/ui/PanelCrearPregunta.java`
  - La clase representa la vista Swing de creación y edición.
  - `construirUI()` crea los controles para todos los campos exigidos:
    - `txtContexto`
    - `txtPreguntaDirecta`
    - `txtDistractores[4]`
    - `txtRespuestaCorrecta`
    - `txtJustificacion`
    - `txtBibliografia`
    - `txtCompetencia`
    - `txtTema`
    - `txtSubtema`
    - `cmbNivelDificultad`
  - El arreglo `txtDistractores` tiene exactamente cuatro posiciones, por lo que la interfaz solicita las cuatro opciones del enunciado.
  - `guardar()` toma los valores de los controles y construye un objeto `Pregunta` mediante `Pregunta.builder()`.
  - `autorId(SesionActual.getUsuarioActual().getId())` vincula la pregunta con el usuario autenticado.
  - Si el formulario es nuevo, `controller.crearPregunta(contenido)` envía el objeto al controlador.
  - Si se está editando una pregunta, `controller.actualizarPregunta(...)` utiliza el mismo formulario con un flujo distinto.
  - La excepción `ValidacionException` se muestra en `lblMensaje`, incluyendo la lista de errores estructurales.

- `src/main/java/com/bancopreguntas/domain/Pregunta.java`
  - La clase contiene los atributos correspondientes al modelo de la pregunta.
  - `Pregunta.Builder` permite construir el objeto campo por campo.
  - El atributo `estado` inicia por defecto como `EstadoPregunta.BORRADOR`.
  - El constructor copia las listas de distractores y revisores, evitando que el objeto dependa directamente de la lista externa recibida.
  - `getDistractores()` y `getRevisoresAsignados()` entregan vistas no modificables.
  - `fechaCreacion` y `fechaActualizacion` se crean automáticamente si no se reciben desde la persistencia.

- `src/main/java/com/bancopreguntas/validacion/ValidadorPregunta.java`
  - Define la interfaz de la estrategia de validación.
  - La capa de negocio depende de esta interfaz y no de una implementación concreta.

- `src/main/java/com/bancopreguntas/validacion/ValidadorEstructuralPregunta.java`
  - `validar(Pregunta pregunta)` verifica que contexto, pregunta directa, justificación, bibliografía, competencia, tema y subtema no estén vacíos.
  - Verifica que exista un nivel de dificultad.
  - `validarDistractoresYRespuesta(...)` exige exactamente cuatro distractores.
  - Usa un `HashSet` para comprobar que los cuatro distractores sean diferentes.
  - Normaliza los textos con `trim().toLowerCase()` para detectar duplicados aunque cambien espacios o mayúsculas.
  - Verifica que la respuesta correcta exista y no coincida con ningún distractor.
  - Verifica que exista un autor asociado.
  - Devuelve `ResultadoValidacion.ok()` cuando no hay errores o `ResultadoValidacion.error(errores)` cuando hay uno o varios errores.

- `src/main/java/com/bancopreguntas/service/PreguntaServiceImpl.java`
  - `crearPregunta(Pregunta pregunta, Usuario actor)` concentra la regla de negocio.
  - `exigirRol(actor, Rol.AUTOR)` limita la operación al rol autor.
  - Comprueba que el autor autenticado coincida con `pregunta.getAutorId()`.
  - Impide crear una pregunta con un estado diferente de `BORRADOR` o con revisores previamente asignados.
  - Impide reutilizar un identificador ya existente.
  - Ejecuta `validar(pregunta)` antes de llamar al repositorio.
  - Guarda únicamente después de aprobar la validación.
  - Registra la operación de creación mediante `registrar(..., "CREAR", ...)`.

- `src/main/java/com/bancopreguntas/ui/PreguntaController.java`
  - `crearPregunta(Pregunta pregunta)` conecta la vista con el servicio y agrega el usuario de la sesión como actor.
  - Las vistas no llaman directamente a SQLite ni a los repositorios.

**Criterios de aceptación que se pueden mostrar:**

1. Un autor puede abrir la pestaña `Crear pregunta`.
2. El formulario muestra todos los campos requeridos.
3. Una pregunta válida se guarda en estado `Borrador`.
4. Una pregunta con campos vacíos muestra los errores de validación.
5. Una pregunta con menos o más de cuatro distractores es rechazada.
6. Una pregunta con distractores repetidos es rechazada.
7. Una respuesta correcta igual a un distractor es rechazada.
8. La pregunta queda asociada al autor que inició sesión.

### HU02 — Enviar preguntas a revisión y visualizar estados

**Requisito funcional:** el autor debe cambiar una pregunta propia de `Borrador` a `Pendiente de revisión`; los estados deben aparecer visualmente diferenciados por colores.

**Partes del código:**

- `src/main/java/com/bancopreguntas/ui/PanelListarPreguntas.java`
  - La tabla muestra la columna `Estado`.
  - `btnEnviarRevision` permite ejecutar la acción sobre la pregunta seleccionada.
  - `enviarARevision()` llama a `controller.enviarARevision(...)`.
  - `actualizarAcciones()` habilita el botón únicamente cuando la pregunta seleccionada está en `BORRADOR`.
  - La interfaz no habilita enviar a revisión una pregunta ya enviada, aprobada, rechazada o eliminada.

- `src/main/java/com/bancopreguntas/ui/PreguntaController.java`
  - `enviarARevision(String preguntaId)` solicita al servicio el cambio a `EstadoPregunta.PENDIENTE_REVISION`.

- `src/main/java/com/bancopreguntas/domain/EstadoPregunta.java`
  - Define los estados del ciclo de vida.
  - Cada estado tiene una etiqueta y un color.
  - `BORRADOR` permite transicionar a `PENDIENTE_REVISION` o `ELIMINADA`.
  - `PENDIENTE_REVISION` permite pasar a `EN_REVISION`, volver a `BORRADOR` o marcarse como `ELIMINADA`.
  - `EN_REVISION` permite pasar a `APROBADA`, `RECHAZADA` o `ELIMINADA`.
  - `RECHAZADA` permite volver a `BORRADOR` o pasar a `ELIMINADA`.
  - `APROBADA` y `ELIMINADA` no tienen transiciones posteriores.
  - `puedeTransicionarA(...)` centraliza la validación del flujo de estados.

- `src/main/java/com/bancopreguntas/ui/EstadoBadgeRenderer.java`
  - Personaliza la celda de estado de la tabla.
  - Usa `estado.getColor()` como color de fondo.
  - Usa `colorTextoLegible(...)` para escoger texto negro o blanco según la luminancia del fondo.
  - Muestra la etiqueta legible del estado en lugar del nombre técnico del enum.

- `src/main/java/com/bancopreguntas/service/PreguntaServiceImpl.java`
  - `cambiarEstado(...)` verifica que el actor tenga rol autor.
  - `exigirPropietario(...)` impide modificar preguntas de otro autor.
  - `puedeTransicionarA(...)` impide saltos inválidos, como pasar directamente de borrador a aprobada.
  - Solo permite al autor solicitar los estados `PENDIENTE_REVISION` o `ELIMINADA`.
  - Registra el cambio en auditoría con la acción `CAMBIAR_ESTADO`.

**Estados que se pueden señalar en pantalla:**

| Estado | Uso en el flujo | Color definido |
|---|---|---|
| `BORRADOR` | Pregunta creada o editada por el autor, todavía no enviada | Gris |
| `PENDIENTE_REVISION` | Pregunta enviada por el autor y esperando asignación | Naranja |
| `EN_REVISION` | Pregunta asignada a uno o más revisores | Azul |
| `APROBADA` | Pregunta aprobada por un revisor asignado | Verde |
| `RECHAZADA` | Pregunta devuelta por un revisor | Rojo |
| `ELIMINADA` | Pregunta retirada por el autor desde un estado permitido | Gris oscuro |

### HU03 — Consultar, filtrar, paginar, visualizar y editar preguntas propias

**Requisito funcional:** el autor debe consultar únicamente sus preguntas, aplicar filtros, recorrer páginas, ver todos los campos y editar preguntas en `Borrador` o `Rechazada`. Una pregunta rechazada corregida debe volver a `Borrador`.

**Partes del código:**

- `src/main/java/com/bancopreguntas/ui/PanelListarPreguntas.java`
  - Define `TAMANO_PAGINA = 5`.
  - Incluye campos de búsqueda por texto, tema y subtema.
  - Incluye filtros por nivel de dificultad y estado.
  - `cargar()` construye un `FiltroPregunta` y solicita el resultado paginado al controlador.
  - La tabla muestra tema, subtema, competencia, nivel, pregunta y estado.
  - Los botones `Anterior` y `Siguiente` controlan el cambio de página.
  - `lblPagina` muestra la página actual, el total de páginas y el número total de preguntas.
  - `verDetalle()` muestra contexto, pregunta, distractores, respuesta, justificación, bibliografía, competencia, tema, subtema, dificultad y estado.
  - `actualizarAcciones()` habilita editar y eliminar solamente para preguntas en `BORRADOR` o `RECHAZADA`.
  - `editarPregunta()` entrega la pregunta seleccionada a `VentanaPrincipal`, que abre nuevamente `PanelCrearPregunta` en modo edición.

- `src/main/java/com/bancopreguntas/service/FiltroPregunta.java`
  - Representa los criterios opcionales de búsqueda.
  - Permite filtrar por texto, tema, subtema, competencia, dificultad y estado.

- `src/main/java/com/bancopreguntas/service/ResultadoPaginado.java`
  - Encapsula los elementos de la página, número de página, tamaño, total de elementos y total de páginas.
  - También informa si existe página anterior o siguiente.

- `src/main/java/com/bancopreguntas/service/PreguntaServiceImpl.java`
  - `listarPreguntasPorAutor(...)` consulta el repositorio con el id del usuario autenticado.
  - Aplica los filtros de texto, tema, subtema, competencia, dificultad y estado.
  - Ordena por fecha de creación descendente.
  - Calcula los índices `desde` y `hasta` para devolver únicamente la página solicitada.
  - El filtrado por `actor.getId()` evita mostrar preguntas de otro autor.
  - `actualizarPregunta(...)` comprueba que el actor sea autor y propietario.
  - Solo permite editar cuando el estado original es `BORRADOR` o `RECHAZADA`.
  - Reconstruye un candidato con el mismo id y autor de la pregunta original.
  - Valida nuevamente el contenido antes de modificar la pregunta persistida.
  - Si el estado original es `RECHAZADA`, elimina los revisores, cambia el estado a `BORRADOR` y guarda el contenido corregido.

- `src/main/java/com/bancopreguntas/ui/VentanaPrincipal.java`
  - `abrirEdicion(...)` abre el formulario con la pregunta seleccionada.
  - El formulario reutilizado carga los valores mediante `cargarPregunta(...)` en `PanelCrearPregunta`.

### HU04 — Asignar revisores y resolver revisiones

**Requisito funcional:** el administrador debe asignar uno o más revisores a preguntas pendientes. La pregunta pasa a `En revisión`; cada revisor solo debe ver las preguntas asignadas y puede aprobarlas o rechazarlas una vez.

**Partes del código del administrador:**

- `src/main/java/com/bancopreguntas/ui/PanelAsignarRevisores.java`
  - Muestra las preguntas pendientes en `listaPreguntas`.
  - Muestra los revisores disponibles en `listaRevisores`.
  - Usa `MULTIPLE_INTERVAL_SELECTION` para seleccionar uno o más revisores.
  - `cargar()` obtiene los pendientes y los usuarios con rol revisor.
  - `asignar()` exige una pregunta seleccionada y al menos un revisor.
  - Convierte los usuarios seleccionados en una lista de ids.
  - Llama a `controller.asignarRevisores(...)`.

- `src/main/java/com/bancopreguntas/ui/PreguntaController.java`
  - `listarPendientesDeRevision()` solicita al servicio los pendientes usando el usuario autenticado.
  - `listarRevisoresDisponibles()` verifica que la sesión sea de administrador antes de consultar los usuarios.
  - `asignarRevisores(...)` pasa la operación al servicio.

- `src/main/java/com/bancopreguntas/service/PreguntaServiceImpl.java`
  - `asignarRevisores(...)` exige el rol `ADMINISTRADOR`.
  - Solo acepta preguntas en `PENDIENTE_REVISION`.
  - Rechaza una lista vacía de revisores.
  - Busca cada id en `UsuarioRepository`.
  - Elimina ids repetidos mediante `distinct()`.
  - Verifica que todos los usuarios encontrados tengan rol `REVISOR`.
  - Guarda la asignación en la pregunta y cambia su estado a `EN_REVISION`.
  - Registra la acción `ASIGNAR_REVISORES`.
  - Notifica al sujeto de asignaciones.
  - `listarPendientesDeRevision(Usuario actor)` exige el administrador autenticado.
  - La sobrecarga antigua sin actor lanza `SecurityException` para impedir una consulta sin autenticación.

**Partes del código del revisor:**

- `src/main/java/com/bancopreguntas/ui/PanelMisRevisiones.java`
  - `cargar()` muestra las preguntas que devuelve `controller.listarMisRevisiones()`.
  - `verPregunta()` permite consultar todos los datos de la pregunta asignada.
  - Los botones `Aceptar / aprobar` y `Denegar / rechazar` ejecutan `resolver(true)` o `resolver(false)`.
  - Antes de guardar la decisión se solicita confirmación.
  - Después de resolver, la lista se recarga y la pregunta deja de aparecer como pendiente del revisor.

- `src/main/java/com/bancopreguntas/service/PreguntaServiceImpl.java`
  - `listarAsignadasARevisor(...)` exige rol revisor y consulta por id del usuario.
  - `resolverRevision(...)` exige rol revisor.
  - Comprueba que el id del actor esté en `getRevisoresAsignados()`.
  - Solo permite resolver preguntas que estén en `EN_REVISION`.
  - Define `APROBADA` o `RECHAZADA` según el parámetro `aprobada`.
  - Registra `APROBAR` o `RECHAZAR` en auditoría.
  - El modelo de estados impide una segunda decisión porque una pregunta aprobada o rechazada ya no está en `EN_REVISION`.

### Prototipos de interfaz y test de usabilidad

Los prototipos y el test de usabilidad no están implementados como clases Java dentro de `src`. La evidencia correspondiente se presenta mediante los artefactos gráficos y resultados del entregable. La interfaz implementada que corresponde a esos prototipos está distribuida en:

- `DialogoLogin.java`: pantalla de autenticación.
- `VentanaPrincipal.java`: ventana principal y pestañas por rol.
- `PanelCrearPregunta.java`: formulario de creación y edición.
- `PanelListarPreguntas.java`: listado del autor, filtros, paginación, detalle y acciones.
- `PanelAsignarRevisores.java`: bandeja del administrador.
- `PanelMisRevisiones.java`: bandeja del revisor.
- `PanelGraficoEstados.java`: visualización del estado de las preguntas.

## 2. Atributos de calidad relevantes para la iteración — 1 minuto

### Seguridad

- `src/main/java/com/bancopreguntas/service/AutenticacionServiceImpl.java`
  - `autenticar(...)` busca el usuario por correo y verifica la contraseña contra el hash almacenado.
  - El correo se consulta ignorando diferencias de mayúsculas mediante el repositorio y la restricción `COLLATE NOCASE`.
  - Una contraseña incorrecta devuelve `Optional.empty()`.

- `src/main/java/com/bancopreguntas/service/PasswordHasher.java`
  - Genera una sal aleatoria de 16 bytes.
  - Usa `PBKDF2WithHmacSHA256`.
  - Usa 120.000 iteraciones y una clave derivada de 256 bits.
  - Guarda la sal y el resultado derivado en formato hexadecimal separados por `:`.
  - `MessageDigest.isEqual(...)` compara el valor esperado con el valor calculado.
  - El código no almacena contraseñas en texto plano.

- `src/main/java/com/bancopreguntas/service/PreguntaServiceImpl.java`
  - Las reglas de autorización se ejecutan en la capa de negocio, no únicamente en la interfaz.
  - `exigirRol(...)` comprueba el rol del actor y también verifica que el usuario exista en el repositorio con ese mismo rol.
  - `exigirPropietario(...)` impide que un autor modifique preguntas de otro autor.
  - Las operaciones administrativas y de revisión requieren el actor autenticado.

### Integridad y confiabilidad de datos

- `src/main/java/com/bancopreguntas/repository/SQLiteDatabase.java`
  - Centraliza la creación de conexiones.
  - Activa `PRAGMA foreign_keys = ON`.
  - Configura `busy_timeout = 5000`.
  - Crea el esquema si las tablas no existen.

- `src/main/java/com/bancopreguntas/repository/PreguntaRepositorySQLite.java`
  - Usa `PreparedStatement` para guardar y consultar datos.
  - Guarda la pregunta y sus distractores y revisores en una transacción.
  - Usa claves foráneas y `ON DELETE CASCADE` en las relaciones dependientes.
  - Reconstruye el objeto mediante `Pregunta.Builder`.
  - Conserva id, fechas, estado, distractores y revisores al leer desde SQLite.

- `src/main/java/com/bancopreguntas/repository/RepositorioAuditoriaSQLite.java`
  - Registra actor, pregunta, acción, detalle y fecha.
  - Usa una sentencia preparada `INSERT INTO auditoria`.
  - Las acciones se registran desde la capa de negocio después de cada operación relevante.

### Usabilidad y legibilidad de la interfaz

- `PanelCrearPregunta.java` muestra mensajes de validación concretos en el formulario.
- `PanelListarPreguntas.java` habilita o deshabilita acciones según el estado seleccionado.
- `EstadoBadgeRenderer.java` utiliza colores de alto contraste y calcula si el texto debe ser negro o blanco.
- `PanelGraficoEstados.java` muestra un gráfico circular y una leyenda con el conteo de borradores, preguntas en revisión, aprobadas, rechazadas y eliminadas.
- `PanelMisRevisiones.java` presenta acciones explícitas para aprobar o rechazar y solicita confirmación antes de cambiar el estado.

### Mantenibilidad y extensibilidad

- La interfaz `PreguntaService` separa el contrato de negocio de la implementación.
- `PreguntaRepository` y `UsuarioRepository` permiten cambiar la persistencia sin modificar la lógica del servicio.
- `ValidadorPregunta` y `ValidadorEstructuralPregunta` implementan Strategy para intercambiar la validación.
- `SujetoPreguntas` y `SujetoAsignacion` implementan Observer para notificar cambios sin acoplar directamente las vistas con el servicio.
- `Pregunta.Builder` implementa Builder para construir preguntas complejas y reconstruirlas desde SQLite.
- `PreguntaController` funciona como punto único de comunicación entre Swing y la capa de negocio.

## 3. Arquitectura y diseño de software con C4 y UML — 2 minutos

### Modelo C4 — nivel de contexto

**Sistema:** Banco de Preguntas Saber Pro.

**Actores externos:**

- Autor: crea, consulta, filtra, edita y envía preguntas a revisión.
- Administrador: consulta preguntas pendientes y asigna revisores.
- Revisor: consulta preguntas asignadas y decide aprobar o rechazar.

**Interacción principal:**

- Los actores utilizan una aplicación de escritorio Java Swing.
- La aplicación ejecuta reglas de negocio locales.
- La información se persiste en una base de datos SQLite local.

### Modelo C4 — nivel de contenedores o componentes principales

El sistema está organizado como un monolito dividido en tres capas:

| Capa | Componentes principales | Responsabilidad |
|---|---|---|
| Presentación | `ui`, vistas Swing, `PreguntaController` | Capturar eventos, mostrar datos y controlar la sesión |
| Negocio | `service`, `validacion`, reglas de estados y roles | Validar casos de uso y autorizar operaciones |
| Datos | `repository`, adaptadores SQLite | Guardar, consultar y reconstruir entidades |
| Dominio | `domain` | Representar preguntas, usuarios, roles y estados |
| Notificación | `notificacion` | Notificar cambios y asignaciones mediante Observer |

### Composición de dependencias

- `src/main/java/com/bancopreguntas/App.java`
  - Crea `SQLiteDatabase`.
  - Construye `UsuarioRepositorySQLite`, `PreguntaRepositorySQLite` y `RepositorioAuditoriaSQLite`.
  - Carga usuarios y preguntas iniciales.
  - Crea `ValidadorEstructuralPregunta`.
  - Crea `SujetoAsignacion` y `SujetoPreguntas`.
  - Inyecta todas esas dependencias en `PreguntaServiceImpl`.
  - Crea `AutenticacionServiceImpl`.
  - Crea `PreguntaController`.
  - Abre `DialogoLogin` y, después de autenticar, crea `VentanaPrincipal`.

### Relación UML de clases

**Presentación:**

- `VentanaPrincipal` compone las vistas según `Rol`.
- `PanelCrearPregunta`, `PanelListarPreguntas`, `PanelAsignarRevisores` y `PanelMisRevisiones` dependen de `PreguntaController`.
- `PreguntaController` depende de las interfaces `PreguntaService`, `UsuarioRepository` y `AutenticacionService`.
- Las vistas no dependen directamente de `PreguntaRepositorySQLite`.

**Negocio:**

- `PreguntaServiceImpl` implementa `PreguntaService`.
- `PreguntaServiceImpl` depende de `PreguntaRepository`, `UsuarioRepository`, `ValidadorPregunta`, `SujetoAsignacion`, `SujetoPreguntas` y `RepositorioAuditoria`.
- `ValidadorEstructuralPregunta` implementa `ValidadorPregunta`.
- `AutenticacionServiceImpl` implementa `AutenticacionService`.

**Datos:**

- `PreguntaRepositorySQLite` implementa `PreguntaRepository`.
- `UsuarioRepositorySQLite` implementa `UsuarioRepository`.
- `RepositorioAuditoriaSQLite` implementa `RepositorioAuditoria`.
- Las implementaciones SQLite dependen de `SQLiteDatabase`.
- También existen repositorios en memoria para las pruebas unitarias.

**Dominio:**

- `Pregunta` contiene los datos de la pregunta y sus revisores.
- `Usuario` contiene identidad, correo, rol y hash de contraseña.
- `Rol` define `AUTOR`, `ADMINISTRADOR` y `REVISOR`.
- `EstadoPregunta` define el ciclo de vida y sus transiciones.
- `NivelDificultad` define los niveles disponibles.

### Patrones de diseño visibles en el código

- **MVC:** las clases Swing son vistas, `PreguntaController` es el controlador y las entidades del dominio y el modelo de tabla representan el modelo.
- **Builder:** `Pregunta.builder()` y `Pregunta.Builder` construyen preguntas con muchos atributos.
- **Strategy:** `ValidadorPregunta` permite que `PreguntaServiceImpl` dependa de una abstracción de validación.
- **Observer:** `SujetoPreguntas` notifica a `PanelListarPreguntas`; `SujetoAsignacion` notifica las asignaciones a revisores.
- **Repository:** `PreguntaRepository` y `UsuarioRepository` separan el acceso a datos de las reglas de negocio.
- **Dependency Inversion:** `PreguntaServiceImpl` recibe interfaces de repositorio y validación mediante el constructor.

### Modelo de datos relacional

`SQLiteDatabase.java` crea las siguientes tablas:

- `usuarios`: identidad, nombre, correo, rol y `password_hash`.
- `preguntas`: contenido de la pregunta, estado, autor y fechas.
- `pregunta_distractores`: opciones asociadas con posición.
- `pregunta_revisores`: relación entre preguntas y revisores.
- `auditoria`: registro histórico de actor, pregunta, acción, detalle y fecha.

Las opciones y asignaciones están normalizadas en tablas separadas. Las claves foráneas relacionan preguntas con usuarios y permiten eliminar registros dependientes cuando corresponde.

## 4. Pruebas unitarias automatizadas — 1 minuto

### Configuración de pruebas

- `pom.xml`
  - Define Java 17.
  - Incluye JUnit Jupiter `5.10.2`.
  - Incluye Maven Surefire `3.2.5`.
  - El comando del README es `.\mvnw.cmd test`.

### Pruebas del dominio

- `src/test/java/com/bancopreguntas/domain/PreguntaTest.java`
  - Comprueba que el Builder genere un id.
  - Comprueba que el estado inicial sea `BORRADOR`.
  - Comprueba la creación de fechas.
  - Comprueba que una pregunta pueda tener cuatro distractores.
  - Comprueba que las listas expuestas no puedan modificarse directamente.
  - Comprueba que cambiar el estado actualice la fecha.
  - Comprueba la asignación de revisores.

- `src/test/java/com/bancopreguntas/domain/EstadoPreguntaTest.java`
  - Verifica que cada estado tenga color.
  - Verifica que `BORRADOR` pueda pasar a `PENDIENTE_REVISION`.
  - Verifica que `BORRADOR` no pueda pasar directamente a `APROBADA` o `EN_REVISION`.
  - Verifica las transiciones desde `PENDIENTE_REVISION`.
  - Verifica que `APROBADA` no tenga transiciones.

### Pruebas de validación

- `src/test/java/com/bancopreguntas/validacion/ValidadorEstructuralPreguntaTest.java`
  - `unaPreguntaCompletaYCorrectaEsValida()`.
  - `faltarContextoGeneraError()`.
  - `debeTenerExactamenteCuatroDistractores()`.
  - `losDistractoresNoPuedenRepetirse()`.
  - `laRespuestaCorrectaNoPuedeCoincidirConUnDistractor()`.
  - `faltarNivelDeDificultadGeneraError()`.
  - `acumulaVariosErroresALaVez()`.

### Pruebas de reglas de negocio

- `src/test/java/com/bancopreguntas/service/PreguntaServiceImplTest.java`
  - Creación válida en borrador.
  - Rechazo de preguntas inválidas sin guardarlas.
  - Cambio válido de borrador a pendiente.
  - Rechazo de transiciones inválidas.
  - Paginación con siete preguntas y páginas de cinco elementos.
  - Filtro por tema.
  - Aislamiento de preguntas por autor.
  - Asignación de dos revisores y cambio a `EN_REVISION`.
  - Rechazo de asignación sin revisores.
  - Rechazo de asignación de usuarios que no tienen rol revisor.
  - Restricción de operaciones administrativas al administrador.
  - Restricción de edición al propietario.
  - Edición de preguntas rechazadas y retorno a borrador.
  - Visibilidad exclusiva de preguntas asignadas al revisor.
  - Resolución de revisión por aprobar o rechazar.
  - Impedimento de resolver una revisión desde un revisor no asignado.
  - Conteo de estados para la gráfica del autor.
  - Falla segura de la consulta de pendientes sin actor autenticado.

### Pruebas de repositorio y persistencia

- `src/test/java/com/bancopreguntas/repository/PreguntaRepositoryEnMemoriaTest.java`
  - Comprueba guardar, buscar por id, listar por autor y listar por estado.

- `src/test/java/com/bancopreguntas/repository/SQLitePersistenceTest.java`
  - Usa `@TempDir` para crear una base temporal.
  - Comprueba la persistencia de distractores, revisores y fechas.
  - Comprueba la consulta de preguntas asignadas a un revisor.
  - Comprueba autenticación con hash y rechazo de contraseña incorrecta.
  - Comprueba el registro directo en `auditoria`.
  - Comprueba que el servicio registre `CREAR` y `CAMBIAR_ESTADO`.

## 5. Repositorio Git, commits y tablero del primer sprint Scrum — 1 minuto

### Evidencia del repositorio

La carpeta del proyecto es un repositorio Git con remoto:

`Mezitas/ProyectoIngSoftware2_BancoPreguntas`

La rama observada es `master`.

El historial visible en el repositorio incluye:

- `89eb96b` — `Termincado el Sprint_2, Terminado Requisitos funcionales del codigo, a Revision de los no funcionales`
- `7f0a100` — `Se agrega el grafico BurnDown Char`
- `7f81417` — `Se completa el SCRUM 7`

La pantalla de GitHub debe mostrar el historial completo y los autores de los commits para evidenciar la participación de los integrantes. El código fuente no contiene el tablero Scrum como una clase Java; esa evidencia corresponde al tablero utilizado por el equipo, junto con sus historias, tareas, responsables, estados y gráfico Burndown.

### Archivos que se pueden relacionar con las tareas del sprint

- `README.md`: historias funcionales, roles, arquitectura, patrones, base de datos, cuentas de demostración y comandos de prueba.
- `pom.xml`: configuración de Java, SQLite, JUnit y Maven Surefire.
- `src/main/java/com/bancopreguntas/ui`: tareas de interfaz y flujo MVC.
- `src/main/java/com/bancopreguntas/service`: tareas de autenticación y reglas de negocio.
- `src/main/java/com/bancopreguntas/validacion`: tarea de validación estructural.
- `src/main/java/com/bancopreguntas/repository`: tarea de persistencia SQLite y auditoría.
- `src/test/java`: tareas de pruebas unitarias, reglas por rol y persistencia.

## 6. Software funcional y aspectos claves de codificación — 5 minutos

### Flujo de ejecución y composición de la aplicación

1. Ejecutar `.\mvnw.cmd exec:java`.
2. `App.main(...)` crea la base `data/banco-preguntas.db`.
3. `SQLiteDatabase` crea las tablas si todavía no existen.
4. `App.cargarUsuariosIniciales(...)` crea las cuentas de demostración con `PasswordHasher.hash(...)`.
5. `App.cargarPreguntasIniciales(...)` crea dos preguntas de ejemplo si la tabla está vacía.
6. `App` inyecta repositorios, validadores, sujetos de notificación y auditoría en `PreguntaServiceImpl`.
7. Se abre `DialogoLogin`.
8. Después de autenticar, `VentanaPrincipal` construye las pestañas según el rol.

### Demostración con el rol Autor

- Ingresar con `ana@bancopreguntas.local` y la contraseña de demostración definida en `README.md`.
- `DialogoLogin.ingresar()` llama a `controller.autenticar(...)`.
- `PreguntaController.autenticar(...)` delega en `AutenticacionService`.
- `AutenticacionServiceImpl` consulta el usuario y verifica el hash PBKDF2.
- `VentanaPrincipal.reconstruirTabs(...)` detecta `Rol.AUTOR`.
- Se crean las pestañas `Crear pregunta` y `Mis preguntas`.

**Crear una pregunta:**

- Abrir `PanelCrearPregunta`.
- Diligenciar el formulario.
- Al pulsar guardar, `guardar()` construye `Pregunta`.
- `PreguntaController.crearPregunta(...)` pasa la pregunta al servicio.
- `PreguntaServiceImpl.crearPregunta(...)` comprueba rol, autor, estado, id y validación.
- `ValidadorEstructuralPregunta.validar(...)` devuelve el resultado.
- `PreguntaRepositorySQLite.guardar(...)` ejecuta la transacción de persistencia.
- `RepositorioAuditoriaSQLite.registrar(...)` registra `CREAR`.
- `SujetoPreguntas.notificarActualizacion()` actualiza el listado.

**Mostrar una validación:**

- Dejar vacío un campo obligatorio o repetir un distractor.
- La vista recibe `ValidacionException`.
- `PanelCrearPregunta` presenta los errores acumulados en `lblMensaje`.
- La pregunta no se guarda porque el repositorio se invoca únicamente después de validar.

**Consultar y filtrar:**

- Abrir `Mis preguntas`.
- `PanelListarPreguntas.cargar()` construye los filtros.
- `PreguntaController.listarMisPreguntas(...)` llama al servicio con el usuario actual.
- `listarPreguntasPorAutor(...)` filtra por `actor.getId()`, aplica los criterios y pagina.
- La tabla muestra el estado con `EstadoBadgeRenderer`.
- `PanelGraficoEstados.actualizar(...)` recibe los conteos producidos por `contarEstadosVisiblesPorAutor(...)`.

**Enviar a revisión:**

- Seleccionar una pregunta en estado `Borrador`.
- Pulsar `Enviar a revisión`.
- `PreguntaController.enviarARevision(...)` solicita el estado `PENDIENTE_REVISION`.
- `EstadoPregunta` valida la transición.
- El repositorio actualiza el estado.
- La auditoría registra `CAMBIAR_ESTADO`.
- La tabla cambia la etiqueta y el color.

**Editar una pregunta rechazada:**

- Seleccionar una pregunta en estado `Rechazada`.
- Pulsar `Editar pregunta`.
- `VentanaPrincipal.abrirEdicion(...)` abre `PanelCrearPregunta` con la pregunta.
- `cargarPregunta(...)` rellena los campos.
- `PreguntaServiceImpl.actualizarPregunta(...)` valida propietario y estado.
- Al guardar, el contenido se actualiza, se eliminan los revisores y el estado vuelve a `BORRADOR`.

### Demostración con el rol Administrador

- Cerrar sesión mediante el botón de `VentanaPrincipal`.
- Ingresar con `admin@bancopreguntas.local` y la contraseña de demostración definida en `README.md`.
- `reconstruirTabs(...)` crea únicamente la pestaña `Asignar revisores`.
- `PanelAsignarRevisores.cargar()` obtiene las preguntas en `PENDIENTE_REVISION`.
- También obtiene los usuarios mediante `listarRevisoresDisponibles()`.
- Seleccionar una pregunta y uno o más revisores.
- Pulsar `Asignar revisor(es)`.
- `PreguntaServiceImpl.asignarRevisores(...)` verifica:
  - actor administrador;
  - pregunta pendiente;
  - lista no vacía;
  - revisores existentes;
  - rol revisor de cada usuario.
- Se guardan las relaciones en `pregunta_revisores`.
- La pregunta pasa a `EN_REVISION`.
- Se registra `ASIGNAR_REVISORES`.
- `SujetoAsignacion.notificarAsignacion(...)` notifica la asignación.

### Demostración con el rol Revisor

- Cerrar sesión e ingresar con `juan@bancopreguntas.local` o `pablo@bancopreguntas.local`.
- `VentanaPrincipal` crea la pestaña `Mis revisiones`.
- `PanelMisRevisiones.cargar()` llama a `listarMisRevisiones()`.
- `PreguntaRepositorySQLite.listarAsignadasARevisor(...)` hace un `JOIN` entre `preguntas` y `pregunta_revisores`.
- La consulta filtra por el id del revisor autenticado y por estado `EN_REVISION`.
- Seleccionar la pregunta y pulsar `Ver pregunta` para mostrar todos sus campos.
- Pulsar `Aceptar / aprobar`:
  - se confirma la operación;
  - `resolverRevision(..., true)` cambia a `APROBADA`;
  - se registra `APROBAR`;
  - la pregunta deja de aparecer en la bandeja.
- Pulsar `Denegar / rechazar`:
  - se confirma la operación;
  - `resolverRevision(..., false)` cambia a `RECHAZADA`;
  - se registra `RECHAZAR`;
  - la pregunta deja de aparecer como revisión activa.
- Si otro revisor intenta resolver una pregunta que no tiene asignada, `PreguntaServiceImpl` lanza `SecurityException`.

### Evidencia de auditoría y persistencia

- La base local está en `data/banco-preguntas.db`.
- `SQLiteDatabase` crea las tablas `usuarios`, `preguntas`, `pregunta_distractores`, `pregunta_revisores` y `auditoria`.
- `PreguntaRepositorySQLite` usa sentencias preparadas y transacciones.
- Cada acción importante llega a `registrar(...)` desde `PreguntaServiceImpl`.
- Las acciones almacenadas son `CREAR`, `EDITAR`, `CAMBIAR_ESTADO`, `ASIGNAR_REVISORES`, `APROBAR` y `RECHAZAR`.
- Al cerrar y volver a ejecutar la aplicación, SQLite reconstruye las preguntas, estados, distractores, revisores y fechas mediante `Pregunta.Builder`.

### Relación visible entre interfaz, negocio y datos

- La interfaz captura la acción en una vista Swing.
- `PreguntaController` obtiene el usuario autenticado y llama al contrato de servicio.
- `PreguntaServiceImpl` ejecuta validaciones, autorización y transición de estado.
- Las interfaces de repositorio abstraen el almacenamiento.
- `PreguntaRepositorySQLite`, `UsuarioRepositorySQLite` y `RepositorioAuditoriaSQLite` ejecutan las sentencias JDBC.
- `SQLiteDatabase` centraliza la conexión y el esquema.
- Los observadores actualizan las vistas cuando el estado o las asignaciones cambian.
