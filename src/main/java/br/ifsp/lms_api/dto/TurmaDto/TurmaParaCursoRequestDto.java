package br.ifsp.lms_api.dto.TurmaDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TurmaParaCursoRequestDto {
    @NotBlank(message = "O nome da turma é obrigatorio")
    private String nomeTurma;

    private String semestre;

    @NotNull(message = "É obrigatório informar o ID da disciplina existente")
    private Long disciplinaId; // <--- A CHAVE DA SOLUÇÃO
}