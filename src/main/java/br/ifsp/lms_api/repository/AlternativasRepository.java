package br.ifsp.lms_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.lms_api.model.Alternativas;

public interface AlternativasRepository extends JpaRepository<Alternativas, Long> {
    
}
