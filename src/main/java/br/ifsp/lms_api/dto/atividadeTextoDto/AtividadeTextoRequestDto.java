package br.ifsp.lms_api.dto.atividadeTextoDto;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesRequestDto;
import java.time.LocalDate;


public class AtividadeTextoRequestDto extends AtividadesRequestDto{
    private long numeroMaximoPalavras;

    public AtividadeTextoRequestDto() {
        super();
    }

    public AtividadeTextoRequestDto(String tituloAtividade, String descricaoAtividade, LocalDate dataInicioAtividade,
            LocalDate dataFechamentoAtividade, Boolean statusAtividade, long numeroMaximoPalavras) {
        super(tituloAtividade, descricaoAtividade, dataInicioAtividade, dataFechamentoAtividade, statusAtividade);
        this.numeroMaximoPalavras = numeroMaximoPalavras;
    }

    

    public long getNumeroMaximoPalavras() {
        return numeroMaximoPalavras;
    }

    public void setNumeroMaximoPalavras(long numeroMaximoPalavras) {
        this.numeroMaximoPalavras = numeroMaximoPalavras;
    }
}
