package br.ifsp.lms_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.lms_api.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    
}