package br.ifsp.lms_api.dto.atividadeTextoDto;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesRequestDto;


public class AtividadeTextoRequestDto extends AtividadesRequestDto{
    private long numeroMaximoPalavras;


    

    public long getNumeroMaximoPalavras() {
        return numeroMaximoPalavras;
    }

    public void setNumeroMaximoPalavras(long numeroMaximoPalavras) {
        this.numeroMaximoPalavras = numeroMaximoPalavras;
    }
}
