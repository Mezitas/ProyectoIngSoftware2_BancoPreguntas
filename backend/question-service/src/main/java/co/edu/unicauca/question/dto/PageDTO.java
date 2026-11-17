package co.edu.unicauca.question.dto;

import java.util.List;

/** Página de resultados: contenido más los datos necesarios para paginar en el cliente. */
public record PageDTO<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
