package br.ifsp.lms_api.dto.atividadeArquivosDto;

import java.util.List;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesRequestDto;
import jakarta.validation.constraints.NotNull; 
import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.EqualsAndHashCode; 
import lombok.NoArgsConstructor; 

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode(callSuper = true) // off importante para herança com @Data
public class AtividadeArquivosRequestDto extends AtividadesRequestDto {

    @NotNull(message = "A lista de arquivos permitidos é obrigatória (pode ser vazia)")
    private List<String> arquivosPermitidos;

}