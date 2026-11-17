package com.registroqr.registro_qr.model;

import jakarta.persistence.*;

@Entity
@Table(name = "grupo")
public class Grupo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo")
    private Integer idGrupo;

    @Column(name = "nombre_grupo", nullable = false, length = 50)
    private String nombreGrupo;

    @Column(nullable = false)
    private Integer semestre;

    @Column(length = 20)
    private String turno = "MATUTINO";

    @Column(name = "carrera_especialidad", length = 100)
    private String carreraEspecialidad;

    // Getters y Setters
    public Integer getIdGrupo() { return idGrupo; }
    public void setIdGrupo(Integer idGrupo) { this.idGrupo = idGrupo; }
    public String getNombreGrupo() { return nombreGrupo; }
    public void setNombreGrupo(String nombreGrupo) { this.nombreGrupo = nombreGrupo; }
    public Integer getSemestre() { return semestre; }
    public void setSemestre(Integer semestre) { this.semestre = semestre; }
    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
    public String getCarreraEspecialidad() { return carreraEspecialidad; }
    public void setCarreraEspecialidad(String carreraEspecialidad) { this.carreraEspecialidad = carreraEspecialidad; }
}