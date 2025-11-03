package br.ifsp.lms_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.lms_api.model.MaterialDeAula;

public interface MaterialDeAulaRepository extends JpaRepository<MaterialDeAula, Long> {
    
}
