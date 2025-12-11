package br.ifsp.lms_api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
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

    @Column(name = "id_questionario")
    private Long idAtividade;

    @ManyToOne
    @JoinColumn(name = "id_aluno")
    private Aluno aluno;
}
