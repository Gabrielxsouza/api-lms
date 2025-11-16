package br.ifsp.lms_api.dto.TentativaTextoDto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TentativaTextoResponseDto {
    private Long idTentativa;
    private LocalDateTime dataEnvio;
    private String textoResposta;
    private String feedBack;
    private Double nota;
    private Long idAtividadeTexto;
    private Long idAluno;
}
