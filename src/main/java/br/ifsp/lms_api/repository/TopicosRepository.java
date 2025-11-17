package br.ifsp.lms_api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.lms_api.model.Topicos;

import java.util.List;
import java.util.Set;

public interface TopicosRepository extends JpaRepository<Topicos, Long> {
    Page<Topicos> findByTurmaIdTurma(Long idTurma, Pageable pageable);
    List<Topicos> findByTags_NomeIn(Set<String> nomesTags);
}
