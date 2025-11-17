package br.ifsp.lms_api.dto.analise;

import lombok.Data;

@Data
public class TagDesempenhoDto {
    private String nomeTag;
    private double mediaNota;
    private int totalAvaliacoes;
}