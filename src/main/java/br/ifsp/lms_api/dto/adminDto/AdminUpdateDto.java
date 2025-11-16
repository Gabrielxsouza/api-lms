package br.ifsp.lms_api.dto.adminDto;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateDto {

    private Optional<String> nome = Optional.empty();

    private Optional<String> email = Optional.empty();

    private Optional<String> senha = Optional.empty();

}
