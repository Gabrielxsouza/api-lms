package br.ifsp.lms.learning.infrastructure.persistence.entity;

import jakarta.persistence.*;
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
@DiscriminatorValue("ARQUIVOS")
public class AtividadeArquivosEntity extends AtividadeEntity {
    @ElementCollection
    @CollectionTable(name = "atividade_arquivos_permitidos", joinColumns = @JoinColumn(name = "idAtividade"))
    private List<String> arquivosPermitidos = new ArrayList<>();
}
