package br.ifsp.lms_api.dto.tentativaQuestionarioDto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TentativaQuestionarioRequestDto {
    
    private Double nota;
    private int numeroDaTentativa;
    private List<Long> respostas;

    private LocalDateTime dataEnvio;

    @NotNull(message = "O id do questionario é obrigatorio")
    private Long idQuestionario;

    private Long idAluno;
}
