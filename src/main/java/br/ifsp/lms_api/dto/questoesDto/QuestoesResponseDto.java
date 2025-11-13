package br.ifsp.lms_api.dto.questoesDto;

import java.util.List;

import br.ifsp.lms_api.dto.TagDto.TagResponseDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestoesResponseDto {
   
    private Long idQuestao;

    private String enunciado;

    private List<AlternativasResponseDto> alternativas;

    private List<TagResponseDto> tags;
}
