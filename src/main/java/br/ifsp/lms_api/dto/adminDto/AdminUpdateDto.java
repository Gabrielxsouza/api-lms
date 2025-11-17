package br.ifsp.lms_api.dto.adminDto;

import java.util.Optional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateDto {

    private Optional<@Size(min = 3, max = 100) String> nome = Optional.empty();

    private Optional<@Email(message = "Formato de e-mail inválido") String> email = Optional.empty();

    private Optional<@Size(min = 6) String> senha = Optional.empty();

}
