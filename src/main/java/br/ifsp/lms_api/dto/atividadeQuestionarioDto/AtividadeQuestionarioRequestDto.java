package br.ifsp.lms_api.dto.atividadeQuestionarioDto;


import java.util.List;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesRequestDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesRequestDto;


public class AtividadeQuestionarioRequestDto extends AtividadesRequestDto {
    
    private long duracaoQuestionario;

    private int numeroTentativas;

    private List<QuestoesRequestDto> questoesQuestionario;

    public long getDuracaoQuestionario() {
        return duracaoQuestionario;
    }

    public void setDuracaoQuestionario(long duracaoQuestionario) {
        this.duracaoQuestionario = duracaoQuestionario;
    }

    public int getNumeroTentativas() {
        return numeroTentativas;
    }

    public void setNumeroTentativas(int numeroTentativas) {
        this.numeroTentativas = numeroTentativas;
    }

    public List<QuestoesRequestDto> getQuestoesQuestionario() {
        return questoesQuestionario;
    }

    public void setQuestoesQuestionario(List<QuestoesRequestDto> questoesQuestionario) {
        this.questoesQuestionario = questoesQuestionario;
    }
}
