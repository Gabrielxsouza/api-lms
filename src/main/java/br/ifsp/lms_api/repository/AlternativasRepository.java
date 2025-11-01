package br.ifsp.lms_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.ifsp.lms_api.model.Alternativas;

@Repository
public interface AlternativasRepository extends JpaRepository<Alternativas, Long> {
    
}
