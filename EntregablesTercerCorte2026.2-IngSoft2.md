# **Ingeniería de Software II** 

# **Teoría y Laboratorio**

# **Entregables Proyecto de Curso \- Tercer Corte 2026.2**

*Fecha creación: 1 Oct 2026*  
*Última actualización 12 Nov 2026 5:50 PM*

## **Entregables**

Para el tercer corte, se trabajarán requisitos funcionales y no funcionales del Sistema de Gestión de Preguntas. Los requisitos de los cortes anteriores se pueden mejorar. Los nuevos requisitos aparecen en color azul.

## **Requisitos funcionales**

Se deben implementar los siguientes requisitos funcionales de alto valor para el cliente:

1. **Yo como** autor de preguntas **necesito** crear preguntas de selección múltiple con única respuesta acorde a los principios del Diseño Centrado en Evidencia **para** alimentar el banco de preguntas Saber Pro. *Contexto*: Cada pregunta debe tener: Contexto, Pregunta directa, Cuatro distractores, Respuesta correcta, Justificación de la respuesta, Bibliografía, Competencia, Tema, Subtema, Nivel de dificultad. Al momento de grabar el sistema debe aplicar la validación estructural (ver HU03).  
2. **Yo como** autor de preguntas **necesito** cambiar el estado de mis preguntas de “borrador” a “Pendiente de revisión” para que el administrador les asigne al menos un revisor. *Contexto*: los estados se deben visualizar con colores.   
3. **Yo como** autor de preguntas **necesito** listar las preguntas que he creado **para** poder más adelante verlas y editarlas. *Contexto*: aplicar paginación y filtros para hacer funcional y usable la búsqueda y listado de preguntas.  
4. **Yo como** administrador **necesito** asignar revisores (entre 1 y 3\) de las preguntas en estado “Pendiente de revisión” **para** que las preguntas sean revisadas por otros docentes. *Contexto*: Una vez asignados los revisores, el sistema enviará un email para ser notificados.  
5. **Yo como** revisor de preguntas **necesito** listar las preguntas que me han sido asignadas por el administrador **para** poder leerlas, dejar la evaluación y cambiar el estado a Aprobada/Rechazada. *Contexto*: El revisor puede dejar observaciones para que sean leídas por el autor. El sistema debe enviar notificaciones a los implicados.   
6. **Yo como** autor de preguntas **necesito** listar y ver el historial de revisiones y sus observaciones **para** poder sacar una versión mejorada de la pregunta y cambiar de estado. *Contexto*: El sistema debe enviar notificaciones a los implicados. 

## **Requisitos no funcionales**

1. Implementar un sistema de autenticación y autorización basado en tokens JWT. Usar un sistema externo como [**Keycloak**](https://www.keycloak.org/). La gestión de usuarios y roles se puede hacer por Postman, no se requiere aún una interfaz gráfica en el frontend.  
2. Implementar la **arquitectura limpia** (ya sea DDD, hexagonal, o las dos) a un microservicio que lo amerite (que tenga reglas en su lógica de dominio).  
3. **Dockerizar** la aplicación de los microservicios mediante Docker-Compose.   
4. Documento final en PDF con la documentación de la arquitectura del software (el documento que se ha venido trabajando en cada corte). Se sugiere que el **repositorio Github** del proyecto tenga una ligera documentación del proyecto, sus requisitos, tecnología y algo de la arquitectura de tal forma que el repositorio sirva de carta de presentación cuando los estudiantes necesiten evidenciar experiencia laboral ante un sector de la industria.

## **Rúbrica de evaluación** 

| Criterio | Excelente (5 pts) | Bueno (4 pts) | Aceptable (3 pts) | Insuficiente (0, 1 pts) | Peso (%) |
| :---- | :---- | :---- | :---- | :---- | :---- |
| Cumplimiento de los requisitos funcionales | Implementa completamente los requisitos funcionales. | Los requisitos se implementan con pequeñas limitaciones o errores. | Los requisitos se implementan parcialmente o con errores funcionales importantes. | Los requisitos no se implementan o no cumplen las necesidades del cliente. | 20% |
| Cumplimiento de los requisitos NO funcionales de **modificabilidad** (clean architecture)**, escalabilidad (**microservicios**)** y **seguridad** (keycloak, jwt) | Se aplican correctamente los principios de diseño SOLID y patrones de diseño y patrón de microservicios cumpliendo todos los atributos de calidad. | Se aplican los principios SOLID y patrones de diseño con pequeñas limitaciones o errores | Se aplican los principios SOLID y patrones de diseño con limitaciones o errores importantes | No se aplican los principios SOLID y patrones de diseño. | 10% |
| Documentación de arquitectura | Documento claro, estructurado, con diagramas y detalles técnicos relevantes. Repositorio Git bien organizado y documentado | Documento y repositorio adecuados, pero con  pequeños aspectos mejorables. | Documento y repositorio, con  aspectos importantes a mejorar. | No hay documento de la arquitectura y repositorio Git. | 10% |
| Pruebas unitarias automatizadas | Hay pruebas unitarias  a todas las clases del dominio (entidades y servicios) correctamente implementadas  | Hay pruebas unitarias con pequeñas limitaciones o errores | Hay pruebas unitarias con limitaciones o errores importantes | No hay pruebas unitarias | 10% |
| Sustentación y calidad de respuestas a las preguntas | Responden ambas preguntas con total solvencia técnica, claridad y precisión. Demuestran dominio absoluto del código y la arquitectura, logrando convencer al cliente. | Responden ambas preguntas correctamente, aunque con ligeras dudas o imprecisiones menores en la explicación técnica o de negocio. | Responden parcialmente una o ambas preguntas. Muestran vacíos técnicos importantes o requieren excesiva ayuda de otros compañeros para contestar. | No logran responder las preguntas, dan respuestas incorrectas o demuestran desconocimiento del código y del funcionamiento del sistema. | 50% |

**PROTOCOLO DE SUSTENTACIÓN**  
Para la sustentación del tercer entregable del proyecto de curso, se hará de manera presencial. Importante que **todos los integrantes** del equipo participen en la sustentación. Además, se debe **asumir el rol de empresarios** tratando de **vender** el producto a los clientes (los docentes). A continuación, el protocolo de sustentación:

1. Mostrar el software funcional y convencer a los clientes que cumple con los requisitos de negocio de Piedrazul. Mencionar también cómo la aplicación contempla requisitos de **seguridad**, **escalabilidad** y **modificabilidad**: **10 minutos.**  
2. Resolver dos preguntas técnicas de los clientes (docentes). Los docentes eligen quienes del grupo responden cada pregunta. **5 minutos**.

**DOCUMENTO DE ARQUITECTURA**  
El documento de arquitectura debe tener estas partes:

* Portada  
* Introducción breve al documento  
* Historias de usuario (con criterios de aceptación) de las tres iteraciones  
* Pantallazo de planificación de tareas de los Sprint 1, 2 y 3: Jira, Trello.  
* Escenarios de calidad de **modificabilidad** y **escalabilidad**: contexto, estímulo, respuesta, medición de calidad, resultado esperado.  
* Arquitectura y diseño de software de la segunda y tercera iteración, usando el modelo C4. Evidenciar los aspectos de seguridad (Keycloak, JWT).  
* Evidencia que la aplicación fue dockerizada con Docker Compose y utiliza Volúmenes para las bases de datos.  
* Listado de patrones de diseño implementados y una breve descripción de cómo y en qué contexto del problema se aplicaron.  
* URL del repositorio GIT