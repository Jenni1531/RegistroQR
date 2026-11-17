package com.registroqr.registro_qr.config;

import com.registroqr.registro_qr.model.*;
import com.registroqr.registro_qr.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepo,
                                GrupoRepository grupoRepo,
                                AlumnoRepository alumnoRepo) {
        return args -> {
            // 1. Crear grupo inicial si no existe
            Grupo grupo1A;
            if (grupoRepo.count() == 0) {
                grupo1A = new Grupo();
                grupo1A.setNombreGrupo("1A");
                grupo1A.setSemestre(1);
                grupo1A.setTurno("MATUTINO");
                grupo1A.setCarreraEspecialidad("Bachillerato General");
                grupo1A = grupoRepo.save(grupo1A);
            } else {
                grupo1A = grupoRepo.findAll().get(0);
            }

            // 2. Crear Administrador de prueba (Usuario: ADMIN01 / Pass: admin123)
            if (usuarioRepo.findByNumeroControl("ADMIN01").isEmpty()) {
                Usuario adminUser = new Usuario();
                adminUser.setNumeroControl("ADMIN01");
                adminUser.setPasswordHash("admin123");
                adminUser.setRol("ADMIN");
                usuarioRepo.save(adminUser);
            }

            // 3. Crear Alumno de prueba (Usuario: 2026001 / Pass: 2026001)
            if (usuarioRepo.findByNumeroControl("2026001").isEmpty()) {
                Usuario alumnoUser = new Usuario();
                alumnoUser.setNumeroControl("2026001");
                alumnoUser.setPasswordHash("2026001");
                alumnoUser.setRol("ALUMNO");
                alumnoUser = usuarioRepo.save(alumnoUser);

                Alumno alumno = new Alumno();
                alumno.setUsuario(alumnoUser);
                alumno.setGrupo(grupo1A);
                alumno.setNumeroControl("2026001");
                alumno.setNombre("Carlos");
                alumno.setApellidos("Méndez López");
                alumno.setCodigoQrToken("QR-2026001");
                alumnoRepo.save(alumno);
            }
        };
    }
}