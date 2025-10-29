package br.ifsp.lms_api.dto.atividadeArquivosDto;

import java.util.List;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesRequestDto;


public class AtividadeArquivosRequestDto extends AtividadesRequestDto {
    private List<String> arquivosPermitidos;





    public List<String> getArquivosPermitidos() {
        return arquivosPermitidos;
    }

    public void setArquivosPermitidos(List<String> arquivosPermitidos) {
        this.arquivosPermitidos = arquivosPermitidos;
    }

    


}
