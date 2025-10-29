package br.ifsp.lms_api.dto.atividadeQuestionarioDto;

import java.util.List;



import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;



import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
public class AtividadeQuestionarioResponseDto extends AtividadesResponseDto {


    private long duracaoQuestionario;

    private int numeroTentativas;

    private List<QuestoesResponseDto> questionario;

    //setters

   

    public long getDuracaoQuestionario() {
        return duracaoQuestionario;
    }


    public int getNumeroTentativas() {
        return numeroTentativas;
    }

    public List<QuestoesResponseDto> getQuestionario() {
        return questionario;
    }

}
