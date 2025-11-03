package br.ifsp.lms_api.dto.atividadeQuestionarioDto;

import java.util.List;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class AtividadeQuestionarioResponseDto extends AtividadesResponseDto {

    private Long duracaoQuestionario;
    private Integer numeroTentativas;

    private List<QuestoesResponseDto> questoesQuestionario;

    public Long getDuracaoQuestionario() {
        return duracaoQuestionario;
    }

    public void setDuracaoQuestionario(Long duracaoQuestionario) {
        this.duracaoQuestionario = duracaoQuestionario;
    }

    public Integer getNumeroTentativas() {
        return numeroTentativas;
    }

    public void setNumeroTentativas(Integer numeroTentativas) {
        this.numeroTentativas = numeroTentativas;
    }

    public List<QuestoesResponseDto> getQuestoesQuestionario() {
        return questoesQuestionario;
    }

    public void setQuestoesQuestionario(List<QuestoesResponseDto> questoesQuestionario) {
        this.questoesQuestionario = questoesQuestionario;
    }
}
