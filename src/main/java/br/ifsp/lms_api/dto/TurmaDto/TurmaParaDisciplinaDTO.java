package br.ifsp.lms_api.dto.TurmaDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurmaParaDisciplinaDTO {
    
    @NotBlank(message = "O nome da turma é obrigatorio")
    private String nomeTurma;

    @NotBlank(message = "O semestre da turma é obrigatorio")
    private String semestre;
}