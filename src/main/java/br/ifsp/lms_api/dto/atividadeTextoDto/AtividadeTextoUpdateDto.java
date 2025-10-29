package br.ifsp.lms_api.dto.atividadeTextoDto;


import br.ifsp.lms_api.dto.atividadesDto.AtividadesUpdateDto;

import java.util.Optional;

public class AtividadeTextoUpdateDto extends AtividadesUpdateDto{
    private Optional<Long> numeroMaximoPalavras = Optional.empty();


    public Optional<Long> getNumeroMaximoPalavras() {
        return numeroMaximoPalavras;
    }

    public void setNumeroMaximoPalavras(Optional<Long> numeroMaximoPalavras) {
        this.numeroMaximoPalavras = numeroMaximoPalavras;
    }


}
