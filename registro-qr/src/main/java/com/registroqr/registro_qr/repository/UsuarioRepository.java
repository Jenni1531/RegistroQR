package com.registroqr.registro_qr.repository;

import com.registroqr.registro_qr.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByNumeroControl(String numeroControl);
}