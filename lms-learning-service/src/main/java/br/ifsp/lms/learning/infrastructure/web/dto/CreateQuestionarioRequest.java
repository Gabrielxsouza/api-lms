package br.ifsp.lms.learning.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CreateQuestionarioRequest {

    // Matched with Monolith's AtividadesRequestDto
    private String tituloAtividade;
    private String descricaoAtividade;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioAtividade;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFechamentoAtividade;

    private Boolean statusAtividade;

    // Additional fields from Monolith structure if needed, or inferred from context
    // The Monolith DTO doesn't send 'topicoId' in the body?
    // Wait, AtividadeController in Monolith takes DTO... Let's check relation.
    // The Monolith AtividadeQuestionarioRequestDto extends AtividadesRequestDto.

    // Matched with Monolith's AtividadeQuestionarioRequestDto
    private Long duracaoQuestionario;
    private Integer numeroTentativas;

    // Handling Topic/Course context might be tricky if not in DTO.
    // In Monolith, it likely associates via URL or Session?
    // Looking at Monolith Controller: create takes DTO.
    // DTO doesn't seem to have topicoId.
    // It seems missing in my viewing.

    // BUT, for the purpose of THIS integration test, let's match the fields.
}
