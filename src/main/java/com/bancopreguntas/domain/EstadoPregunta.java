package com.bancopreguntas.domain;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida de una pregunta. Cada estado tiene una etiqueta y un color
 * asociado (HU02: "los estados se deben visualizar con colores") y conoce
 * a cuales otros estados puede transicionar, para evitar cambios de estado
 * invalidos desde la capa de negocio.
 */
public enum EstadoPregunta {

    BORRADOR("Borrador", new Color(158, 158, 158)) {
        @Override
        public Set<EstadoPregunta> transicionesValidas() {
            return EnumSet.of(PENDIENTE_REVISION, ELIMINADA);
        }
    },
    PENDIENTE_REVISION("Pendiente de revisión", new Color(255, 152, 0)) {
        @Override
        public Set<EstadoPregunta> transicionesValidas() {
            return EnumSet.of(EN_REVISION, BORRADOR, ELIMINADA);
        }
    },
    EN_REVISION("En revisión", new Color(33, 150, 243)) {
        @Override
        public Set<EstadoPregunta> transicionesValidas() {
            return EnumSet.of(APROBADA, RECHAZADA, ELIMINADA);
        }
    },
    APROBADA("Aprobada", new Color(76, 175, 80)) {
        @Override
        public Set<EstadoPregunta> transicionesValidas() {
            return EnumSet.noneOf(EstadoPregunta.class);
        }
    },
    RECHAZADA("Rechazada", new Color(244, 67, 54)) {
        @Override
        public Set<EstadoPregunta> transicionesValidas() {
            return EnumSet.of(BORRADOR, ELIMINADA);
        }
    },
    ELIMINADA("Eliminada", new Color(117, 117, 117)) {
        @Override
        public Set<EstadoPregunta> transicionesValidas() {
            return EnumSet.noneOf(EstadoPregunta.class);
        }
    };

    private final String etiqueta;
    private final Color color;

    EstadoPregunta(String etiqueta, Color color) {
        this.etiqueta = etiqueta;
        this.color = color;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public Color getColor() {
        return color;
    }

    /** Conjunto de estados a los que se puede transicionar desde este estado. */
    public abstract Set<EstadoPregunta> transicionesValidas();

    public boolean puedeTransicionarA(EstadoPregunta destino) {
        return transicionesValidas().contains(destino);
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
