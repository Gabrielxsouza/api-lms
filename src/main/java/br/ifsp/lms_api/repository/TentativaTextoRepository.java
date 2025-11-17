package br.ifsp.lms_api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.lms_api.model.TentativaTexto;

public interface TentativaTextoRepository extends JpaRepository<TentativaTexto, Long> {
    Page<TentativaTexto> findByAluno_IdUsuario(Long idUsuario, Pageable pageable);
}
