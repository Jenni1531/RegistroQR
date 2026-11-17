package com.registroqr.registro_qr.controller;

import com.registroqr.registro_qr.model.*;
import com.registroqr.registro_qr.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/asistencias")
@CrossOrigin(origins = "*")
public class AsistenciaController {

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @PostMapping("/escanear")
    public ResponseEntity<?> registrarPorQr(@RequestBody Map<String, String> payload) {
        String token = payload.get("codigoQr");
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Codigo QR vacio"));
        }

        Optional<Alumno> alumnoOpt = alumnoRepository.findByCodigoQrToken(token.trim());
        if (alumnoOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("mensaje", "Codigo QR no valido o alumno no registrado"));
        }

        Alumno alumno = alumnoOpt.get();
        LocalDate hoy = LocalDate.now();
        long escaneosHoy = asistenciaRepository.countByAlumnoAndFecha(alumno, hoy);

        if (escaneosHoy >= 2) {
            return ResponseEntity.status(409).body(Map.of(
                "mensaje", "Limite alcanzado: " + alumno.getNombre() + " ya registro 2 asistencias hoy."
            ));
        }

        Asistencia nueva = new Asistencia();
        nueva.setAlumno(alumno);
        nueva.setFecha(hoy);
        nueva.setHoraEntrada(LocalTime.now());
        nueva.setEstado(escaneosHoy == 0 ? "ENTRADA" : "SALIDA");
        asistenciaRepository.save(nueva);

        return ResponseEntity.ok(Map.of(
            "mensaje", "Registro exitoso (" + nueva.getEstado() + ")",
            "alumno", alumno.getNombre() + " " + alumno.getApellidos(),
            "numeroControl", alumno.getNumeroControl(),
            "grupo", alumno.getGrupo().getNombreGrupo(),
            "hora", nueva.getHoraEntrada().toString()
        ));
    }

    @PostMapping("/alumnos")
    public ResponseEntity<?> registrarAlumno(@RequestBody Map<String, String> datos) {
        String numeroControl = datos.get("numeroControl").trim();
        String nombre = datos.get("nombre").trim();
        String apellidos = datos.get("apellidos").trim();
        String nombreGrupo = datos.getOrDefault("grupo", "1A").trim();

        if (usuarioRepository.findByNumeroControl(numeroControl).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("mensaje", "Ese numero de control ya esta registrado"));
        }

        Grupo grupo = grupoRepository.findAll().stream()
                .filter(g -> g.getNombreGrupo().equalsIgnoreCase(nombreGrupo))
                .findFirst()
                .orElseGet(() -> {
                    Grupo g = new Grupo();
                    g.setNombreGrupo(nombreGrupo);
                    g.setSemestre(1);
                    g.setTurno("MATUTINO");
                    return grupoRepository.save(g);
                });

        Usuario usuario = new Usuario();
        usuario.setNumeroControl(numeroControl);
        usuario.setPasswordHash(numeroControl);
        usuario.setRol("ALUMNO");
        usuario = usuarioRepository.save(usuario);

        Alumno alumno = new Alumno();
        alumno.setUsuario(usuario);
        alumno.setGrupo(grupo);
        alumno.setNumeroControl(numeroControl);
        alumno.setNombre(nombre);
        alumno.setApellidos(apellidos);
        alumno.setCodigoQrToken("QR-" + numeroControl);
        alumnoRepository.save(alumno);

        return ResponseEntity.ok(Map.of(
            "mensaje", "Alumno registrado. Su usuario y contrasena inicial es: " + numeroControl,
            "alumno", alumno
        ));
    }

@GetMapping("/dashboard")
    public ResponseEntity<?> obtenerDashboardAdmin() {
        LocalDate hoy = LocalDate.now();
        List<Alumno> todosAlumnos = alumnoRepository.findAll();
        List<Asistencia> todasAsistencias = asistenciaRepository.findAllByOrderByFechaDescHoraEntradaDesc();
        List<Asistencia> asistenciasHoy = asistenciaRepository.findByFechaOrderByHoraEntradaDesc(hoy);

        Set<LocalDate> fechasDeClase = new TreeSet<>();
        for (Asistencia a : todasAsistencias) {
            fechasDeClase.add(a.getFecha());
        }

        List alertasFaltas = new ArrayList<>();
        for (Alumno al : todosAlumnos) {
            Set<LocalDate> diasAsistidos = new HashSet<>();
            for (Asistencia a : asistenciaRepository.findByAlumno(al)) {
                diasAsistidos.add(a.getFecha());
            }

            List<String> diasFaltados = new ArrayList<>();
            for (LocalDate fecha : fechasDeClase) {
                if (!diasAsistidos.contains(fecha)) {
                    diasFaltados.add(fecha.toString());
                }
            }

            if (diasFaltados.size() >= 3) {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("numeroControl", al.getNumeroControl());
                alerta.put("nombreCompleto", al.getNombre() + " " + al.getApellidos());
                alerta.put("grupo", al.getGrupo().getNombreGrupo());
                alerta.put("totalFaltas", diasFaltados.size());
                alerta.put("fechasFaltadas", diasFaltados);
                alertasFaltas.add(alerta);
            }
        }

        long alumnosAsistieronHoy = asistenciasHoy.stream()
                .map(a -> a.getAlumno().getIdAlumno())
                .distinct()
                .count();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalAlumnosRegistrados", todosAlumnos.size());
        dashboard.put("asistieronHoy", alumnosAsistieronHoy);
        dashboard.put("totalAlertasFaltas", alertasFaltas.size());
        dashboard.put("alertasFaltas", alertasFaltas);
        dashboard.put("registrosHoy", asistenciasHoy);
        dashboard.put("listaAlumnos", todosAlumnos);

        return ResponseEntity.ok(dashboard);
    }
}