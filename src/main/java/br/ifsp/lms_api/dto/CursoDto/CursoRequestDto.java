package br.ifsp.lms_api.dto.CursoDto;

import java.util.ArrayList;
import java.util.List;

import br.ifsp.lms_api.dto.TurmaDto.TurmaParaCursoRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoRequestDto {
    @NotBlank(message = "O nome do curso é obrigatorio")
    private String nomeCurso;

    @NotBlank(message = "A descricao do curso é obrigatorio")
    private String descricaoCurso;

    @NotBlank(message = "O codigo do curso é obrigatorio")
    private String codigoCurso;

    @Valid
    // Mude o tipo da Lista para o NOVO DTO
    private List<TurmaParaCursoRequestDto> turmas = new ArrayList<>(); 
}