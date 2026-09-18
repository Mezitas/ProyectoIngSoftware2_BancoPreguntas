# Banco de Preguntas Saber Pro — Primer Corte (Ingeniería de Software II)
# Joseph David Trujillo Gómez - Juan Pablo Hernandez Bravo - Juan David Meza Paz

Aplicación de escritorio en **Java (Swing)**, con arquitectura **monolítica en 3 capas**
(Presentación, Negocio, Datos) y micro-patrón **MVC**, que implementa las 4 historias de
usuario de alto valor solicitadas para el primer corte.

## Descripción


Al iniciar, la aplicación carga usuarios y preguntas de ejemplo. Arriba a la izquierda hay
un selector de "usuario en sesión" para alternar entre el rol **Autor** (Ana Torres) y el
rol **Administrador** (David Gómez) sin necesidad de un módulo de login, que no forma
parte del alcance funcional de este corte.

## Historias de usuario implementadas

| HU | Descripción | Dónde se implementa |
|----|-------------|----------------------|
| HU01 | Crear preguntas de selección múltiple (Diseño Centrado en Evidencia) con validación estructural al grabar | `PanelCrearPregunta`, `PreguntaServiceImpl#crearPregunta`, `ValidadorEstructuralPregunta` |
| HU02 | Cambiar estado de "Borrador" a "Pendiente de revisión", visualizado con colores | `PanelListarPreguntas`, `PreguntaServiceImpl#cambiarEstado`, `EstadoPregunta` (color por estado), `EstadoBadgeRenderer` |
| HU03 | Listar preguntas propias con paginación, filtros, eliminación lógica y gráfico de estados | `PanelListarPreguntas`, `PanelGraficoEstados`, `PreguntaServiceImpl#listarPreguntasPorAutor` |
| HU04 | Administrador asigna revisor(es) a preguntas "Pendiente de revisión" y el sistema notifica por correo | `PanelAsignarRevisores`, `PreguntaServiceImpl#asignarRevisores`, patrón Observer (`SujetoAsignacion`, `ObservadorAsignacion`, `NotificadorEmailObservador`) |

## Arquitectura en 3 capas

```
com.bancopreguntas
├── domain/        → Entidades del negocio (capa de Negocio / Modelo)
├── validacion/     → Estrategias de validación (Strategy)
├── notificacion/   → Mecanismo de notificación (Observer)
├── repository/     → Capa de Datos (interfaces + implementación en memoria)
├── service/        → Capa de Negocio (reglas, orquestación)
└── ui/             → Capa de Presentación (Vistas Swing + Controlador → MVC)
```

* **Presentación**: paquete `ui`. `VentanaPrincipal` compone las vistas (`PanelCrearPregunta`,
  `PanelListarPreguntas`, `PanelAsignarRevisores`), que son la "Vista" del MVC. Toda vista
  habla únicamente con `PreguntaController` (el "Controlador"), nunca con la capa de datos
  directamente.
* **Negocio**: paquete `service`. `PreguntaServiceImpl` contiene las reglas de negocio
  (validación estructural, transiciones de estado válidas, paginación/filtrado, asignación
  de revisores) y es el "Modelo" del MVC desde el punto de vista de la vista.
* **Datos**: paquete `repository`. Interfaces (`PreguntaRepository`, `UsuarioRepository`)
  con una implementación en memoria (suficiente para la demo funcional del primer corte),
  desacoplada del resto de la aplicación gracias al principio de Inversión de Dependencias.

## Principios SOLID aplicados

* **S**RP: cada clase tiene una única responsabilidad (`ValidadorEstructuralPregunta` solo
  valida, `PreguntaRepositoryEnMemoria` solo persiste, `PreguntaController` solo media entre
  UI y negocio).
* **O**CP: se pueden agregar nuevas estrategias de validación (`ValidadorPregunta`) o nuevos
  observadores de notificación (`ObservadorAsignacion`) sin modificar el código existente.
* **L**SP: cualquier implementación de `PreguntaRepository`, `ValidadorPregunta` o
  `EmailService` puede sustituir a otra sin romper el comportamiento esperado por quien la usa.
* **I**SP: interfaces pequeñas y específicas (`PreguntaRepository`, `UsuarioRepository`,
  `ValidadorPregunta`, `EmailService`, `ObservadorAsignacion`) en lugar de una interfaz
  monolítica.
* **D**IP: `PreguntaServiceImpl` depende de abstracciones (`PreguntaRepository`,
  `UsuarioRepository`, `ValidadorPregunta`) inyectadas por constructor, no de clases concretas.

## Patrones de diseño GoF implementados

1. **Builder** (`Pregunta.Builder`): construye el objeto `Pregunta`, que tiene muchos campos
   obligatorios, paso a paso y de forma legible, evitando un constructor telescópico.
2. **Strategy** (`ValidadorPregunta` / `ValidadorEstructuralPregunta`): encapsula el algoritmo
   de validación estructural como una estrategia intercambiable, aplicada al grabar (HU01).
3. **Observer** (`SujetoAsignacion`, `ObservadorAsignacion`, `NotificadorEmailObservador`,
   `SujetoPreguntas`, `ObservadorPreguntas`): además de notificar la asignación de revisores,
   actualiza el gráfico de "Mis preguntas" cuando se crea, envía a revisión o elimina una pregunta.
4. **Singleton** (`PreguntaRepositoryEnMemoria#getInstance`, `UsuarioRepositoryEnMemoria#getInstance`):
   garantiza una única fuente de datos en memoria durante la ejecución de la aplicación de
   escritorio (la UI los consume vía `getInstance()`; las pruebas unitarias usan el
   constructor público para tener instancias aisladas).


## Pruebas unitarias

Se incluyen pruebas JUnit 5 para las clases de dominio y de negocio (`mvn test`):

* `domain/PreguntaTest`, `domain/EstadoPreguntaTest`
* `validacion/ValidadorEstructuralPreguntaTest`
* `repository/PreguntaRepositoryEnMemoriaTest`
* `service/PreguntaServiceImplTest` (cubre las 4 historias de usuario, incluida la
  notificación vía Observer)

## Persistencia

Este entregable usa repositorios **en memoria** (los datos se reinician al cerrar la
aplicación), lo cual es adecuado para la demostración funcional del primer corte descrita
por el profesor. La capa de datos está aislada detrás de interfaces (`PreguntaRepository`,
`UsuarioRepository`), de modo que en un corte posterior se puede sustituir por una
implementación con base de datos (JDBC/JPA) sin modificar la capa de negocio ni la de
presentación.
