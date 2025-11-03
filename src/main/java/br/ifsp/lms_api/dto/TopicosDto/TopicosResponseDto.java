package br.ifsp.lms_api.dto.TopicosDto;

import java.util.List;

import br.ifsp.lms_api.dto.MaterialDeAulaDto.MaterialDeAulaResponseDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicosResponseDto {
    private Long idTopico;

    private String tituloTopico;

    private String conteudoHtml;

    private TurmaResponseDto turma;
 
    private List<MaterialDeAulaResponseDto> materiaisDeAula;

}
