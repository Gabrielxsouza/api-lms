package br.ifsp.lms_api.dto.atividadeArquivosDto;


import java.util.List;
import java.time.LocalDate;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;

public class AtividadeArquivosResponseDto extends AtividadesResponseDto {
    private List<String> arquivosPermitidos;

    public AtividadeArquivosResponseDto() {
        super();
    }

    public AtividadeArquivosResponseDto(Long idAtividade, String tituloAtividade, String descricaoAtividade, LocalDate dataInicioAtividade,
            LocalDate dataFechamentoAtividade, Boolean statusAtividade, List<String> arquivosPermitidos) {
        super(idAtividade, tituloAtividade, descricaoAtividade, dataInicioAtividade, dataFechamentoAtividade, statusAtividade);
        this.arquivosPermitidos = arquivosPermitidos;
    }


    public List<String> getArquivosPermitidos() {
        return arquivosPermitidos;
    }



}
