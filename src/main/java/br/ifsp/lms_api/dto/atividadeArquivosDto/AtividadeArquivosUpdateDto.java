package br.ifsp.lms_api.dto.atividadeArquivosDto;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesUpdateDto;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;





public class AtividadeArquivosUpdateDto extends AtividadesUpdateDto {
    private Optional<List<String>> arquivosPermitidos = Optional.empty();

    public AtividadeArquivosUpdateDto() {
        super();
    }

    public AtividadeArquivosUpdateDto(Optional<String> tituloAtividade, Optional<String> descricaoAtividade, Optional<LocalDate> dataInicioAtividade,
                                     Optional<LocalDate> dataFechamentoAtividade, Optional<Boolean> statusAtividade, Optional<List<String>> arquivosPermitidos) {
        super(tituloAtividade, descricaoAtividade, dataInicioAtividade, dataFechamentoAtividade, statusAtividade);
        this.arquivosPermitidos = arquivosPermitidos;
    }

    public Optional<List<String>> getArquivosPermitidos() {
        return arquivosPermitidos;
    }

    public void setArquivosPermitidos(Optional<List<String>> arquivosPermitidos) {
        this.arquivosPermitidos = arquivosPermitidos;
    }


}
