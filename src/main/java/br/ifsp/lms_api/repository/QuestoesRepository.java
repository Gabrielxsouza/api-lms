package br.ifsp.lms_api.repository;

import br.ifsp.lms_api.model.Questoes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuestoesRepository extends JpaRepository<Questoes, Long>, JpaSpecificationExecutor<Questoes> {
    
}