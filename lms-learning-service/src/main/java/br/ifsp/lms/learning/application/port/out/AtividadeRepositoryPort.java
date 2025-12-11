package br.ifsp.lms.learning.application.port.out;

import br.ifsp.lms.learning.domain.model.Atividade;
import java.util.List;
import java.util.Optional;

public interface AtividadeRepositoryPort {
    Atividade save(Atividade atividade);

    Optional<Atividade> findById(Long id);

    List<Atividade> findAllByTurmaId(Long turmaId); // Assuming we query by Turma (from context map)
}
