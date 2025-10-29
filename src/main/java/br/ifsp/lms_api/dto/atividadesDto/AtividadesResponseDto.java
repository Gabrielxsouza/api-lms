package br.ifsp.lms_api.dto.atividadesDto;

import java.time.LocalDate;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AtividadesResponseDto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAtividade;

    @NotBlank(message = "O título da atividade é obrigatório")
    @Size(min = 5, max = 100, message = "O título da atividade deve conter entre 5 e 100 caracteres")
    private String tituloAtividade;
  
    private String descricaoAtividade;

    @NotBlank(message = "A data de início da atividade é obrigatória")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioAtividade;

    @NotBlank(message = "A data de fechamento da atividade é obrigatória")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFechamentoAtividade;

    private Boolean statusAtividade;

}
