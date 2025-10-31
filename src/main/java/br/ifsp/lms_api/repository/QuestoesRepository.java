package br.ifsp.lms_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.lms_api.model.Questoes;

public interface QuestoesRepository extends JpaRepository<Questoes, Long> {
    
}
