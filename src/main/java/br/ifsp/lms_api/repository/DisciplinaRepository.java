package br.ifsp.lms_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.lms_api.model.Disciplina;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
    
}
