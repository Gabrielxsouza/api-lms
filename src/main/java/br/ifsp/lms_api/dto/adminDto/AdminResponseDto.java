package br.ifsp.lms_api.dto.adminDto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminResponseDto {

    private Long idUsuario;
    private String nome;
    private String email;
    private String cpf;
    private String tipoUsuario;

}
