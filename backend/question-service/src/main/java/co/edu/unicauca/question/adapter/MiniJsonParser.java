package co.edu.unicauca.question.adapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Mini parser JSON sin dependencias: convierte un texto JSON a Map/List/String/Double/Boolean/null. */
public final class MiniJsonParser {
    private MiniJsonParser() {}

    public static Object parse(String json) {
        if (json == null) throw new IllegalArgumentException("JSON nulo");
        Parser p = new Parser(json);
        Object value = p.parseValue();
        p.skipWs();
        if (!p.atEnd()) throw p.error("contenido sobrante después del valor");
        return value;
    }

    private static final class Parser {
        private final String s;
        private int i;

        Parser(String s) { this.s = s; }

        boolean atEnd() { return i >= s.length(); }
        void skipWs() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }

        IllegalArgumentException error(String msg) {
            return new IllegalArgumentException(msg + " (posición " + i + ")");
        }

        Object parseValue() {
            skipWs();
            if (atEnd()) throw error("valor inesperado: fin del texto");
            char c = s.charAt(i);
            return switch (c) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> literal("true", Boolean.TRUE);
                case 'f' -> literal("false", Boolean.FALSE);
                case 'n' -> literal("null", null);
                default -> parseNumber();
            };
        }

        Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            i++;
            skipWs();
            if (!atEnd() && s.charAt(i) == '}') { i++; return map; }
            while (true) {
                skipWs();
                if (atEnd() || s.charAt(i) != '"') throw error("se esperaba la clave entre comillas");
                String key = parseString();
                skipWs();
                if (atEnd() || s.charAt(i) != ':') throw error("se esperaba ':'");
                i++;
                map.put(key, parseValue());
                skipWs();
                if (atEnd()) throw error("objeto sin cerrar");
                char c = s.charAt(i);
                if (c == ',') { i++; continue; }
                if (c == '}') { i++; return map; }
                throw error("se esperaba ',' o '}'");
            }
        }

        List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            i++;
            skipWs();
            if (!atEnd() && s.charAt(i) == ']') { i++; return list; }
            while (true) {
                list.add(parseValue());
                skipWs();
                if (atEnd()) throw error("arreglo sin cerrar");
                char c = s.charAt(i);
                if (c == ',') { i++; continue; }
                if (c == ']') { i++; return list; }
                throw error("se esperaba ',' o ']'");
            }
        }

        String parseString() {
            i++;
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (atEnd()) throw error("cadena sin cerrar");
                char c = s.charAt(i++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    if (atEnd()) throw error("escape incompleto");
                    char e = s.charAt(i++);
                    switch (e) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            if (i + 4 > s.length()) throw error("unicode incompleto");
                            sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                            i += 4;
                        }
                        default -> throw error("escape inválido: \\" + e);
                    }
                } else {
                    sb.append(c);
                }
            }
        }

        Object literal(String word, Object value) {
            if (s.startsWith(word, i)) { i += word.length(); return value; }
            throw error("literal inválido");
        }

        Number parseNumber() {
            int start = i;
            while (i < s.length() && "-+.0123456789eE".indexOf(s.charAt(i)) >= 0) i++;
            try {
                return Double.valueOf(s.substring(start, i));
            } catch (NumberFormatException e) {
                throw error("número inválido");
            }
        }
    }
}
