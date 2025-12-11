package br.ifsp.lms_api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TentativaTexto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTentativa;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dataEnvio;

    @NotBlank(message = "O texto da resposta não pode estar vazio")
    private String textoResposta;

    private String feedBack;

    private Double nota;

    @Column(name = "id_atividade_texto")
    private Long idAtividade;

    @ManyToOne
    @JoinColumn(name = "id_aluno")
    private Aluno aluno;
}