# Banco de Preguntas Saber Pro — Primer corte

Aplicación de escritorio Java 17 con Swing, monolito en tres capas (presentación, negocio y datos) y micro-patrón MVC. Incluye autenticación local con roles, preguntas Saber Pro, revisión y persistencia SQLite.

## Ejecutar y probar

Se incluye Maven Wrapper para Windows y sistemas Unix. Se necesita Java 17 o posterior; no hace falta instalar Maven:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd exec:java
```

En Unix/macOS: `sh ./mvnw clean test` y `sh ./mvnw exec:java`.

En Java 24 o posterior, el Maven Wrapper habilita el acceso nativo requerido por SQLite y evita la advertencia de carga JNI. Si se ejecuta `App` directamente desde un IDE, añadir `--enable-native-access=ALL-UNNAMED` a las opciones de la máquina virtual. La dependencia `slf4j-nop` configura el backend silencioso que usa SQLite y evita el aviso de que falta un proveedor SLF4J.

La base `data/banco-preguntas.db` y sus tablas se crean al iniciar la aplicación. Se excluye del control de versiones. Los usuarios de demostración y dos preguntas de ejemplo se inicializan si corresponde; la información creada por usuarios permanece en SQLite al cerrar la aplicación.

## Cuentas de demostración

Las cuentas se almacenan con contraseñas derivadas mediante PBKDF2, no en texto plano. Estas credenciales son únicamente para desarrollo y sustentación; deben cambiarse antes de cualquier uso real.

| Rol | Correo | Contraseña |
|---|---|---|
| Autor | `ana@bancopreguntas.local` | `Autor123!` |
| Administrador | `admin@bancopreguntas.local` | `Admin123!` |
| Revisor | `juan@bancopreguntas.local` | `Revisor123!` |
| Revisor | `pablo@bancopreguntas.local` | `Revisor123!` |

El login permite cerrar sesión y entrar con otra cuenta. Las pestañas y las operaciones disponibles dependen del rol autenticado.

## Historias funcionales

| HU | Funcionalidad | Implementación |
|---|---|---|
| HU01 | Crear preguntas de selección múltiple con contexto, pregunta, cuatro distractores, respuesta, justificación, bibliografía, competencia, tema, subtema y dificultad. Validación estructural al guardar. | `PanelCrearPregunta`, `PreguntaServiceImpl`, `ValidadorEstructuralPregunta` |
| HU02 | Cambiar preguntas propias de borrador a pendientes de revisión y visualizar estados con colores. | `PanelListarPreguntas`, `PreguntaServiceImpl`, `EstadoBadgeRenderer` |
| HU03 | Consultar preguntas propias con filtros y paginación, ver todos sus campos y editar borradores o preguntas rechazadas. Al corregir una rechazada, vuelve a borrador. | `PanelListarPreguntas`, `PanelCrearPregunta`, `FiltroPregunta`, `ResultadoPaginado` |
| HU04 | El administrador asigna uno o más revisores a preguntas pendientes. La asignación cambia el estado a “En revisión”; cada revisor ve en su pestaña las preguntas asignadas y puede aprobarlas o rechazarlas. | `PanelAsignarRevisores`, `PanelMisRevisiones`, `PreguntaServiceImpl` |

Los cambios de creación, edición, estado, asignación y decisión de revisión se registran en la tabla SQLite `auditoria` con actor, pregunta, acción, detalle y fecha.
No se envía correo: la asignación se comunica al revisor mediante su pestaña de preguntas asignadas.
La gráfica del autor distingue borradores, preguntas en revisión, aprobadas, rechazadas y eliminadas; cada categoría tiene un color de alto contraste y su conteo se muestra en la leyenda.
El servicio de conteo ofrece sobrecargas para recibir el usuario autenticado o el id del autor, manteniendo compatibilidad con ambos tipos de cliente.

## Reglas por rol

- **Autor:** crea preguntas en su propio nombre; lista y edita únicamente las propias. Puede editar solo en estado Borrador o Rechazada. Al editar una pregunta rechazada, vuelve a Borrador. Solo puede enviar borradores a revisión y eliminar borradores o preguntas rechazadas.
- **Administrador:** consulta pendientes y asigna revisores existentes. El negocio verifica el rol de administrador y que cada usuario asignado tenga rol Revisor. La asignación mueve la pregunta a En revisión.
- **Revisor:** consulta únicamente preguntas asignadas a su usuario y puede tomar una decisión una vez: Aprobar o Rechazar.

Las reglas se validan en la capa de negocio y no dependen solamente de ocultar pestañas en Swing. En este corte la autenticación valida cuentas locales con credenciales de demostración; no reemplaza un sistema de identidad para producción.
Los métodos de servicio que consultan pendientes requieren recibir el usuario administrador autenticado; la sobrecarga antigua sin actor se conserva únicamente por compatibilidad y falla de forma segura.

## Arquitectura y diseño

```text
com.bancopreguntas
├── ui/             Presentación Swing: vistas y PreguntaController (MVC)
├── service/        Reglas de negocio, autenticación y hash de contraseñas
├── domain/         Pregunta, Usuario, roles y estados
├── validacion/     Validación estructural intercambiable (Strategy)
├── repository/     Interfaces y adaptadores SQLite
└── notificacion/   Observers para cambios/asignaciones
```

La composición de dependencias se realiza en `App`. `PreguntaRepositorySQLite`, `UsuarioRepositorySQLite` y `RepositorioAuditoriaSQLite` encapsulan el acceso a SQLite mediante JDBC y sentencias preparadas. La capa de negocio depende de interfaces de repositorio (DIP), y la factoría de conexiones/esquema `SQLiteDatabase` centraliza la inicialización de la base. Se mantienen Builder, Strategy y Observer; `Pregunta.Builder` admite reconstruir preguntas con sus fechas e ids para preservar los datos leídos de SQLite.

La base implementa las tablas `usuarios`, `preguntas`, `pregunta_distractores`, `pregunta_revisores` y `auditoria`. Las opciones y asignaciones están normalizadas y enlazadas con claves foráneas.

## Pruebas

`.\mvnw.cmd test` ejecuta pruebas unitarias de dominio, validación, reglas por rol, edición, flujo de revisión, autenticación y persistencia SQLite en una base temporal.
