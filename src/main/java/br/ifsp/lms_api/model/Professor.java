package br.ifsp.lms_api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("PROFESSOR")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Professor extends Usuario {
    
    @Column(length = 100)
    private String departamento;

    @OneToMany(
        mappedBy = "professor",
        fetch = FetchType.LAZY
    )
    @JsonManagedReference
    private List<Turma> turmas;


}