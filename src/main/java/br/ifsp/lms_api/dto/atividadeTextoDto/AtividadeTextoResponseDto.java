package br.ifsp.lms_api.dto.atividadeTextoDto;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;

public class AtividadeTextoResponseDto extends AtividadesResponseDto {
    private long numeroMaximoPalavras;



    public long getNumeroMaximoPalavras() {
        return numeroMaximoPalavras;
    }

}
