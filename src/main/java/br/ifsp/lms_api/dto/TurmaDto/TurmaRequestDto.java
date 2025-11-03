package br.ifsp.lms_api.dto.TurmaDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurmaRequestDto {

    @NotBlank(message = "O nome da turma é obrigatorio")
    private String nomeTurma;

    @NotBlank(message = "O semestre da turma é obrigatorio")
    private String semestre;

    @NotNull(message = "O ID da disciplina é obrigatório")
    private Long idDisciplina;

}
