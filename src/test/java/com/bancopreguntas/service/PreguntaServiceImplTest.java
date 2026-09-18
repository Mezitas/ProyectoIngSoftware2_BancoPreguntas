package com.bancopreguntas.service;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.NivelDificultad;
import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.domain.Rol;
import com.bancopreguntas.domain.Usuario;
import com.bancopreguntas.notificacion.ObservadorAsignacion;
import com.bancopreguntas.notificacion.SujetoAsignacion;
import com.bancopreguntas.repository.PreguntaRepository;
import com.bancopreguntas.repository.PreguntaRepositoryEnMemoria;
import com.bancopreguntas.repository.UsuarioRepository;
import com.bancopreguntas.repository.UsuarioRepositoryEnMemoria;
import com.bancopreguntas.validacion.ValidadorEstructuralPregunta;
import com.bancopreguntas.validacion.ValidadorPregunta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PreguntaServiceImplTest {

    private PreguntaRepository preguntaRepository;
    private UsuarioRepository usuarioRepository;
    private SujetoAsignacion sujetoAsignacion;
    private PreguntaService service;

    private Usuario autor;
    private Usuario revisor1;
    private Usuario revisor2;

    @BeforeEach
    void inicializar() {
        preguntaRepository = new PreguntaRepositoryEnMemoria();
        usuarioRepository = new UsuarioRepositoryEnMemoria();
        ValidadorPregunta validador = new ValidadorEstructuralPregunta();
        sujetoAsignacion = new SujetoAsignacion();

        service = new PreguntaServiceImpl(preguntaRepository, usuarioRepository, validador, sujetoAsignacion);

        autor = new Usuario("Autor de prueba", "autor@test.com", Rol.AUTOR);
        revisor1 = new Usuario("Revisor Uno", "revisor1@test.com", Rol.REVISOR);
        revisor2 = new Usuario("Revisor Dos", "revisor2@test.com", Rol.REVISOR);
        usuarioRepository.guardar(autor);
        usuarioRepository.guardar(revisor1);
        usuarioRepository.guardar(revisor2);
    }

    private Pregunta.Builder preguntaValidaBuilder() {
        return Pregunta.builder()
                .contexto("Contexto")
                .preguntaDirecta("¿Pregunta?")
                .distractores(List.of("A", "B", "C", "D"))
                .respuestaCorrecta("E")
                .justificacion("Justificación")
                .bibliografia("Bibliografía")
                .competencia("Competencia")
                .tema("Matemáticas")
                .subtema("Álgebra")
                .nivelDificultad(NivelDificultad.MEDIO)
                .autorId(autor.getId());
    }

    // ---------- HU01 ----------

    @Test
    void crearPreguntaValidaLaGuardaEnEstadoBorrador() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build());

        assertNotNull(pregunta.getId());
        assertEquals(EstadoPregunta.BORRADOR, pregunta.getEstado());
        assertTrue(preguntaRepository.buscarPorId(pregunta.getId()).isPresent());
    }

    @Test
    void crearPreguntaInvalidaLanzaValidacionExceptionYNoLaGuarda() {
        Pregunta preguntaInvalida = preguntaValidaBuilder().contexto("").build();

        assertThrows(ValidacionException.class, () -> service.crearPregunta(preguntaInvalida));
        assertTrue(preguntaRepository.buscarPorId(preguntaInvalida.getId()).isEmpty());
    }

    // ---------- HU02 ----------

    @Test
    void cambiarEstadoDeBorradorAPendienteDeRevisionEsValido() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build());

        Pregunta actualizada = service.cambiarEstado(pregunta.getId(), EstadoPregunta.PENDIENTE_REVISION);

        assertEquals(EstadoPregunta.PENDIENTE_REVISION, actualizada.getEstado());
    }

    @Test
    void cambiarEstadoDeBorradorAAprobadaNoEsValido() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build());

        assertThrows(IllegalStateException.class,
                () -> service.cambiarEstado(pregunta.getId(), EstadoPregunta.APROBADA));
    }

    // ---------- HU03 ----------

    @Test
    void listarPreguntasPorAutorAplicaPaginacion() {
        for (int i = 0; i < 7; i++) {
            service.crearPregunta(preguntaValidaBuilder().build());
        }

        ResultadoPaginado<Pregunta> pagina1 = service.listarPreguntasPorAutor(autor.getId(), null, 1, 5);
        ResultadoPaginado<Pregunta> pagina2 = service.listarPreguntasPorAutor(autor.getId(), null, 2, 5);

        assertEquals(5, pagina1.getElementos().size());
        assertEquals(2, pagina2.getElementos().size());
        assertEquals(7, pagina1.getTotalElementos());
        assertEquals(2, pagina1.getTotalPaginas());
    }

    @Test
    void listarPreguntasPorAutorAplicaFiltroPorTema() {
        service.crearPregunta(preguntaValidaBuilder().tema("Matemáticas").build());
        service.crearPregunta(preguntaValidaBuilder().tema("Historia").build());

        ResultadoPaginado<Pregunta> resultado = service.listarPreguntasPorAutor(
                autor.getId(), FiltroPregunta.vacio().tema("Historia"), 1, 10);

        assertEquals(1, resultado.getTotalElementos());
        assertEquals("Historia", resultado.getElementos().get(0).getTema());
    }

    @Test
    void listarPreguntasNoDevuelvePreguntasDeOtroAutor() {
        service.crearPregunta(preguntaValidaBuilder().build());

        Usuario otroAutor = new Usuario("Otro Autor", "otro@test.com", Rol.AUTOR);
        usuarioRepository.guardar(otroAutor);
        service.crearPregunta(preguntaValidaBuilder().autorId(otroAutor.getId()).build());

        ResultadoPaginado<Pregunta> resultado = service.listarPreguntasPorAutor(autor.getId(), null, 1, 10);

        assertEquals(1, resultado.getTotalElementos());
    }

    // ---------- HU04 ----------

    @Test
    void asignarRevisoresCambiaEstadoANotificaAlObservador() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build());
        service.cambiarEstado(pregunta.getId(), EstadoPregunta.PENDIENTE_REVISION);

        List<Usuario> revisoresNotificados = new ArrayList<>();
        ObservadorAsignacion observadorDePrueba = (p, revisores) -> revisoresNotificados.addAll(revisores);
        sujetoAsignacion.suscribir(observadorDePrueba);

        Pregunta actualizada = service.asignarRevisores(pregunta.getId(),
                List.of(revisor1.getId(), revisor2.getId()));

        assertEquals(EstadoPregunta.EN_REVISION, actualizada.getEstado());
        assertEquals(2, actualizada.getRevisoresAsignados().size());
        assertEquals(2, revisoresNotificados.size());
        assertTrue(revisoresNotificados.contains(revisor1));
        assertTrue(revisoresNotificados.contains(revisor2));
    }

    @Test
    void noSePuedenAsignarRevisoresAUnaPreguntaEnBorrador() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build());

        assertThrows(IllegalStateException.class,
                () -> service.asignarRevisores(pregunta.getId(), List.of(revisor1.getId())));
    }

    @Test
    void debeAsignarseAlMenosUnRevisor() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build());
        service.cambiarEstado(pregunta.getId(), EstadoPregunta.PENDIENTE_REVISION);

        assertThrows(IllegalArgumentException.class,
                () -> service.asignarRevisores(pregunta.getId(), List.of()));
    }

    @Test
    void listarPendientesDeRevisionSoloIncluyeEseEstado() {
        Pregunta p1 = service.crearPregunta(preguntaValidaBuilder().build());
        service.cambiarEstado(p1.getId(), EstadoPregunta.PENDIENTE_REVISION);
        service.crearPregunta(preguntaValidaBuilder().build()); // se queda en Borrador

        List<Pregunta> pendientes = service.listarPendientesDeRevision();

        assertEquals(1, pendientes.size());
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, pendientes.get(0).getEstado());
    }
}
