package br.ifsp.lms_api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaterialDeAula {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMaterialDeAula;

    @NotBlank(message = "O nome do arquivo é obrigatorio")
    private String nomeArquivo;

    @NotBlank(message = "O url do arquivo é obrigatorio")
    private String urlArquivo;

    @NotBlank(message = "O tipo do arquivo é obrigatorio")
    private String tipoArquivo;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTopico")
    private Topicos topico;

}
