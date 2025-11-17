package br.ifsp.lms_api.dto.CursoDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// Não precisamos mais importar a lista de turmas

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

}