package br.ifsp.lms_api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("ALUNO")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Aluno extends Usuario {
    @Column(unique = true, length = 20)
    private String ra;

    @JsonBackReference
    @OneToMany(
        mappedBy = "aluno",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Matricula> matriculas;
}
