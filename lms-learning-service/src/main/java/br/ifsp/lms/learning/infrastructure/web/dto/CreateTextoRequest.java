package br.ifsp.lms.learning.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTextoRequest {
    private String tituloAtividade;
    private String descricaoAtividade;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioAtividade;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFechamentoAtividade;
    private Boolean statusAtividade;

    private Long numeroMaximoCaracteres;

    private Long idTopico;
    private java.util.Set<String> tags;
}
