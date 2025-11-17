package br.ifsp.lms_api.dto.analise;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioDesempenhoResponseDto {

    private List<TagDesempenhoDto> desempenhoGeral; 

    private List<TagDesempenhoDto> pontosFracos;

    private List<MaterialSugeridoDto> sugestoesEstudo;
}