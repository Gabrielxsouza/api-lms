package br.ifsp.lms_api.dto.atividadesDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtividadesRequestDto {


    @NotBlank(message = "O título da atividade é obrigatório")
    @Size(min = 5, max = 100, message = "O título da atividade deve conter entre 5 e 100 caracteres")
    private String tituloAtividade;
  
    private String descricaoAtividade;

    @NotNull(message = "A data de início da atividade é obrigatória")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioAtividade;

    @NotNull(message = "A data de fechamento da atividade é obrigatória")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFechamentoAtividade;

    @NotNull(message = "O status da atividade (aberto ou fechado) é obrigatório")
    private Boolean statusAtividade;

    private List<Long> tagIds = new ArrayList<>();
}
