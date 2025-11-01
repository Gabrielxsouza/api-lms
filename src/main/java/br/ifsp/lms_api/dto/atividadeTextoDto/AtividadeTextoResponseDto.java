package br.ifsp.lms_api.dto.atividadeTextoDto;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;
import lombok.AllArgsConstructor; // Importado
import lombok.Data; // Importado
import lombok.EqualsAndHashCode; // Importado
import lombok.NoArgsConstructor; // Importado

@Data // Adicionado
@NoArgsConstructor // Adicionado
@AllArgsConstructor // Adicionado
@EqualsAndHashCode(callSuper = true)
public class AtividadeTextoResponseDto extends AtividadesResponseDto {

    private long numeroMaximoCaracteres; 

}