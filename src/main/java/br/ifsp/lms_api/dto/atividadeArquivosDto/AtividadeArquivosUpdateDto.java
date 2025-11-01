package br.ifsp.lms_api.dto.atividadeArquivosDto;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesUpdateDto;
import java.util.Optional;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data; 
import lombok.EqualsAndHashCode; 
import lombok.NoArgsConstructor; 

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode(callSuper = true)
public class AtividadeArquivosUpdateDto extends AtividadesUpdateDto {
    
    private Optional<List<String>> arquivosPermitidos = Optional.empty();

}