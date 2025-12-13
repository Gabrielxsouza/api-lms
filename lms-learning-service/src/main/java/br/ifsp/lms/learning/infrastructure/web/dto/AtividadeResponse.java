package br.ifsp.lms.learning.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AtividadeResponse {

    private Long idAtividade;
    private String tituloAtividade;
    private String descricaoAtividade; 
    private Long idTopico;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioAtividade;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFechamentoAtividade;

    private Boolean statusAtividade;

    @JsonProperty("tipoAtividade")
    private String tipoAtividade; 

    private Long duracaoQuestionario;
    private Integer numeroTentativas;
    private Long numeroMaximoCaracteres; 
    private java.util.List<String> arquivosPermitidos; 

    private List<Object> questoesQuestionario;
    private List<Object> tags;
}
