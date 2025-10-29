package br.ifsp.lms_api.dto.atividadeTextoDto;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;

import java.time.LocalDate;

public class AtividadeTextoResponseDto extends AtividadesResponseDto {
    private long numeroMaximoPalavras;

    public AtividadeTextoResponseDto() {
        super();
    }

    public AtividadeTextoResponseDto(Long idAtividade, String tituloAtividade, String descricaoAtividade, LocalDate dataInicioAtividade,
            LocalDate dataFechamentoAtividade, Boolean statusAtividade, long numeroMaximoPalavras) {
        super(idAtividade, tituloAtividade, descricaoAtividade, dataInicioAtividade, dataFechamentoAtividade, statusAtividade);
        this.numeroMaximoPalavras = numeroMaximoPalavras;
    }

    public long getNumeroMaximoPalavras() {
        return numeroMaximoPalavras;
    }

}
