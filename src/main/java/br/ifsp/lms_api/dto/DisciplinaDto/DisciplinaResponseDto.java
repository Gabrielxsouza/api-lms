package br.ifsp.lms_api.dto.DisciplinaDto;

import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto; 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisciplinaResponseDto {
    private Long idDisciplina;

    private String nomeDisciplina;

    private String descricaoDisciplina;

    private String codigoDisciplina;
    
    @JsonManagedReference
    private List<TurmaResponseDto> turmas; 
}