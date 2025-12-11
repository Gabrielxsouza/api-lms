package br.ifsp.lms.learning.application.usecase;

import br.ifsp.lms.learning.application.port.out.AtividadeRepositoryPort;
import br.ifsp.lms.learning.domain.model.Atividade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CriarAtividadeUseCase {

    private final AtividadeRepositoryPort repository;

    public Atividade execute(Atividade atividade) {
        return repository.save(atividade);
    }
}
