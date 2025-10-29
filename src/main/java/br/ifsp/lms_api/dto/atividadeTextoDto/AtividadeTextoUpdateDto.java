package br.ifsp.lms_api.dto.atividadeTextoDto;


import br.ifsp.lms_api.dto.atividadesDto.AtividadesUpdateDto;

import java.time.LocalDate;

import java.util.Optional;

public class AtividadeTextoUpdateDto extends AtividadesUpdateDto{
    private Optional<Long> numeroMaximoPalavras = Optional.empty();

    public AtividadeTextoUpdateDto() {
        super();
    }

    public AtividadeTextoUpdateDto(Optional<String> tituloAtividade, Optional<String> descricaoAtividade, Optional<LocalDate> dataInicioAtividade,
            Optional<LocalDate> dataFechamentoAtividade, Optional<Boolean> statusAtividade, Optional<Long> numeroMaximoPalavras) {
        super(tituloAtividade, descricaoAtividade, dataInicioAtividade, dataFechamentoAtividade, statusAtividade);
        this.numeroMaximoPalavras = numeroMaximoPalavras;
    }

    public Optional<Long> getNumeroMaximoPalavras() {
        return numeroMaximoPalavras;
    }

    public void setNumeroMaximoPalavras(Optional<Long> numeroMaximoPalavras) {
        this.numeroMaximoPalavras = numeroMaximoPalavras;
    }


}
