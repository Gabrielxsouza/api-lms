package br.ifsp.lms_api.dto.atividadeArquivosDto;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesUpdateDto;

import java.util.Optional;
import java.util.List;





public class AtividadeArquivosUpdateDto extends AtividadesUpdateDto {
    private Optional<List<String>> arquivosPermitidos = Optional.empty();


    public Optional<List<String>> getArquivosPermitidos() {
        return arquivosPermitidos;
    }

    public void setArquivosPermitidos(Optional<List<String>> arquivosPermitidos) {
        this.arquivosPermitidos = arquivosPermitidos;
    }


}
