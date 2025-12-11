package br.ifsp.lms.learning.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idQuestao;

    private String enunciado;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_questao")
    private List<AlternativaEntity> alternativas = new ArrayList<>();
}
