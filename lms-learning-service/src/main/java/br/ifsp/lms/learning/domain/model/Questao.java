package br.ifsp.lms.learning.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Questao {
    private Long id;
    private String enunciado;
    private List<Alternativa> alternativas = new ArrayList<>();
    private Set<String> tags = new HashSet<>();
}
