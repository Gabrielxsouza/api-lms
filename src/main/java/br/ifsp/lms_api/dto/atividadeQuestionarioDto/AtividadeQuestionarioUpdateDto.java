package br.ifsp.lms_api.dto.atividadeQuestionarioDto;


import java.util.Optional;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesUpdateDto;



public class AtividadeQuestionarioUpdateDto extends AtividadesUpdateDto {
    
    private Optional<Long> duracaoQuestionario = Optional.empty();
    private Optional<Integer> numeroTentativas = Optional.empty();

    public Optional<Long> getDuracaoQuestionario() {
        return duracaoQuestionario;
    }
    
    public void setDuracaoQuestionario(Optional<Long> duracaoQuestionario) {
        this.duracaoQuestionario = duracaoQuestionario;
    }
    
    public Optional<Integer> getNumeroTentativas() {
        return numeroTentativas;
    }
    
    public void setNumeroTentativas(Optional<Integer> numeroTentativas) {
        this.numeroTentativas = numeroTentativas;
    }
}
