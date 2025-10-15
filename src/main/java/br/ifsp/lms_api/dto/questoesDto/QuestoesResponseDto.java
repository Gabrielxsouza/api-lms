package br.ifsp.lms_api.dto.questoesDto;

import java.util.List;

import br.ifsp.lms_api.dto.alternativasDto.AlternativasResponseDto;
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
public class QuestoesResponseDto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idQuestao;

    @NotBlank
    @Size (min = 5, max = 100, message = "O enunciado da questão deve conter entre 5 e 100 caracteres")
    private String enunciado;

    private List<AlternativasResponseDto> alternativas;
}
