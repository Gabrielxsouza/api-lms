package br.ifsp.lms_api.model;

import jakarta.persistence.Entity;

@Entity
public enum Status {
    ATIVA,
    PENDENTE,
    REPROVADA
}
