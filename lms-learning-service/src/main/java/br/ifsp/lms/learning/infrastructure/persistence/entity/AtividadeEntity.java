package br.ifsp.lms.learning.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "atividade")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TIPO_ATIVIDADE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AtividadeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAtividade;

    private String tituloAtividade;
    private String descricaoAtividade;
    private LocalDate dataInicioAtividade;
    private LocalDate dataFechamentoAtividade;
    private Boolean statusAtividade;

    // Decoupled from Topicos Entity
    @Column(name = "id_topico")
    private Long topicoId;

    @ElementCollection
    @CollectionTable(name = "atividade_tags_simple", joinColumns = @JoinColumn(name = "id_atividade"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();
}
