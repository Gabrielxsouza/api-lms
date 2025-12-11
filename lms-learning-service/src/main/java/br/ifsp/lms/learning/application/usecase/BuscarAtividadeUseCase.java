package br.ifsp.lms.learning.application.usecase;

import br.ifsp.lms.learning.application.port.out.AtividadeRepositoryPort;
import br.ifsp.lms.learning.domain.model.Atividade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BuscarAtividadeUseCase {
    private final AtividadeRepositoryPort repository;

    public Optional<Atividade> execute(Long id) {
        return repository.findById(id);
    }
}
