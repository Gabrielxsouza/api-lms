package br.ifsp.lms.learning.infrastructure.persistence.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DiscriminatorValue("TEXTO")
public class AtividadeTextoEntity extends AtividadeEntity {
    private Long numeroMaximoCaracteres;
}
