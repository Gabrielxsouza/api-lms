package br.ifsp.lms_api.dto.atividadeTextoDto;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesRequestDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor; 
import lombok.Data;
import lombok.EqualsAndHashCode; 
import lombok.NoArgsConstructor; 

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode(callSuper = true) 
public class AtividadeTextoRequestDto extends AtividadesRequestDto {
    
    @NotNull(message = "O número máximo de caracteres é obrigatório")
    @PositiveOrZero(message = "O número máximo de caracteres deve ser 0 ou maior")
    private Long numeroMaximoCaracteres; 

}