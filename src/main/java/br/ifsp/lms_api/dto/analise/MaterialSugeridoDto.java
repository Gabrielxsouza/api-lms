// DTO para o material de estudo sugerido
package br.ifsp.lms_api.dto.analise;

import lombok.Data;

@Data
public class MaterialSugeridoDto {
    private String nomeTopicoRelacionado;
    private String nomeMaterial;
    private String urlMaterial;
}