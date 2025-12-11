package br.ifsp.lms.learning.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeQuestionario extends Atividade {
    private Long duracaoMinutes;
    private Integer tentativasPermitidas;
    @Builder.Default
    private List<Questao> questoes = new ArrayList<>();
}
