package br.ifsp.lms_api.dto.atividadeTextoDto;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesUpdateDto;
import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.EqualsAndHashCode; 
import lombok.NoArgsConstructor;
import java.util.Optional;

@Data 
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AtividadeTextoUpdateDto extends AtividadesUpdateDto {

    private Optional<Long> numeroMaximoCaracteres = Optional.empty();

}