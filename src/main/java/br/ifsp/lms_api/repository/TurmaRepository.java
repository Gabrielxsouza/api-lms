package br.ifsp.lms_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.lms_api.model.Turma;

public interface TurmaRepository extends JpaRepository<Turma, Long> {
    
}
