package br.ifsp.lms_api.dto.MaterialDeAulaDto;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDeAulaUpdateDto {
    private Optional<String> nomeArquivo;
    private Optional<String> urlArquivo;
    private Optional<String> tipoArquivo;

}
