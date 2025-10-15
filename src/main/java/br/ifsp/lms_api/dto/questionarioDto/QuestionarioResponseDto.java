package br.ifsp.lms_api.dto.questionarioDto;

import java.util.List;



import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;



import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
public class QuestionarioResponseDto extends AtividadesResponseDto {


    private long duracaoQuestionario;

    private int numeroTentativas;

    private List<QuestoesResponseDto> questionario;

    // getters e setters

   

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

    public List<QuestoesResponseDto> getQuestionario() {
        return questionario;
    }

    public void setQuestionario(List<QuestoesResponseDto> questionario) {
        this.questionario = questionario;
    }
}
