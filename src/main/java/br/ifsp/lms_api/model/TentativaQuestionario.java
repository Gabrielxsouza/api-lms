package br.ifsp.lms_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TentativaQuestionario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTentativaQuestionario;
    
    private Double nota;
    
    private int numeroDaTentativa;

    @NotNull(message = "A data de envio é obrigatorio")
    private LocalDateTime dataEnvio;
    private List<Long> respostas;

    @ManyToOne
    @JoinColumn(name = "id_questionario")
    private AtividadeQuestionario atividadeQuestionario;

    @ManyToOne
    @JoinColumn(name = "id_aluno")
    private Aluno aluno;
}
