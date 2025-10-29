package br.ifsp.lms_api.dto.atividadeArquivosDto;


import java.util.List;


import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;

public class AtividadeArquivosResponseDto extends AtividadesResponseDto {
    private List<String> arquivosPermitidos;




    public List<String> getArquivosPermitidos() {
        return arquivosPermitidos;
    }



}
