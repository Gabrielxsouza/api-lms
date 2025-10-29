package br.ifsp.lms_api.dto.atividadesDto;

import java.time.LocalDate;
import java.util.Optional;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AtividadesUpdateDto {
    private Optional<String> tituloAtividade = Optional.empty();
    private Optional<String> descricaoAtividade = Optional.empty();
    private Optional<LocalDate> dataInicioAtividade = Optional.empty();
    private Optional<LocalDate> dataFechamentoAtividade = Optional.empty();
    private Optional<Boolean> statusAtividade = Optional.empty();
}
