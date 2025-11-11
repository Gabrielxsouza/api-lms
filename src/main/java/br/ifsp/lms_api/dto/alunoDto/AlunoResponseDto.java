package br.ifsp.lms_api.dto.alunoDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlunoResponseDto {
    
    private Long idUsuario;
    private String nome;
    private String email;
    private String cpf;

    private String ra;
}