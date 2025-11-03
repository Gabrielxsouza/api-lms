package br.ifsp.lms_api.dto.alternativasDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlternativasResponseDto {

    private Long idAlternativa;
    private String alternativa;
    private Boolean alternativaCorreta;

}
