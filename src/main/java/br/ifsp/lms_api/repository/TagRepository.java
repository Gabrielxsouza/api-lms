package br.ifsp.lms_api.repository;

import br.ifsp.lms_api.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findAllById(Iterable<Long> ids);
}