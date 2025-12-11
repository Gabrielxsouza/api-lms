package br.ifsp.lms.learning.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Atividade {
    private Long id;
    private String titulo;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFechamento;
    private Boolean status;
    private Long topicoId;
    @Builder.Default
    private Set<String> tags = new HashSet<>();
}
