package br.ifsp.lms.learning.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateArquivosRequest {
    private String tituloAtividade;
    private String descricaoAtividade;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioAtividade;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFechamentoAtividade;
    private Boolean statusAtividade;

    private List<String> arquivosPermitidos;

    private Long idTopico;
    private Set<String> tags;
}
