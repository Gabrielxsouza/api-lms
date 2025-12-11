package br.ifsp.lms.learning.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeArquivos extends Atividade {
    @Builder.Default
    private List<String> arquivosPermitidos = new ArrayList<>();
}
