package br.ifsp.lms_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
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
}