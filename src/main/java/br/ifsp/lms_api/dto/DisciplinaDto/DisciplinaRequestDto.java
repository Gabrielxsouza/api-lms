package br.ifsp.lms_api.dto.DisciplinaDto;

import java.util.List;

import br.ifsp.lms_api.dto.TurmaDto.TurmaRequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisciplinaRequestDto {
    @NotBlank(message = "O nome da disciplina é obrigatorio")
    private String nomeDisciplina;

    @NotBlank(message = "A descricao da disciplina é obrigatorio")
    private String descricaoDisciplina;

    @NotBlank(message = "O codigo da disciplina é obrigatorio")
    private String codigoDisciplina;

    private List<TurmaRequestDto> turmas;
}
