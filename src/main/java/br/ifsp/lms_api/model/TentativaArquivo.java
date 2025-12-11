package br.ifsp.lms_api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
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
public class TentativaArquivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTentativa;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dataEnvio;

    private String feedBack;
    private Double nota;

    private String nomeArquivo;
    private String urlArquivo;
    private String tipoArquivo;

    @Column(name = "id_atividade_arquivo")
    private Long idAtividade;

    @ManyToOne
    @JoinColumn(name = "id_aluno")
    private Aluno aluno;
}