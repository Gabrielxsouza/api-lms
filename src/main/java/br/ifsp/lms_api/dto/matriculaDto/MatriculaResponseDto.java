package br.ifsp.lms_api.dto.matriculaDto;

import br.ifsp.lms_api.model.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatriculaResponseDto {

    private Long idMatricula;
    private Status statusMatricula;

    // Dados do Aluno
    private Long idAluno;
    private String nomeAluno;
    private String raAluno;

    // Dados da Turma
    private Long idTurma;
    private String nomeTurma;
    private String semestreTurma;

}
