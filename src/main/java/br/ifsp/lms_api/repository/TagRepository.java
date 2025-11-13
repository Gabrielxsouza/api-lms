package br.ifsp.lms_api.repository;

import br.ifsp.lms_api.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    // depois vem a busca por nome e etc
}