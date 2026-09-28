package com.bancopreguntas.repository;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.NivelDificultad;
import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.domain.Rol;
import com.bancopreguntas.domain.Usuario;
import com.bancopreguntas.notificacion.SujetoAsignacion;
import com.bancopreguntas.notificacion.SujetoPreguntas;
import com.bancopreguntas.service.AutenticacionService;
import com.bancopreguntas.service.AutenticacionServiceImpl;
import com.bancopreguntas.service.PasswordHasher;
import com.bancopreguntas.service.PreguntaService;
import com.bancopreguntas.service.PreguntaServiceImpl;
import com.bancopreguntas.validacion.ValidadorEstructuralPregunta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLitePersistenceTest {

    @TempDir
    Path directorioTemporal;

    @Test
    void persisteUsuariosPreguntasRevisoresYFechas() {
        SQLiteDatabase database = new SQLiteDatabase(directorioTemporal.resolve("prueba.db"));
        UsuarioRepositorySQLite usuarios = new UsuarioRepositorySQLite(database);
        PreguntaRepositorySQLite preguntas = new PreguntaRepositorySQLite(database);
        Usuario autor = new Usuario("autor", "Autora", "autor@test.local", Rol.AUTOR,
                PasswordHasher.hash("Clave123!"));
        Usuario revisor = new Usuario("revisor", "Revisor", "revisor@test.local", Rol.REVISOR,
                PasswordHasher.hash("Clave123!"));
        usuarios.guardar(autor);
        usuarios.guardar(revisor);

        LocalDateTime creada = LocalDateTime.of(2025, 1, 2, 3, 4);
        Pregunta original = Pregunta.builder()
                .id("pregunta-1")
                .contexto("Contexto")
                .preguntaDirecta("Pregunta")
                .distractores(List.of("A", "B", "C", "D"))
                .respuestaCorrecta("E")
                .justificacion("Justificación")
                .bibliografia("Bibliografía")
                .competencia("Competencia")
                .tema("Tema")
                .subtema("Subtema")
                .nivelDificultad(NivelDificultad.ALTO)
                .estado(EstadoPregunta.EN_REVISION)
                .autorId(autor.getId())
                .revisoresAsignados(List.of(revisor.getId()))
                .fechas(creada, creada.plusMinutes(2))
                .build();

        preguntas.guardar(original);
        Pregunta cargada = preguntas.buscarPorId(original.getId()).orElseThrow();

        assertEquals(original.getDistractores(), cargada.getDistractores());
        assertEquals(original.getRevisoresAsignados(), cargada.getRevisoresAsignados());
        assertEquals(creada, cargada.getFechaCreacion());
        assertEquals(List.of(cargada.getId()), preguntas.listarAsignadasARevisor(revisor.getId())
                .stream().map(Pregunta::getId).toList());
    }

    @Test
    void autenticaConHashYRegistraAuditoria() throws Exception {
        SQLiteDatabase database = new SQLiteDatabase(directorioTemporal.resolve("auth.db"));
        UsuarioRepositorySQLite usuarios = new UsuarioRepositorySQLite(database);
        Usuario usuario = new Usuario("admin", "Admin", "admin@test.local", Rol.ADMINISTRADOR,
                PasswordHasher.hash("Admin123!"));
        usuarios.guardar(usuario);
        AutenticacionService autenticacion = new AutenticacionServiceImpl(usuarios);
        assertTrue(autenticacion.autenticar("ADMIN@test.local", "Admin123!").isPresent());
        assertTrue(autenticacion.autenticar("admin@test.local", "incorrecta").isEmpty());

        Pregunta pregunta = Pregunta.builder().id("p1").contexto("c").preguntaDirecta("q")
                .distractores(List.of("a", "b", "c", "d")).respuestaCorrecta("e")
                .justificacion("j").bibliografia("b").competencia("c").tema("t").subtema("s")
                .nivelDificultad(NivelDificultad.BAJO).autorId(usuario.getId()).build();
        PreguntaRepositorySQLite preguntas = new PreguntaRepositorySQLite(database);
        preguntas.guardar(pregunta);
        new RepositorioAuditoriaSQLite(database).registrar(usuario.getId(), pregunta.getId(),
                "CREAR", "Prueba", LocalDateTime.now());
        try (Connection connection = database.abrirConexion();
             PreparedStatement statement = connection.prepareStatement("SELECT accion FROM auditoria");
             ResultSet result = statement.executeQuery()) {
            assertTrue(result.next());
            assertEquals("CREAR", result.getString(1));
        }
    }

    @Test
    void servicioRegistraAccionesDeNegocioEnLaAuditoria() throws Exception {
        SQLiteDatabase database = new SQLiteDatabase(directorioTemporal.resolve("auditoria.db"));
        UsuarioRepositorySQLite usuarios = new UsuarioRepositorySQLite(database);
        PreguntaRepositorySQLite preguntas = new PreguntaRepositorySQLite(database);
        Usuario autor = new Usuario("autor", "Autora", "autora@test.local", Rol.AUTOR, "hash");
        usuarios.guardar(autor);
        PreguntaService servicio = new PreguntaServiceImpl(preguntas, usuarios,
                new ValidadorEstructuralPregunta(), new SujetoAsignacion(), new SujetoPreguntas(),
                new RepositorioAuditoriaSQLite(database));
        Pregunta pregunta = Pregunta.builder().contexto("Contexto").preguntaDirecta("Pregunta")
                .distractores(List.of("A", "B", "C", "D")).respuestaCorrecta("E")
                .justificacion("Justificación").bibliografia("Bibliografía").competencia("Competencia")
                .tema("Tema").subtema("Subtema").nivelDificultad(NivelDificultad.MEDIO)
                .autorId(autor.getId()).build();

        Pregunta creada = servicio.crearPregunta(pregunta, autor);
        servicio.cambiarEstado(creada.getId(), EstadoPregunta.PENDIENTE_REVISION, autor);

        try (Connection connection = database.abrirConexion();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT accion FROM auditoria ORDER BY id");
             ResultSet result = statement.executeQuery()) {
            assertTrue(result.next());
            assertEquals("CREAR", result.getString(1));
            assertTrue(result.next());
            assertEquals("CAMBIAR_ESTADO", result.getString(1));
        }
    }
}
