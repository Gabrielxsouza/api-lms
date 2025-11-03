package br.ifsp.lms_api.dto.alternativasDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlternativasRequestDto {


    @NotBlank
    @Size(min = 5, max = 500, message = "A alternativa deve conter entre 5 e 500 caracteres")
    private String alternativa;

    @NotNull(message = "É obrigatório indicar se a alternativa é correta")
    private Boolean alternativaCorreta;

    @NotNull(message = "O ID da Questão é obrigatório")
    private Long idQuestao;

}
