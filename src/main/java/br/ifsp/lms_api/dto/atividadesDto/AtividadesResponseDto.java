package br.ifsp.lms_api.dto.atividadesDto;

import java.time.LocalDate;
// Removidas importações de validation e persistence
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data; // Ou @Getter @Setter
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AtividadesResponseDto {

    private Long idAtividade; 

    private String tituloAtividade;
 
    private String descricaoAtividade;

    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioAtividade;

    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFechamentoAtividade;

    private Boolean statusAtividade;
}