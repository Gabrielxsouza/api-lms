package br.ifsp.lms_api.dto.questoesDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestoesUpdateDto {
    private Optional<String> enunciado = Optional.empty();
}
