package br.ifsp.lms_api.model;


import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Topicos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTopico;

    @NotBlank(message = "O titulo do topico é obrigatorio")
    @Max(value = 100, message = "O titulo do topico deve conter no maximo 100 caracteres")
    private String tituloTopico;

    @Lob 
    private String conteudoHTML;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idTurma")
    private Turma turma;
}
