package br.ifsp.lms_api.repository;

import br.ifsp.lms_api.model.TentativaArquivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TentativaArquivoRepository extends JpaRepository<TentativaArquivo, Long> {
    
    Page<TentativaArquivo> findByAluno_IdUsuario(Long idUsuario, Pageable pageable);
    
}