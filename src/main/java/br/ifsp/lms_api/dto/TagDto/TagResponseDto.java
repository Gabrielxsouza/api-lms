package br.ifsp.lms_api.dto.TagDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagResponseDto {
    private Long idTag;
    private String nome;
}