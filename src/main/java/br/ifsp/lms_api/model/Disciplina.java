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
public class Disciplina {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDisciplina;

    @NotBlank(message = "O nome da disciplina é obrigatório")
    private String nomeDisciplina;

    @NotBlank(message = "O codigo da disciplina é obrigatório")
    private String codigoDisciplina;

    @NotBlank(message = "A descricao da disciplina é obrigatório")
    private String descricaoDisciplina;

    @OneToMany(mappedBy = "disciplina", 
    cascade = CascadeType.ALL, 
    orphanRemoval = true)
    @JsonManagedReference
    private List<Turma> turmas;
}
