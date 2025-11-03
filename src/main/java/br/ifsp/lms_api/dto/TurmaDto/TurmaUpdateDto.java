package br.ifsp.lms_api.dto.TurmaDto;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurmaUpdateDto {
    private Optional<String> semestre = Optional.empty();
    private Optional<String> nomeTurma = Optional.empty();
    private Optional<Long> idDisciplina = Optional.empty(); // acredito que nem pode mudar isso
    private Optional<Long> idTurma = Optional.empty(); // acredito que nem pode mudar isso
}
