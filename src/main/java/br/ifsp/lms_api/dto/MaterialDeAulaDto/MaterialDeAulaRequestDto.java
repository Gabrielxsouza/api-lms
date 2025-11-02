package br.ifsp.lms_api.dto.MaterialDeAulaDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDeAulaRequestDto {
    private String nomeArquivo;

    private String urlArquivo;

    private String tipoArquivo;
}
