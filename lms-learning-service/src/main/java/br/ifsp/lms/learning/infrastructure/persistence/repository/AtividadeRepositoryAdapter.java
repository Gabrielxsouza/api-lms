package br.ifsp.lms.learning.infrastructure.persistence.repository;

import br.ifsp.lms.learning.application.port.out.AtividadeRepositoryPort;
import br.ifsp.lms.learning.domain.model.Atividade;
import br.ifsp.lms.learning.infrastructure.mapper.AtividadeMapper;
import br.ifsp.lms.learning.infrastructure.persistence.entity.AtividadeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AtividadeRepositoryAdapter implements AtividadeRepositoryPort {

    private final JpaAtividadeRepository jpaRepository;
    private final AtividadeMapper mapper;

    @Override
    public Atividade save(Atividade atividade) {
        AtividadeEntity entity = mapper.toEntity(atividade);
        AtividadeEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Atividade> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Atividade> findAllByTurmaId(Long turmaId) {
        // Implementation pending: need join with topics?
        // For now return empty or implement simple find all
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
