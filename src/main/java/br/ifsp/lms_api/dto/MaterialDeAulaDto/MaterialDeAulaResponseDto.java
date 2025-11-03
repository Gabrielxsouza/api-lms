package br.ifsp.lms_api.dto.MaterialDeAulaDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialDeAulaResponseDto {
    private Long idMaterialDeAula;
    
    private String nomeArquivo;

    private String urlArquivo;

    private String tipoArquivo;
}
