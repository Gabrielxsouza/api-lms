package br.ifsp.lms_api.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AtividadeQuestionario extends Atividade {

    private long duracaoQuestionario;

    private int numeroTentativas;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }) 
    @JoinTable(
        name = "questionario_questoes", 
        joinColumns = @JoinColumn(name = "id_questionario"), // esta entidade (AtividadeQuestionario)
        inverseJoinColumns = @JoinColumn(name = "id_questao") // outra entidade (Questoes)
    )
    @JsonManagedReference
    private List<Questoes> questoes = new ArrayList<>();

}