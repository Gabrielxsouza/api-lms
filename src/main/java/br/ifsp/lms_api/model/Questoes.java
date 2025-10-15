package br.ifsp.lms_api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Questoes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idQuestao;

    @NotBlank
    @Size(min = 5, max = 100, message = "O enunciado da questão deve conter entre 5 e 100 caracteres")
    private String enunciado;

    @ManyToOne
    @JoinColumn(name = "idQuestionario", nullable = false)
    @JsonBackReference
    @NotNull(message = "A questão deve pertencer a um questionário")
    private Questionario questionario;

    @OneToMany(mappedBy = "questoes", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @NotEmpty(message = "A questão deve ter pelo menos uma alternativa")
    private List<Alternativas> alternativas = new ArrayList<>();

}