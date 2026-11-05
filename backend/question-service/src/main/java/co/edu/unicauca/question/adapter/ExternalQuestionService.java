package co.edu.unicauca.question.adapter;

/**
 * Patrón Adapter - clase a adaptar (Adaptee). Simula una plataforma externa de
 * reactivos con formato propio (JSON con nombres de campo ajenos y cadenas crudas
 * delimitadas por '#'). NO se modifica: la adapta {@link ExternalQuestionAdapter}.
 */
public class ExternalQuestionService {
    /** Arreglo JSON con reactivos (el último es inválido a propósito: solo 3 opciones). */
    public String fetchQuestionsJson() {
        return "[" +
                "  {\"codigo\": \"EXT-101\"," +
                "   \"texto_contexto\": \"Una empresa quiere que su sistema de ventas permita agregar nuevos medios de pago sin modificar el código existente.\"," +
                "   \"enunciado\": \"¿Qué principio SOLID se cumple al poder extender el sistema sin modificarlo?\"," +
                "   \"respuestas\": [\"Abierto/Cerrado\", \"Responsabilidad única\", \"Segregación de interfaces\", \"Sustitución de Liskov\"]," +
                "   \"indice_correcta\": 0," +
                "   \"retroalimentacion\": \"El principio Abierto/Cerrado indica que el software debe estar abierto a la extensión y cerrado a la modificación.\"," +
                "   \"competencia\": \"Diseño de software\", \"tema\": \"Principios SOLID\", \"subtema\": \"Abierto/Cerrado\"}," +
                "  {\"codigo\": \"EXT-102\"," +
                "   \"texto_contexto\": \"Un analista revisa el crecimiento de usuarios de una aplicación.\"," +
                "   \"enunciado\": \"¿Qué medida de tendencia central es menos sensible a valores atípicos?\"," +
                "   \"respuestas\": [\"Media\", \"Mediana\", \"Rango\", \"Varianza\"]," +
                "   \"indice_correcta\": 1," +
                "   \"retroalimentacion\": \"La mediana depende de la posición central de los datos, no de sus valores extremos.\"," +
                "   \"competencia\": \"Razonamiento cuantitativo\", \"tema\": \"Estadística\", \"subtema\": \"Medidas de tendencia central\"}," +
                "  {\"codigo\": \"EXT-103\"," +
                "   \"texto_contexto\": \"Reactivo incompleto exportado por error.\"," +
                "   \"enunciado\": \"¿Cuál es la capital de Colombia?\"," +
                "   \"respuestas\": [\"Bogotá\", \"Medellín\", \"Cali\"]," +
                "   \"indice_correcta\": 0," +
                "   \"retroalimentacion\": \"Bogotá es la capital.\"," +
                "   \"competencia\": \"Lectura crítica\", \"tema\": \"General\", \"subtema\": \"General\"}" +
                "]";
    }

    public int rawQuestionCount() { return 2; }

    public String fetchRawQuestion(int n) {
        String[] raw = {
                "EXT-201#Un estudiante lee un artículo sobre cambio climático con datos de 20 años."
                        + "#¿Cuál es el propósito principal de presentar series históricas de datos?"
                        + "#Entretener al lector;Respaldar con evidencia la tesis del texto;Reducir la extensión del artículo;"
                        + "Evitar citar fuentes#B"
                        + "#Las series históricas se usan como evidencia para sustentar una afirmación."
                        + "#Lectura crítica#Comprensión de textos#Función de la evidencia",
                "EXT-202#Una universidad desea clasificar sus reactivos por competencia."
                        + "#¿Qué estructura de datos permite buscar una competencia por su nombre en tiempo promedio constante?"
                        + "#Lista enlazada;Pila;Tabla hash;Cola#C"
                        + "#La tabla hash accede por clave en tiempo promedio O(1)."
                        + "#Diseño de software#Estructuras de datos#Tablas hash"
        };
        return raw[n];
    }
}
