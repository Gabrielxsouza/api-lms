package br.ifsp.lms_api.dto.matriculaDto;

import br.ifsp.lms_api.model.Status;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaRequestDto {

    @NotNull(message = "O ID do aluno é obrigatório")
    private Long idAluno;

    @NotNull(message = "O ID da turma é obrigatório")
    private Long idTurma;

    // Opcional: Se você quiser que o front-end defina o status inicial.
    // Se não, remova isto e defina o status como PENDENTE no seu Service.
    @NotNull(message = "O status da matrícula é obrigatório")
    private Status statusMatricula;
}
