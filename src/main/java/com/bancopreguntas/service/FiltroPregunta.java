package com.bancopreguntas.service;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.NivelDificultad;

/**
 * Criterios de filtrado opcionales para el listado de preguntas (HU03:
 * "aplicar paginacion y filtros para hacer funcional y usable la busqueda y
 * listado de preguntas"). Cualquier campo nulo o en blanco se ignora.
 */
public class FiltroPregunta {

    private String texto;
    private String tema;
    private String subtema;
    private String competencia;
    private NivelDificultad nivelDificultad;
    private EstadoPregunta estado;

    public static FiltroPregunta vacio() {
        return new FiltroPregunta();
    }

    public String getTexto() {
        return texto;
    }

    public FiltroPregunta texto(String texto) {
        this.texto = texto;
        return this;
    }

    public String getTema() {
        return tema;
    }

    public FiltroPregunta tema(String tema) {
        this.tema = tema;
        return this;
    }

    public String getSubtema() {
        return subtema;
    }

    public FiltroPregunta subtema(String subtema) {
        this.subtema = subtema;
        return this;
    }

    public String getCompetencia() {
        return competencia;
    }

    public FiltroPregunta competencia(String competencia) {
        this.competencia = competencia;
        return this;
    }

    public NivelDificultad getNivelDificultad() {
        return nivelDificultad;
    }

    public FiltroPregunta nivelDificultad(NivelDificultad nivelDificultad) {
        this.nivelDificultad = nivelDificultad;
        return this;
    }

    public EstadoPregunta getEstado() {
        return estado;
    }

    public FiltroPregunta estado(EstadoPregunta estado) {
        this.estado = estado;
        return this;
    }
}
