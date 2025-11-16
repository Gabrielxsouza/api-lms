package br.ifsp.lms_api.dto.matriculaDto;

import br.ifsp.lms_api.model.Matricula;
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

    // gemini mandou fazer esse construtor senao o create nao ia funcionar direito
    public MatriculaResponseDto(Matricula matricula) {
        this.idMatricula = matricula.getIdMatricula();
        this.statusMatricula = matricula.getStatusMatricula();

        if (matricula.getAluno() != null) {
            this.idAluno = matricula.getAluno().getIdUsuario();
            this.nomeAluno = matricula.getAluno().getNome();
            this.raAluno = matricula.getAluno().getRa();
        }

        if (matricula.getTurma() != null) {
            this.idTurma = matricula.getTurma().getIdTurma();
            this.nomeTurma = matricula.getTurma().getNomeTurma();
            this.semestreTurma = matricula.getTurma().getSemestre();
        }
    }

}
