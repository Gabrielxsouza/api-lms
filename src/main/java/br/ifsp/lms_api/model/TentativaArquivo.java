package br.ifsp.lms_api.model;

import jakarta.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "id_atividade_arquivo")
    private AtividadeArquivos atividadeArquivo; 

    @ManyToOne
    @JoinColumn(name = "id_aluno")
    private Aluno aluno;
}