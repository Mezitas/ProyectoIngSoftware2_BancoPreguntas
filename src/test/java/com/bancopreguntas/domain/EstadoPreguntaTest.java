package com.bancopreguntas.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoPreguntaTest {

    @Test
    void todosLosEstadosTienenColorAsociado() {
        for (EstadoPregunta estado : EstadoPregunta.values()) {
            assertNotNull(estado.getColor(), "El estado " + estado + " debe tener un color asociado");
        }
    }

    @Test
    void borradorSoloPuedeTransicionarAPendienteDeRevision() {
        assertTrue(EstadoPregunta.BORRADOR.puedeTransicionarA(EstadoPregunta.PENDIENTE_REVISION));
        assertFalse(EstadoPregunta.BORRADOR.puedeTransicionarA(EstadoPregunta.APROBADA));
        assertFalse(EstadoPregunta.BORRADOR.puedeTransicionarA(EstadoPregunta.EN_REVISION));
    }

    @Test
    void pendienteDeRevisionPuedeVolverABorradorOPasarAEnRevision() {
        assertTrue(EstadoPregunta.PENDIENTE_REVISION.puedeTransicionarA(EstadoPregunta.EN_REVISION));
        assertTrue(EstadoPregunta.PENDIENTE_REVISION.puedeTransicionarA(EstadoPregunta.BORRADOR));
        assertFalse(EstadoPregunta.PENDIENTE_REVISION.puedeTransicionarA(EstadoPregunta.APROBADA));
    }

    @Test
    void aprobadaEsUnEstadoFinalSinTransicionesValidas() {
        assertTrue(EstadoPregunta.APROBADA.transicionesValidas().isEmpty());
    }
}
