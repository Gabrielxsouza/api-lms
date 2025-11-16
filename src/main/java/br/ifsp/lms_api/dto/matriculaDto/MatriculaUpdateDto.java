package br.ifsp.lms_api.dto.matriculaDto;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaUpdateDto {

    private Optional<String> statusMatricula = Optional.empty();

}
