package br.ifsp.lms_api.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotEmpty;
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
public class Questionario extends Atividades {

    private long duracaoQuestionario;

    private int numeroTentativas;

    @OneToMany(mappedBy = "questionario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @NotEmpty(message = "O questionário deve ter pelo menos uma questão")
    private List<Questoes> questoes = new ArrayList<>();

}