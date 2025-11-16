package br.ifsp.lms_api.model;

// Import adicionado
import com.fasterxml.jackson.annotation.JsonBackReference; 

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMatricula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario") // Chave estrangeira para Aluno (que é um Usuario)
    @JsonBackReference // <-- ADICIONADO (Evita loop Aluno -> Matricula -> Aluno)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTurma") // Chave estrangeira para Turma
    @JsonBackReference // <-- ADICIONADO (Evita loop Turma -> Matricula -> Turma)
    private Turma turma;

    @NotNull(message = "O status da matrícula é obrigatório")
    @Enumerated(EnumType.STRING)
    private Status statusMatricula;
}