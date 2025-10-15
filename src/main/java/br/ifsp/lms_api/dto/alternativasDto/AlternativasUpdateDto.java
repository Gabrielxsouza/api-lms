package br.ifsp.lms_api.dto.alternativasDto;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlternativasUpdateDto {
    private Optional<String> alternativa = Optional.empty();
    private Optional<Boolean> alternativaCorreta = Optional.empty();
}
