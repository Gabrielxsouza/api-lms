package br.ifsp.lms_api.repository;

import br.ifsp.lms_api.model.AtividadeTexto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtividadeTextoRepository extends JpaRepository<AtividadeTexto, Long> {
    
    // Você pode adicionar buscas específicas para este tipo, se necessário.
    // Ex:
    // List<AtividadeTexto> findByNumeroMaximoCaracteresGreaterThan(long max);
}