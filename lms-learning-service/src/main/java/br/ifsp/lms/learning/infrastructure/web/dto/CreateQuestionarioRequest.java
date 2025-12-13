package br.ifsp.lms.learning.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CreateQuestionarioRequest {

    private String tituloAtividade;
    private String descricaoAtividade;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioAtividade;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFechamentoAtividade;

    private Boolean statusAtividade;


    private Long duracaoQuestionario;
    private Integer numeroTentativas;

    private Long idTopico;
    private java.util.Set<String> tags;

}
