package br.ifsp.lms_api.dto.alunoDto;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlunoUpdateDto {

    private Optional<String> nome = Optional.empty();

    private Optional<String> email = Optional.empty();

    private Optional<String> senha = Optional.empty();

    private Optional<String> ra = Optional.empty();
}