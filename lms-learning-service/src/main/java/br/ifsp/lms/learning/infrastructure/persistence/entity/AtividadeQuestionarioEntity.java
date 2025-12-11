package br.ifsp.lms.learning.infrastructure.persistence.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("QUESTIONARIO")
public class AtividadeQuestionarioEntity extends AtividadeEntity {

    private Long duracaoQuestionario;
    private Integer numeroTentativas;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "questionario_questoes_ref", joinColumns = @JoinColumn(name = "id_questionario"), inverseJoinColumns = @JoinColumn(name = "id_questao"))
    private List<QuestaoEntity> questoes = new ArrayList<>();
}
