package br.ifsp.lms_api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTurma;

    @NotBlank(message = "O nome da turma é obrigatorio")
    private String nomeTurma;

    @Column(length = 20)
    private String semestre;

    // --- RELACIONAMENTOS PAIS (Turma "pertence a") ---
    // Usam @JsonBackReference para evitar loop na serialização

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDisciplina")
    @JsonBackReference // <-- CORRIGIDO (Era @JsonManagedReference)
    private Disciplina disciplina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCurso")
    @JsonBackReference // <-- ADICIONADO
    private Curso curso;


    // --- RELACIONAMENTOS FILHOS (Turma "possui") ---
    // Usam @JsonManagedReference para mostrar a lista no JSON da Turma

    @JsonManagedReference
    @OneToMany(
        mappedBy = "turma",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<Topicos> topicos;

    @JsonManagedReference // <-- CORRIGIDO (Era @JsonBackReference)
    @OneToMany(
        mappedBy = "turma",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Matricula> matriculas;
}