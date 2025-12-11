package br.ifsp.lms.learning.infrastructure.persistence.repository;

import br.ifsp.lms.learning.infrastructure.persistence.entity.AtividadeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAtividadeRepository extends JpaRepository<AtividadeEntity, Long> {
}
