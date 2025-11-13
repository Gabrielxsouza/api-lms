package br.ifsp.lms_api.dto.questoesDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestoesUpdateDto {
    private Optional<String> enunciado = Optional.empty();
    private Optional<List<Long>> tagIds = Optional.empty();
}
