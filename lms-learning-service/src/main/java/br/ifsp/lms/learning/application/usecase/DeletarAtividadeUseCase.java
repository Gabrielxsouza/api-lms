package br.ifsp.lms.learning.application.usecase;

import br.ifsp.lms.learning.application.port.out.AtividadeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeletarAtividadeUseCase {
    private final AtividadeRepositoryPort repository;

    public void execute(Long id) {
        repository.deleteById(id);
    }
}
