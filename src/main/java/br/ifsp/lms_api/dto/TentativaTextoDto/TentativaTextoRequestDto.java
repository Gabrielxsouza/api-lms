package br.ifsp.lms_api.dto.TentativaTextoDto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TentativaTextoRequestDto {

    private LocalDateTime dataEnvio;

    @NotBlank(message = "O texto da resposta não pode estar vazio")
    private String textoResposta;

    private String feedBack;
    private Double nota;
    private Long idAtividadeTexto;
    private Long idAluno;
}
