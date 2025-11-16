package br.ifsp.lms_api.dto.tentativaQuestionarioDto;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TentativaQuestionarioUpdateDto {
    private Optional<Double> nota;
    private Optional<Integer> numeroDaTentativa;
    private Optional<LocalDateTime> dataEnvio;
    private Optional<Long> idQuestionario;
    private Optional<Long> idAluno;
}
