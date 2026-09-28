package com.bancopreguntas;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.NivelDificultad;
import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.domain.Rol;
import com.bancopreguntas.domain.Usuario;
import com.bancopreguntas.notificacion.SujetoAsignacion;
import com.bancopreguntas.notificacion.SujetoPreguntas;
import com.bancopreguntas.repository.PreguntaRepository;
import com.bancopreguntas.repository.PreguntaRepositorySQLite;
import com.bancopreguntas.repository.RepositorioAuditoriaSQLite;
import com.bancopreguntas.repository.SQLiteDatabase;
import com.bancopreguntas.repository.UsuarioRepository;
import com.bancopreguntas.repository.UsuarioRepositorySQLite;
import com.bancopreguntas.service.AutenticacionService;
import com.bancopreguntas.service.AutenticacionServiceImpl;
import com.bancopreguntas.service.PasswordHasher;
import com.bancopreguntas.service.PreguntaService;
import com.bancopreguntas.service.PreguntaServiceImpl;
import com.bancopreguntas.ui.DialogoLogin;
import com.bancopreguntas.ui.PreguntaController;
import com.bancopreguntas.ui.SesionActual;
import com.bancopreguntas.ui.VentanaPrincipal;
import com.bancopreguntas.validacion.ValidadorEstructuralPregunta;

import javax.swing.*;
import java.nio.file.Path;
import java.util.List;

public class App {

    private static final String ANA_ID = "usuario-autora-ana";

    public static void main(String[] args) {
        SQLiteDatabase database = new SQLiteDatabase(Path.of("data", "banco-preguntas.db"));
        UsuarioRepository usuarioRepository = new UsuarioRepositorySQLite(database);
        PreguntaRepository preguntaRepository = new PreguntaRepositorySQLite(database);
        cargarUsuariosIniciales(usuarioRepository);
        cargarPreguntasIniciales(preguntaRepository);

        SujetoAsignacion sujetoAsignacion = new SujetoAsignacion();
        SujetoPreguntas sujetoPreguntas = new SujetoPreguntas();
        PreguntaService preguntaService = new PreguntaServiceImpl(
                preguntaRepository, usuarioRepository, new ValidadorEstructuralPregunta(),
                sujetoAsignacion, sujetoPreguntas, new RepositorioAuditoriaSQLite(database));
        AutenticacionService autenticacionService = new AutenticacionServiceImpl(usuarioRepository);
        PreguntaController controller = new PreguntaController(
                preguntaService, usuarioRepository, autenticacionService);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("No se pudo activar el estilo visual del sistema: " + e.getMessage());
            }
            Usuario usuario = new DialogoLogin(null, controller).mostrar();
            if (usuario != null) {
                SesionActual.setUsuarioActual(usuario);
                new VentanaPrincipal(controller, sujetoPreguntas, usuario).setVisible(true);
            }
        });
    }

    private static void cargarUsuariosIniciales(UsuarioRepository repository) {
        List<Usuario> usuarios = List.of(
                usuario("usuario-autora-ana", "Ana Torres", "ana@bancopreguntas.local",
                        Rol.AUTOR, "Autor123!"),
                usuario("usuario-admin-david", "David Gómez", "admin@bancopreguntas.local",
                        Rol.ADMINISTRADOR, "Admin123!"),
                usuario("usuario-revisor-juan", "Juan Meza", "juan@bancopreguntas.local",
                        Rol.REVISOR, "Revisor123!"),
                usuario("usuario-revisor-pablo", "Pablo Hernandez", "pablo@bancopreguntas.local",
                        Rol.REVISOR, "Revisor123!")
        );
        usuarios.forEach(repository::guardar);
    }

    private static Usuario usuario(String id, String nombre, String email, Rol rol, String password) {
        return new Usuario(id, nombre, email, rol, PasswordHasher.hash(password));
    }

    private static void cargarPreguntasIniciales(PreguntaRepository repository) {
        if (!repository.listarTodas().isEmpty()) return;
        repository.guardar(Pregunta.builder()
                .contexto("Un estudiante analiza el crecimiento poblacional de una ciudad a lo largo de 10 años.")
                .preguntaDirecta("¿Cuál de las siguientes funciones modela mejor un crecimiento exponencial?")
                .distractores(List.of("f(x) = 2x + 3", "f(x) = x^2 - 1", "f(x) = 5", "f(x) = log(x)"))
                .respuestaCorrecta("f(x) = 2^x")
                .justificacion("La función exponencial crece proporcionalmente a su valor actual.")
                .bibliografia("Stewart, J. (2018). Cálculo de una variable. Cengage Learning.")
                .competencia("Razonamiento cuantitativo")
                .tema("Funciones")
                .subtema("Crecimiento exponencial")
                .nivelDificultad(NivelDificultad.MEDIO)
                .autorId(ANA_ID)
                .build());
        repository.guardar(Pregunta.builder()
                .contexto("Una empresa de software evalúa la calidad de su arquitectura.")
                .preguntaDirecta("¿Qué atributo de calidad facilita modificar software sin afectar otros componentes?")
                .distractores(List.of("Disponibilidad", "Usabilidad", "Portabilidad", "Interoperabilidad"))
                .respuestaCorrecta("Modificabilidad")
                .justificacion("La modificabilidad mide el costo y riesgo de realizar cambios en el software.")
                .bibliografia("Bass, L., Clements, P., & Kazman, R. (2012). Software Architecture in Practice.")
                .competencia("Diseño de software")
                .tema("Arquitectura de software")
                .subtema("Atributos de calidad")
                .nivelDificultad(NivelDificultad.BAJO)
                .autorId(ANA_ID)
                .estado(EstadoPregunta.PENDIENTE_REVISION)
                .build());
    }
}
