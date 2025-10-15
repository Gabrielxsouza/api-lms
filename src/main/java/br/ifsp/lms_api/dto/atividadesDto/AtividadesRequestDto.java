package br.ifsp.lms_api.dto.atividadesDto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtividadesRequestDto {


    @NotBlank(message = "O título da atividade é obrigatório")
    @Size(min = 5, max = 100, message = "O título da atividade deve conter entre 5 e 100 caracteres")
    private String tituloAtividade;
  
    private String descricaoAtividade;

    @NotNull(message = "A data de início da atividade é obrigatória")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioAtividade;

    @NotNull(message = "A data de fechamento da atividade é obrigatória")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFechamentoAtividade;

    private Boolean statusAtividade;

}
