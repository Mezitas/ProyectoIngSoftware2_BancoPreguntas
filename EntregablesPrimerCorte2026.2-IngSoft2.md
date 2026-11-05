# Ingeniería de Software II

## Teoría y Laboratorio

## Entregables Proyecto de Curso - Primer Corte 2026.2

**Fecha creación:** 8 Ago 2026  
**Última actualización:** 8 Ago 2026 7:38 PM

## Entregables

En grupos de 3 a 5 estudiantes, hacer una implementación del proyecto de Banco de Preguntas. La aplicación debe estar implementada en Java Desktop con una arquitectura monolítica en 3 capas. Además, usar el micro-patrón MVC. Utilizar principios de diseño SOLID y algunos patrones de diseño GoF para garantizar la modificabilidad de la aplicación. Se debe implementar al menos un conjunto de historias de usuario de alto valor para el cliente.

A continuación, los requisitos funcionales que se deben entregar para el primer corte.

## Requisitos funcionales

Se deben implementar los siguientes requisitos funcionales de alto valor para el cliente:

1. Yo como autor de preguntas necesito crear preguntas de selección múltiple con única respuesta acorde a los principios del Diseño Centrado en Evidencia para alimentar el banco de preguntas Saber Pro.  
    **Contexto:** Cada pregunta debe tener: Contexto, Pregunta directa, Cuatro distractores, Respuesta correcta, Justificación de la respuesta, Bibliografía, Competencia, Tema, Subtema, Nivel de dificultad. Al momento de grabar el sistema debe aplicar la validación estructural (ver HU03).
    
2. Yo como autor de preguntas necesito cambiar el estado de mis preguntas de “borrador” a “Pendiente de revisión” para que el administrador les asigne al menos un revisor.  
    **Contexto:** los estados se deben visualizar con colores.
    
3. Yo como autor de preguntas necesito listar las preguntas que he creado para poder más adelante verlas y editarlas.  
    **Contexto:** aplicar paginación y filtros para hacer funcional y usable la búsqueda y listado de preguntas.
    
4. Yo como administrador necesito asignar al menos un revisor de las preguntas en estado “Pendiente de revisión” para que las preguntas sean revisadas por otros docentes.  
    **Contexto:** Una vez asignados los revisores, el sistema enviará un email para ser notificados.
    

> Universidad del Cauca  
> Facultad de Ingeniería Electrónica y Telecomunicaciones  
> Programa de Ingeniería de Sistemas  
> Ingeniería de Software II Grupos A y B  
> Laboratorio de Ingeniería de Software II Grupos A, B y C  
> Paola Andrea Bedoya, Pablo Magé, W. Libardo Pantoja, Julio Hurtado  
> 2026.2

## Rúbrica de evaluación

|Criterio|Excelente (5 pts)|Bueno (4 pts)|Aceptable (3 pts)|Insuficiente (0, 1 pts)|Peso (%)|
|---|---|---|---|---|---|
|Cumplimiento de los requisitos funcionales|Implementa completamente los requisitos funcionales del primer corte.|Los requisitos se implementan con pequeñas limitaciones o errores.|Los requisitos se implementan parcialmente o con errores funcionales importantes.|Los requisitos no se implementan o no cumplen las necesidades del cliente.|30%|
|Cumplimiento de los requisitos NO funcionales de modificabilidad|Se aplican correctamente los principios de diseño SOLID y patrones de diseño.|Se aplican los principios SOLID y patrones de diseño con pequeñas limitaciones o errores.|Se aplican los principios SOLID y patrones de diseño con limitaciones o errores importantes.|No se aplican los principios SOLID y patrones de diseño.|30%|
|Documentación de arquitectura|Documento claro, estructurado, con diagramas y detalles técnicos relevantes. Repositorio Git bien organizado y documentado.|Documento y repositorio adecuados, pero con pequeños aspectos mejorables.|Documento y repositorio, con aspectos importantes a mejorar.|No hay documento de la arquitectura y repositorio Git.|20%|
|Pruebas unitarias automatizadas|Hay pruebas unitarias a todas las clases del dominio (entidades y servicios) correctamente implementadas.|Hay pruebas unitarias con pequeñas limitaciones o errores.|Hay pruebas unitarias con limitaciones o errores importantes.|No hay pruebas unitarias.|20%|

## PROTOCOLO DE SUSTENTACIÓN

Para la sustentación del primer entregable del proyecto de curso, cada equipo debe elaborar un video en YouTube. Es importante que todos los integrantes del equipo participen en la sustentación. A continuación, el protocolo de sustentación:

1. Mostrar las historias de usuario con sus criterios de aceptación, prototipos de la interfaz gráfica de usuario y test de usabilidad: **2 minutos**.
    
2. Mostrar los atributos de calidad relevantes para la iteración: **1 minuto**.
    
3. Mostrar la arquitectura y diseño de software (usar el modelo C4 y UML): **2 minutos**.
    
4. Mostrar las pruebas unitarias automatizadas: **1 minuto**.
    
5. Mostrar el repositorio Git con los commits de todos los integrantes del equipo y el tablero de tareas del primer sprint Scrum: **1 minuto**.
    
6. Mostrar el software funcional y algunos aspectos claves de la codificación que evidencien la implementación de la arquitectura: **5 minutos**.
    

**NOTA:** Los grupos que se pasen de estos tiempos (o aceleren el video) serán penalizados con **-1 punto**.

## DOCUMENTO DE ARQUITECTURA

El documento de arquitectura debe tener estas partes:

- Portada.
    
- Introducción breve al documento.
    
- Historias de usuario (con criterios de aceptación) implementadas en la primera iteración.
    
- Prototipos de la interfaz de usuario y un test de evaluación de usabilidad.
    
- Pantallazo de planificación de tareas del Sprint 1: Jira, Trello.
    
- Escenario de calidad modificabilidad: contexto, estímulo, respuesta, medición de calidad, resultado esperado.
    
- Arquitectura y diseño de software usando el modelo C4.
    
- Listado de patrones de diseño implementados y una breve descripción de cómo y en qué contexto del problema se aplicaron.
    
- URL del video de YouTube.
    
- URL del repositorio Git.