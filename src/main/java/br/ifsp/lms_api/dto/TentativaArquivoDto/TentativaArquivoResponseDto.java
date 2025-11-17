package br.ifsp.lms_api.dto.TentativaArquivoDto; // Novo package

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TentativaArquivoResponseDto {

    private Long idTentativa;
    private LocalDateTime dataEnvio;
    private String feedBack;
    private Double nota;
    private Long idAluno;

    private String nomeArquivo;
    private String urlArquivo;
    private String tipoArquivo;

    private Long idAtividadeArquivo;

}