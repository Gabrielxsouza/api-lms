package br.ifsp.lms_api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Curso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCurso;

    @NotBlank(message = "O nome do curso é obrigatório")
    private String nomeCurso;
    
    @NotBlank(message = "O codigo do curso é obrigatório")
    private String codigoCurso;

    @NotBlank(message = "A descricao do curso é obrigatório")
    private String descricaoCurso;

    @OneToMany(mappedBy = "curso", // IMPORTANTE: Assumindo que Turma terá um campo "private Curso curso;"
    cascade = CascadeType.ALL, 
    orphanRemoval = true)
    @JsonManagedReference
    private List<Turma> turmas;
}