package br.ifsp.lms_api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Alternativas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAlternativa;

    @NotBlank
    @Size(min = 1, max = 500, message = "A alternativa deve conter entre 1 e 500 caracteres")
    private String alternativa;

    @NotNull(message = "É obrigatório indicar se a alternativa é correta")
    private Boolean alternativaCorreta;

    @ManyToOne
    @JoinColumn(name = "idQuestao", nullable = false)
    @JsonBackReference
    private Questoes questoes;
}