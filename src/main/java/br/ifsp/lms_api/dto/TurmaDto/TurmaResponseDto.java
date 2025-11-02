package br.ifsp.lms_api.dto.TurmaDto;

import java.util.List;

import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaResponseDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurmaResponseDto {

    private Long idTurma;

    private String nomeTurma;

    private String semestre;

    private DisciplinaResponseDto disciplina;

    private List<TopicosResponseDto> topicos;
}
