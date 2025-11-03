package br.ifsp.lms_api.dto.TurmaDto;


import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaResponseDto;
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

}
