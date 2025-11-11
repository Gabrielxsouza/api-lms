package br.ifsp.lms_api.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("ADMIN") 
@NoArgsConstructor
public class Administrador extends Usuario {
    
}