package br.ifsp.lms_api.dto.atividadeArquivosDto;

import java.util.List;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesRequestDto;
import java.time.LocalDate;

public class AtividadeArquivosRequestDto extends AtividadesRequestDto {
    private List<String> arquivosPermitidos;

    public AtividadeArquivosRequestDto() {
        super();
    }

    public AtividadeArquivosRequestDto(String tituloAtividade,String descricaoAtividade, LocalDate dataInicioAtividade,LocalDate dataFechamentoAtividade,Boolean statusAtividade,List<String> arquivosPermitidos) {
        super(tituloAtividade, descricaoAtividade, dataInicioAtividade, dataFechamentoAtividade, statusAtividade);
        this.arquivosPermitidos = arquivosPermitidos;
    }



    public List<String> getArquivosPermitidos() {
        return arquivosPermitidos;
    }

    public void setArquivosPermitidos(List<String> arquivosPermitidos) {
        this.arquivosPermitidos = arquivosPermitidos;
    }

    


}
