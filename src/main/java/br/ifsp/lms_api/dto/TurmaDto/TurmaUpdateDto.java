package br.ifsp.lms_api.dto.TurmaDto;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurmaUpdateDto {
    private Optional<String> semestre = Optional.empty();
    private Optional<String> nomeTurma = Optional.empty();

    //removi os de id pq na atualização não faz sentido atualizar o id da disciplina vinculada e nem o id da turma
}
