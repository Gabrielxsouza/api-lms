package br.ifsp.lms_api.dto.atividadeTextoDto;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AtividadeTextoResponseDto extends AtividadesResponseDto {

    private long numeroMaximoCaracteres; 

}