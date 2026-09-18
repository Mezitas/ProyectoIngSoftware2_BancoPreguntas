package com.bancopreguntas.service;

import java.util.Collections;
import java.util.List;

/**
 * Pagina de resultados generica, usada por el listado de preguntas (HU03).
 */
public class ResultadoPaginado<T> {

    private final List<T> elementos;
    private final int pagina;
    private final int tamanoPagina;
    private final int totalElementos;

    public ResultadoPaginado(List<T> elementos, int pagina, int tamanoPagina, int totalElementos) {
        this.elementos = Collections.unmodifiableList(elementos);
        this.pagina = pagina;
        this.tamanoPagina = tamanoPagina;
        this.totalElementos = totalElementos;
    }

    public List<T> getElementos() {
        return elementos;
    }

    public int getPagina() {
        return pagina;
    }

    public int getTamanoPagina() {
        return tamanoPagina;
    }

    public int getTotalElementos() {
        return totalElementos;
    }

    public int getTotalPaginas() {
        if (tamanoPagina <= 0) return 1;
        return Math.max(1, (int) Math.ceil((double) totalElementos / tamanoPagina));
    }

    public boolean hayPaginaSiguiente() {
        return pagina < getTotalPaginas();
    }

    public boolean hayPaginaAnterior() {
        return pagina > 1;
    }
}
