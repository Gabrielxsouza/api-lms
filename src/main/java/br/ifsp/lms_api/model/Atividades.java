package br.ifsp.lms_api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Define a estratégia de herança
public class Atividades {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAtividade;

    @NotBlank(message = "O título da atividade é obrigatório")
    @Size(min = 5, max = 100, message = "O título da atividade deve conter entre 5 e 100 caracteres")
    private String tituloAtividade;

    private String descricaoAtividade;

    @NotNull(message = "A data de início da atividade é obrigatória")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioAtividade;

    @NotNull(message = "A data de fechamento da atividade é obrigatória")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFechamentoAtividade;

    private Boolean statusAtividade;
}