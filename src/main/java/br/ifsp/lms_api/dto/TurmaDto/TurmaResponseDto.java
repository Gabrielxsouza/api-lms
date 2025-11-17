package br.ifsp.lms_api.dto.TurmaDto;

import br.ifsp.lms_api.dto.CursoDto.CursoParaTurmaResponseDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaParaTurmaResponseDto;
import br.ifsp.lms_api.dto.professorDto.ProfessorParaTurmaResponseDto;
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
    
    private CursoParaTurmaResponseDto curso;
    private ProfessorParaTurmaResponseDto professor;

    // --- CORREÇÃO AQUI ---
    // Trocamos 'DisciplinaResponseDto' por 'DisciplinaParaTurmaResponseDto'
    private DisciplinaParaTurmaResponseDto disciplina;
}