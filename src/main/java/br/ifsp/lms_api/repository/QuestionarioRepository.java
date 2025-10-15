package br.ifsp.lms_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.lms_api.model.Questionario;

public interface QuestionarioRepository extends JpaRepository<Questionario, Long> {
    
}
