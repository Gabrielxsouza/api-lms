package br.ifsp.lms_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.ifsp.lms_api.model.Questoes;

@Repository
public interface QuestoesRepository extends JpaRepository<Questoes, Long> {
    
}
