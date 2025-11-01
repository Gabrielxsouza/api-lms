package br.ifsp.lms_api.dto.atividadeArquivosDto;

import java.util.List;
import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;
import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.EqualsAndHashCode; 
import lombok.NoArgsConstructor; 

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode(callSuper = true)
public class AtividadeArquivosResponseDto extends AtividadesResponseDto {
    
    private List<String> arquivosPermitidos;

}