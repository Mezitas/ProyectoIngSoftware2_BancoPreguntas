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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PreguntaServiceImplTest {

    private PreguntaRepository preguntaRepository;
    private UsuarioRepository usuarioRepository;
    private SujetoAsignacion sujetoAsignacion;
    private PreguntaService service;

    private Usuario autor;
    private Usuario otroAutor;
    private Usuario administrador;
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
        otroAutor = new Usuario("Otro Autor", "otro@test.com", Rol.AUTOR);
        administrador = new Usuario("Administrador", "admin@test.com", Rol.ADMINISTRADOR);
        revisor1 = new Usuario("Revisor Uno", "revisor1@test.com", Rol.REVISOR);
        revisor2 = new Usuario("Revisor Dos", "revisor2@test.com", Rol.REVISOR);
        usuarioRepository.guardar(autor);
        usuarioRepository.guardar(otroAutor);
        usuarioRepository.guardar(administrador);
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
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build(), autor);

        assertNotNull(pregunta.getId());
        assertEquals(EstadoPregunta.BORRADOR, pregunta.getEstado());
        assertTrue(preguntaRepository.buscarPorId(pregunta.getId()).isPresent());
    }

    @Test
    void crearPreguntaInvalidaLanzaValidacionExceptionYNoLaGuarda() {
        Pregunta preguntaInvalida = preguntaValidaBuilder().contexto("").build();

        assertThrows(ValidacionException.class, () -> service.crearPregunta(preguntaInvalida, autor));
        assertTrue(preguntaRepository.buscarPorId(preguntaInvalida.getId()).isEmpty());
    }

    // ---------- HU02 ----------

    @Test
    void cambiarEstadoDeBorradorAPendienteDeRevisionEsValido() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build(), autor);

        Pregunta actualizada = service.cambiarEstado(pregunta.getId(), EstadoPregunta.PENDIENTE_REVISION, autor);

        assertEquals(EstadoPregunta.PENDIENTE_REVISION, actualizada.getEstado());
    }

    @Test
    void cambiarEstadoDeBorradorAAprobadaNoEsValido() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build(), autor);

        assertThrows(IllegalStateException.class,
                () -> service.cambiarEstado(pregunta.getId(), EstadoPregunta.APROBADA, autor));
    }

    // ---------- HU03 ----------

    @Test
    void listarPreguntasPorAutorAplicaPaginacion() {
        for (int i = 0; i < 7; i++) {
            service.crearPregunta(preguntaValidaBuilder().build(), autor);
        }

        ResultadoPaginado<Pregunta> pagina1 = service.listarPreguntasPorAutor(autor, null, 1, 5);
        ResultadoPaginado<Pregunta> pagina2 = service.listarPreguntasPorAutor(autor, null, 2, 5);

        assertEquals(5, pagina1.getElementos().size());
        assertEquals(2, pagina2.getElementos().size());
        assertEquals(7, pagina1.getTotalElementos());
        assertEquals(2, pagina1.getTotalPaginas());
    }

    @Test
    void listarPreguntasPorAutorAplicaFiltroPorTema() {
        service.crearPregunta(preguntaValidaBuilder().tema("Matemáticas").build(), autor);
        service.crearPregunta(preguntaValidaBuilder().tema("Historia").build(), autor);

        ResultadoPaginado<Pregunta> resultado = service.listarPreguntasPorAutor(
                autor, FiltroPregunta.vacio().tema("Historia"), 1, 10);

        assertEquals(1, resultado.getTotalElementos());
        assertEquals("Historia", resultado.getElementos().get(0).getTema());
    }

    @Test
    void listarPreguntasNoDevuelvePreguntasDeOtroAutor() {
        service.crearPregunta(preguntaValidaBuilder().build(), autor);
        service.crearPregunta(preguntaValidaBuilder().autorId(otroAutor.getId()).build(), otroAutor);

        ResultadoPaginado<Pregunta> resultado = service.listarPreguntasPorAutor(autor, null, 1, 10);

        assertEquals(1, resultado.getTotalElementos());
    }

    // ---------- HU04 ----------

    @Test
    void asignarRevisoresCambiaEstadoANotificaAlObservador() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build(), autor);
        service.cambiarEstado(pregunta.getId(), EstadoPregunta.PENDIENTE_REVISION, autor);

        List<Usuario> revisoresNotificados = new ArrayList<>();
        ObservadorAsignacion observadorDePrueba = (p, revisores) -> revisoresNotificados.addAll(revisores);
        sujetoAsignacion.suscribir(observadorDePrueba);

        Pregunta actualizada = service.asignarRevisores(pregunta.getId(),
                List.of(revisor1.getId(), revisor2.getId()), administrador);

        assertEquals(EstadoPregunta.EN_REVISION, actualizada.getEstado());
        assertEquals(2, actualizada.getRevisoresAsignados().size());
        assertEquals(2, revisoresNotificados.size());
        assertTrue(revisoresNotificados.contains(revisor1));
        assertTrue(revisoresNotificados.contains(revisor2));
    }

    @Test
    void noSePuedenAsignarRevisoresAUnaPreguntaEnBorrador() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build(), autor);

        assertThrows(IllegalStateException.class,
                () -> service.asignarRevisores(pregunta.getId(), List.of(revisor1.getId()), administrador));
    }

    @Test
    void debeAsignarseAlMenosUnRevisor() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build(), autor);
        service.cambiarEstado(pregunta.getId(), EstadoPregunta.PENDIENTE_REVISION, autor);

        assertThrows(IllegalArgumentException.class,
                () -> service.asignarRevisores(pregunta.getId(), List.of(), administrador));
    }

    @Test
    void listarPendientesDeRevisionSoloIncluyeEseEstado() {
        Pregunta p1 = service.crearPregunta(preguntaValidaBuilder().build(), autor);
        service.cambiarEstado(p1.getId(), EstadoPregunta.PENDIENTE_REVISION, autor);
        service.crearPregunta(preguntaValidaBuilder().build(), autor); // se queda en Borrador

        List<Pregunta> pendientes = service.listarPendientesDeRevision(administrador);

        assertEquals(1, pendientes.size());
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, pendientes.get(0).getEstado());
    }

    @Test
    @SuppressWarnings("deprecation")
    void listarPendientesSinActorAutenticadoFallaDeFormaSegura() {
        assertThrows(SecurityException.class, () -> service.listarPendientesDeRevision());
    }

    @Test
    void soloElPropietarioPuedeEditarYLaEdicionValidaLaPregunta() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build(), autor);
        Pregunta nuevoContenido = preguntaValidaBuilder().tema("Geometría").build();

        assertThrows(SecurityException.class,
                () -> service.actualizarPregunta(pregunta.getId(), nuevoContenido, otroAutor));
        Pregunta actualizada = service.actualizarPregunta(pregunta.getId(), nuevoContenido, autor);
        assertEquals("Geometría", actualizada.getTema());

        Pregunta invalida = preguntaValidaBuilder().contexto("").build();
        assertThrows(ValidacionException.class,
                () -> service.actualizarPregunta(pregunta.getId(), invalida, autor));
        assertEquals("Geometría", pregunta.getTema());
    }

    @Test
    void revisorSoloVeYResuelvePreguntasAsignadas() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build(), autor);
        service.cambiarEstado(pregunta.getId(), EstadoPregunta.PENDIENTE_REVISION, autor);
        service.asignarRevisores(pregunta.getId(), List.of(revisor1.getId()), administrador);

        assertEquals(1, service.listarAsignadasARevisor(revisor1).size());
        assertTrue(service.listarAsignadasARevisor(revisor2).isEmpty());
        assertThrows(SecurityException.class,
                () -> service.resolverRevision(pregunta.getId(), true, revisor2));
        assertEquals(EstadoPregunta.APROBADA,
                service.resolverRevision(pregunta.getId(), true, revisor1).getEstado());
    }

    @Test
    void validaRolAdministradorYRolRevisorAlAsignar() {
        Pregunta pregunta = service.crearPregunta(preguntaValidaBuilder().build(), autor);
        service.cambiarEstado(pregunta.getId(), EstadoPregunta.PENDIENTE_REVISION, autor);
        assertThrows(SecurityException.class,
                () -> service.asignarRevisores(pregunta.getId(), List.of(revisor1.getId()), autor));
        assertThrows(SecurityException.class,
                () -> service.asignarRevisores(pregunta.getId(), List.of(autor.getId()), administrador));
    }

    @Test
    void noPermiteCrearPreguntaEnOtroEstadoNiConIdExistente() {
        Pregunta fueraDeBorrador = preguntaValidaBuilder()
                .estado(EstadoPregunta.PENDIENTE_REVISION).build();
        assertThrows(SecurityException.class, () -> service.crearPregunta(fueraDeBorrador, autor));

        Pregunta original = service.crearPregunta(preguntaValidaBuilder().build(), autor);
        Pregunta duplicada = preguntaValidaBuilder().id(original.getId()).build();
        assertThrows(IllegalArgumentException.class, () -> service.crearPregunta(duplicada, autor));
    }

    @Test
    void cuentaLasPreguntasAprobadasParaLaGraficaDelAutor() {
        Pregunta aprobada = service.crearPregunta(preguntaValidaBuilder().build(), autor);
        service.cambiarEstado(aprobada.getId(), EstadoPregunta.PENDIENTE_REVISION, autor);
        service.asignarRevisores(aprobada.getId(), List.of(revisor1.getId()), administrador);
        service.resolverRevision(aprobada.getId(), true, revisor1);

        Map<String, Integer> conteos = service.contarEstadosVisiblesPorAutor(autor);

        assertEquals(1, conteos.get("Aprobada"));
        assertEquals(0, conteos.get("Borrador"));
        assertEquals(0, conteos.get("En revisión"));
        assertEquals(0, conteos.get("Eliminada"));
        assertEquals(conteos, service.contarEstadosVisiblesPorAutor(autor.getId()));
    }

    @Test
    void cuentaLasPreguntasRechazadasParaLaGraficaDelAutor() {
        Pregunta rechazada = service.crearPregunta(preguntaValidaBuilder().build(), autor);
        service.cambiarEstado(rechazada.getId(), EstadoPregunta.PENDIENTE_REVISION, autor);
        service.asignarRevisores(rechazada.getId(), List.of(revisor1.getId()), administrador);
        service.resolverRevision(rechazada.getId(), false, revisor1);

        Map<String, Integer> conteos = service.contarEstadosVisiblesPorAutor(autor);

        assertEquals(1, conteos.get("Rechazada"));
        assertEquals(0, conteos.get("Aprobada"));
        assertEquals(0, conteos.get("En revisión"));
    }
}
