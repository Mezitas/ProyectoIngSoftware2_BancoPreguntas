package com.bancopreguntas.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Pregunta de seleccion multiple con unica respuesta del banco Saber Pro,
 * construida siguiendo los principios de Diseno Centrado en Evidencia (HU01):
 * contexto, pregunta directa, 4 distractores, respuesta correcta,
 * justificacion, bibliografia, competencia, tema, subtema y nivel de
 * dificultad.
 *
 * La creacion se realiza mediante el patron de diseno GoF "Builder", lo que
 * evita constructores con muchos parametros y hace explicita la construccion
 * paso a paso de un objeto complejo e inmutable en sus datos de identidad.
 */
public class Pregunta implements Serializable {

    private final String id;
    private String contexto;
    private String preguntaDirecta;
    private List<String> distractores;
    private String respuestaCorrecta;
    private String justificacion;
    private String bibliografia;
    private String competencia;
    private String tema;
    private String subtema;
    private NivelDificultad nivelDificultad;
    private EstadoPregunta estado;
    private final String autorId;
    private List<String> revisoresAsignados;
    private final LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private Pregunta(Builder b) {
        this.id = b.id;
        this.contexto = b.contexto;
        this.preguntaDirecta = b.preguntaDirecta;
        this.distractores = new ArrayList<>(b.distractores);
        this.respuestaCorrecta = b.respuestaCorrecta;
        this.justificacion = b.justificacion;
        this.bibliografia = b.bibliografia;
        this.competencia = b.competencia;
        this.tema = b.tema;
        this.subtema = b.subtema;
        this.nivelDificultad = b.nivelDificultad;
        this.estado = b.estado;
        this.autorId = b.autorId;
        this.revisoresAsignados = new ArrayList<>();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = this.fechaCreacion;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ---- Getters ----

    public String getId() {
        return id;
    }

    public String getContexto() {
        return contexto;
    }

    public String getPreguntaDirecta() {
        return preguntaDirecta;
    }

    public List<String> getDistractores() {
        return Collections.unmodifiableList(distractores);
    }

    public String getRespuestaCorrecta() {
        return respuestaCorrecta;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public String getBibliografia() {
        return bibliografia;
    }

    public String getCompetencia() {
        return competencia;
    }

    public String getTema() {
        return tema;
    }

    public String getSubtema() {
        return subtema;
    }

    public NivelDificultad getNivelDificultad() {
        return nivelDificultad;
    }

    public EstadoPregunta getEstado() {
        return estado;
    }

    public String getAutorId() {
        return autorId;
    }

    public List<String> getRevisoresAsignados() {
        return Collections.unmodifiableList(revisoresAsignados);
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    // ---- Mutaciones controladas (usadas por la capa de negocio) ----

    public void cambiarEstado(EstadoPregunta nuevoEstado) {
        this.estado = nuevoEstado;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void asignarRevisores(List<String> revisoresIds) {
        this.revisoresAsignados = new ArrayList<>(revisoresIds);
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void actualizarContenido(String contexto, String preguntaDirecta, List<String> distractores,
                                     String respuestaCorrecta, String justificacion, String bibliografia,
                                     String competencia, String tema, String subtema,
                                     NivelDificultad nivelDificultad) {
        this.contexto = contexto;
        this.preguntaDirecta = preguntaDirecta;
        this.distractores = new ArrayList<>(distractores);
        this.respuestaCorrecta = respuestaCorrecta;
        this.justificacion = justificacion;
        this.bibliografia = bibliografia;
        this.competencia = competencia;
        this.tema = tema;
        this.subtema = subtema;
        this.nivelDificultad = nivelDificultad;
        this.fechaActualizacion = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return preguntaDirecta;
    }

    /** Builder (GoF) para construir instancias de {@link Pregunta} paso a paso. */
    public static class Builder {
        private String id = UUID.randomUUID().toString();
        private String contexto = "";
        private String preguntaDirecta = "";
        private List<String> distractores = new ArrayList<>();
        private String respuestaCorrecta = "";
        private String justificacion = "";
        private String bibliografia = "";
        private String competencia = "";
        private String tema = "";
        private String subtema = "";
        private NivelDificultad nivelDificultad;
        private EstadoPregunta estado = EstadoPregunta.BORRADOR;
        private String autorId;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder contexto(String contexto) {
            this.contexto = contexto;
            return this;
        }

        public Builder preguntaDirecta(String preguntaDirecta) {
            this.preguntaDirecta = preguntaDirecta;
            return this;
        }

        public Builder distractores(List<String> distractores) {
            this.distractores = new ArrayList<>(distractores);
            return this;
        }

        public Builder respuestaCorrecta(String respuestaCorrecta) {
            this.respuestaCorrecta = respuestaCorrecta;
            return this;
        }

        public Builder justificacion(String justificacion) {
            this.justificacion = justificacion;
            return this;
        }

        public Builder bibliografia(String bibliografia) {
            this.bibliografia = bibliografia;
            return this;
        }

        public Builder competencia(String competencia) {
            this.competencia = competencia;
            return this;
        }

        public Builder tema(String tema) {
            this.tema = tema;
            return this;
        }

        public Builder subtema(String subtema) {
            this.subtema = subtema;
            return this;
        }

        public Builder nivelDificultad(NivelDificultad nivelDificultad) {
            this.nivelDificultad = nivelDificultad;
            return this;
        }

        public Builder estado(EstadoPregunta estado) {
            this.estado = estado;
            return this;
        }

        public Builder autorId(String autorId) {
            this.autorId = autorId;
            return this;
        }

        public Pregunta build() {
            return new Pregunta(this);
        }
    }
}
