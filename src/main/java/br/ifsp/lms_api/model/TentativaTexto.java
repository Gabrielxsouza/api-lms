package br.ifsp.lms_api.model;

import jakarta.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "id_atividade_texto")
    private AtividadeTexto atividadeTexto;

    @ManyToOne
    @JoinColumn(name = "id_aluno")
    private Aluno aluno;
}