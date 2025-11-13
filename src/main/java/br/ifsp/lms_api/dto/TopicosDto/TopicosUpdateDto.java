package br.ifsp.lms_api.dto.TopicosDto;

import java.util.Optional;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicosUpdateDto {
    private Optional<String> conteudoHtml = Optional.empty();
    private Optional<String> tituloTopico = Optional.empty();
    private Optional<List<Long>> tagIds = Optional.empty();
}
