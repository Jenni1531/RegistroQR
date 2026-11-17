package com.registroqr.registro_qr.controller;

import com.registroqr.registro_qr.model.Alumno;
import com.registroqr.registro_qr.model.Usuario;
import com.registroqr.registro_qr.repository.AlumnoRepository;
import com.registroqr.registro_qr.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String numeroControl = credenciales.get("numeroControl");
        String password = credenciales.get("password");

        if (numeroControl == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Ingresa usuario y contraseña"));
        }

        Optional<Usuario> userOpt = usuarioRepository.findByNumeroControl(numeroControl.trim());
        if (userOpt.isEmpty() || !userOpt.get().getPasswordHash().equals(password)) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Número de control o contraseña incorrectos"));
        }

        Usuario usuario = userOpt.get();
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("idUsuario", usuario.getIdUsuario());
        respuesta.put("numeroControl", usuario.getNumeroControl());
        respuesta.put("rol", usuario.getRol());

        // Si es ALUMNO, devolvemos de una vez sus datos completos y su QR permanente
        if ("ALUMNO".equals(usuario.getRol())) {
            Optional<Alumno> alumnoOpt = alumnoRepository.findByUsuario(usuario);
            if (alumnoOpt.isPresent()) {
                Alumno al = alumnoOpt.get();
                respuesta.put("idAlumno", al.getIdAlumno());
                respuesta.put("nombreCompleto", al.getNombre() + " " + al.getApellidos());
                respuesta.put("grupo", al.getGrupo().getNombreGrupo() + " - " + al.getGrupo().getTurno());
                respuesta.put("codigoQrToken", al.getCodigoQrToken());
            }
        }

        return ResponseEntity.ok(respuesta);
    }

    // Cambiar contraseña opcional del alumno
    @PutMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> datos) {
        String numeroControl = datos.get("numeroControl");
        String nuevaPassword = datos.get("nuevaPassword");

        Optional<Usuario> userOpt = usuarioRepository.findByNumeroControl(numeroControl);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("mensaje", "Usuario no encontrado"));
        }

        Usuario usuario = userOpt.get();
        usuario.setPasswordHash(nuevaPassword);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente"));
    }
}