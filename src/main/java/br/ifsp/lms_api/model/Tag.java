package br.ifsp.lms_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTag;

    @NotBlank(message = "O nome da tag é obrigatório")
    @Column(unique = true, nullable = false, length = 100)
    private String nome;

    @ManyToMany(mappedBy = "tags")
    private Set<Topicos> topicos = new HashSet<>();

    @ManyToMany(mappedBy = "tags")
    private Set<Questoes> questoes = new HashSet<>();
}