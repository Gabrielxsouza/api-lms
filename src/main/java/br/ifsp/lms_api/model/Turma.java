package br.ifsp.lms_api.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Turma {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTurma;

    @NotBlank(message = "O nome da turma é obrigatorio")
    private String nomeTurma;

    @Column(length = 20)
    private String semestre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDisciplina")
    @JsonManagedReference
    private Disciplina disciplina;

    @JsonManagedReference
    @OneToMany(
        mappedBy = "turma", 
        cascade = CascadeType.ALL, 
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<Topicos> topicos;
}
