package br.ifsp.lms_api.dto.DisciplinaDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisciplinaResponseDto {
    private Long idDisciplina;

    private String nomeDisciplina;

    private String descricaoDisciplina;

    private String codigoDisciplina;
}
