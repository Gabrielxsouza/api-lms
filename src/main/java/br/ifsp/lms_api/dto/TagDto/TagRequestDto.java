package br.ifsp.lms_api.dto.TagDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagRequestDto {
    @NotBlank(message = "O nome da tag é obrigatório")
    private String nome;
}