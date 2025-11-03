package br.ifsp.lms_api.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DiscriminatorValue("ARQUIVOS")
public class AtividadeArquivos extends Atividade {
    @ElementCollection
    @CollectionTable(name = "atividade_arquivos_permitidos", joinColumns = @JoinColumn(name = "idAtividade"))
    private List<String> arquivosPermitidos = new ArrayList<>();
}
