package br.ifsp.lms_api.dto.tentativaQuestionarioDto;

import java.time.LocalDateTime;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TentativaQuestionarioResponseDto {

    private Long idTentativaQuestionario;
    private Double nota;
    private int numeroDaTentativa;
    private List<Long> respostas;

    private LocalDateTime dataEnvio;

    private Long idQuestionario;

    private Long idAluno;
}
