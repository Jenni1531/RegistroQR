package com.registroqr.registro_qr.repository;

import com.registroqr.registro_qr.model.Alumno;
import com.registroqr.registro_qr.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {
    long countByAlumnoAndFecha(Alumno alumno, LocalDate fecha);
    List<Asistencia> findByFechaOrderByHoraEntradaDesc(LocalDate fecha);
    List<Asistencia> findByAlumno(Alumno alumno);
    List<Asistencia> findAllByOrderByFechaDescHoraEntradaDesc();
}