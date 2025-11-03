package br.ifsp.lms_api.dto.DisciplinaDto;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisciplinaUpdateDto {

    private Optional<String> nomeDisciplina = Optional.empty();
    private Optional<String> descricaoDisciplina = Optional.empty();
    private Optional<String> codigoDisciplina = Optional.empty();
    
}