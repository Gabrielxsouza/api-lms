package br.ifsp.lms_api.dto.DisciplinaDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Este DTO é simples, SÓ para ser usado DENTRO de TurmaResponseDto
// Note que ele NÃO tem a List<TurmaResponseDto>
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisciplinaParaTurmaResponseDto {
    private Long idDisciplina;
    private String nomeDisciplina;
    private String codigoDisciplina;
}