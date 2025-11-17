package br.ifsp.lms_api.dto.CursoDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoParaTurmaResponseDto {
    private Long idCurso;
    private String nomeCurso;
    private String codigoCurso;
}