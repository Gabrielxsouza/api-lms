package br.ifsp.lms_api.dto.CursoDto;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoUpdateDto {

    private Optional<String> nomeCurso = Optional.empty();
    private Optional<String> descricaoCurso = Optional.empty();
    private Optional<String> codigoCurso = Optional.empty();
    
}