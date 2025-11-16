package br.ifsp.lms_api.dto.TentativaTextoDto;

import java.time.LocalDateTime;
import java.util.Optional;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TentativaTextoUpdateDto {
    Optional<String> textoResposta = Optional.empty();
    Optional<String> feedback = Optional.empty();
    Optional<Double> nota = Optional.empty();
    Optional<LocalDateTime> dataEnvio = Optional.empty();


    
}
