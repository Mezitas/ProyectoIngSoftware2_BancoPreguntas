package com.bancopreguntas;

import com.bancopreguntas.domain.EstadoPregunta;
import com.bancopreguntas.domain.NivelDificultad;
import com.bancopreguntas.domain.Pregunta;
import com.bancopreguntas.domain.Rol;
import com.bancopreguntas.domain.Usuario;
import com.bancopreguntas.notificacion.ConsoleEmailService;
import com.bancopreguntas.notificacion.NotificadorEmailObservador;
import com.bancopreguntas.notificacion.SujetoAsignacion;
import com.bancopreguntas.notificacion.SujetoPreguntas;
import com.bancopreguntas.repository.PreguntaRepository;
import com.bancopreguntas.repository.PreguntaRepositoryEnMemoria;
import com.bancopreguntas.repository.UsuarioRepository;
import com.bancopreguntas.repository.UsuarioRepositoryEnMemoria;
import com.bancopreguntas.service.PreguntaService;
import com.bancopreguntas.service.PreguntaServiceImpl;
import com.bancopreguntas.ui.PreguntaController;
import com.bancopreguntas.ui.VentanaPrincipal;
import com.bancopreguntas.validacion.ValidadorEstructuralPregunta;
import com.bancopreguntas.validacion.ValidadorPregunta;

import javax.swing.*;
import java.util.List;

/**
 * Punto de entrada de la aplicacion. Se encarga de "conectar" las tres
 * capas (Datos, Negocio, Presentacion) siguiendo inyeccion de dependencias
 * manual por constructor, y de cargar datos de ejemplo para la
 * sustentacion en vivo.
 */
public class App {

    public static void main(String[] args) {
        // ---- Capa de datos ----
        UsuarioRepository usuarioRepository = UsuarioRepositoryEnMemoria.getInstance();
        PreguntaRepository preguntaRepository = PreguntaRepositoryEnMemoria.getInstance();

        // ---- Capa de negocio ----
        ValidadorPregunta validador = new ValidadorEstructuralPregunta();
        SujetoAsignacion sujetoAsignacion = new SujetoAsignacion();
        SujetoPreguntas sujetoPreguntas = new SujetoPreguntas();
        sujetoAsignacion.suscribir(new NotificadorEmailObservador(new ConsoleEmailService()));

        PreguntaService preguntaService =
                new PreguntaServiceImpl(preguntaRepository, usuarioRepository, validador,
                        sujetoAsignacion, sujetoPreguntas);

        cargarDatosDeEjemplo(usuarioRepository, preguntaRepository);

        // ---- Capa de presentacion ----
        PreguntaController controller = new PreguntaController(preguntaService, usuarioRepository);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Se usa el look and feel por defecto si el del sistema no esta disponible.
            }
            new VentanaPrincipal(controller, sujetoPreguntas).setVisible(true);
        });
    }

    private static void cargarDatosDeEjemplo(UsuarioRepository usuarioRepository,
                                              PreguntaRepository preguntaRepository) {
        Usuario ana = new Usuario("Ana Torres", "anatorres@unicauca.edu.co", Rol.AUTOR);
        Usuario david = new Usuario("David Gómez", "josephtrujillo@unicauca.edu.co", Rol.ADMINISTRADOR);
        Usuario juan = new Usuario("Juan Meza", "jdmeza@unicauca.edu.co", Rol.REVISOR);
        Usuario pablo = new Usuario("Pablo Hernandez", "jphernandez@unicauca.edu.co", Rol.REVISOR);

        List.of(ana, david, juan, pablo).forEach(usuarioRepository::guardar);

        Pregunta p1 = Pregunta.builder()
                .contexto("Un estudiante analiza el crecimiento poblacional de una ciudad a lo largo de 10 años.")
                .preguntaDirecta("¿Cuál de las siguientes funciones modela mejor un crecimiento exponencial?")
                .distractores(List.of("f(x) = 2x + 3", "f(x) = x^2 - 1", "f(x) = 5", "f(x) = log(x)"))
                .respuestaCorrecta("f(x) = 2^x")
                .justificacion("La función exponencial 2^x crece proporcionalmente a su valor actual, "
                        + "a diferencia de las funciones lineal, cuadrática, constante y logarítmica dadas.")
                .bibliografia("Stewart, J. (2018). Cálculo de una variable. Cengage Learning.")
                .competencia("Razonamiento cuantitativo")
                .tema("Funciones")
                .subtema("Crecimiento exponencial")
                .nivelDificultad(NivelDificultad.MEDIO)
                .autorId(ana.getId())
                .build();
        preguntaRepository.guardar(p1);

        Pregunta p2 = Pregunta.builder()
                .contexto("Una empresa de software evalúa la calidad de su arquitectura.")
                .preguntaDirecta("¿Cuál atributo de calidad se relaciona directamente con la facilidad de "
                        + "modificar el software sin afectar otros componentes?")
                .distractores(List.of("Disponibilidad", "Usabilidad", "Portabilidad", "Interoperabilidad"))
                .respuestaCorrecta("Modificabilidad")
                .justificacion("La modificabilidad es el atributo de calidad que mide el costo y riesgo de "
                        + "realizar cambios en el software.")
                .bibliografia("Bass, L., Clements, P., & Kazman, R. (2012). Software Architecture in Practice.")
                .competencia("Diseño de software")
                .tema("Arquitectura de software")
                .subtema("Atributos de calidad")
                .nivelDificultad(NivelDificultad.BAJO)
                .autorId(ana.getId())
                .estado(EstadoPregunta.PENDIENTE_REVISION)
                .build();
        preguntaRepository.guardar(p2);
    }
}
