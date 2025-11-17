package br.ifsp.lms_api.dto.analise;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotaTagAgregada {
    private String nomeTag;
    private double nota;
}