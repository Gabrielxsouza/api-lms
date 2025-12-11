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

    // Must match Monolith AtividadesResponseDto + AtividadeQuestionarioResponseDto

    // Base fields
    private Long idAtividade;
    private String tituloAtividade;
    private String descricaoAtividade; // Monolith seems to have this in base
    private Long idTopico;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioAtividade;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFechamentoAtividade;

    private Boolean statusAtividade;

    // Polymorphic field discriminator
    @JsonProperty("tipoAtividade")
    private String tipoAtividade; // "QUESTIONARIO"

    // Subclass fields (AtividadeQuestionarioResponseDto)
    private Long duracaoQuestionario;
    private Integer numeroTentativas; // AtividadeQuestionario
    private Long numeroMaximoCaracteres; // AtividadeTexto
    private java.util.List<String> arquivosPermitidos; // AtividadeArquivos

    // Nullable/Empty for now unless we implement Question/Tag mapping fully
    private List<Object> questoesQuestionario;
    private List<Object> tags;
}
