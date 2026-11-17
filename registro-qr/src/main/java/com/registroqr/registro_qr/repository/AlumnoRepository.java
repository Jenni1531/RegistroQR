package com.registroqr.registro_qr.repository;

import com.registroqr.registro_qr.model.Alumno;
import com.registroqr.registro_qr.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {
    Optional<Alumno> findByCodigoQrToken(String codigoQrToken);
    Optional<Alumno> findByUsuario(Usuario usuario);
    Optional<Alumno> findByNumeroControl(String numeroControl);
}