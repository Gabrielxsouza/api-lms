package br.ifsp.lms_api.dto.TagDto;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagUpdateDto {
    private Optional<String> nome = Optional.empty();
}