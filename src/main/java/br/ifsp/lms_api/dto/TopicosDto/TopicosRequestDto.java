package br.ifsp.lms_api.dto.TopicosDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicosRequestDto {
    @NotBlank(message = "O titulo do topico é obrigatorio")
    private String tituloTopico;

    private String conteudoHtml;

    @NotNull(message = "O id da turma é obrigatorio")
    private long idTurma;

}
