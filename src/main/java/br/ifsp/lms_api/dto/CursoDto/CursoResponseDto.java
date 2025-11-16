package br.ifsp.lms_api.dto.CursoDto;

import java.util.List; 

import com.fasterxml.jackson.annotation.JsonManagedReference;

import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoResponseDto {
    private Long idCurso;
    private String nomeCurso;
    private String descricaoCurso;
    private String codigoCurso;
    
    @JsonManagedReference
    private List<TurmaResponseDto> turmas; 
}