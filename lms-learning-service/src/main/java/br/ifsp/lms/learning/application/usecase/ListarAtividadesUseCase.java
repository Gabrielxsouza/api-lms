package br.ifsp.lms.learning.application.usecase;

import br.ifsp.lms.learning.application.port.out.AtividadeRepositoryPort;
import br.ifsp.lms.learning.domain.model.Atividade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarAtividadesUseCase {
    private final AtividadeRepositoryPort repository;

    public List<Atividade> execute() {
        return repository.findAll();
    }
}
