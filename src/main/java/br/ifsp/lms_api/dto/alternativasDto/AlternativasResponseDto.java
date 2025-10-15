package br.ifsp.lms_api.dto.alternativasDto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlternativasResponseDto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAlternativa;

    @NotBlank
    @Size(min = 5, max = 500, message = "A alternativa deve conter entre 5 e 500 caracteres")
    private String alternativa;

    @NotBlank
    private Boolean alternativaCorreta;

}
