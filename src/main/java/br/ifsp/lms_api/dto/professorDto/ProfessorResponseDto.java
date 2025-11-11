package br.ifsp.lms_api.dto.professorDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorResponseDto {
    
    private Long idUsuario;
    private String nome;
    private String email;
    private String cpf;

    private String departamento;
}