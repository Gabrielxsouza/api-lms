package br.ifsp.lms_api.dto.atividadesDto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import br.ifsp.lms_api.dto.TagDto.TagResponseDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipoAtividade", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AtividadeTextoResponseDto.class, name = "TEXTO"),
        @JsonSubTypes.Type(value = AtividadeArquivosResponseDto.class, name = "ARQUIVOS"),
        @JsonSubTypes.Type(value = AtividadeQuestionarioResponseDto.class, name = "QUESTIONARIO")
})
public class AtividadesResponseDto {

    private Long idAtividade;

    private String tituloAtividade;

    private String descricaoAtividade;

    private Long idTopico;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioAtividade;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFechamentoAtividade;

    private Boolean statusAtividade;

    private List<String> tags;
}
