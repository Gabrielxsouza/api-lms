package br.ifsp.lms_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.ifsp.lms_api.model.TentativaQuestionario;

public interface TentativaQuestionarioRepository extends JpaRepository<TentativaQuestionario, Long> {
    @Query("SELECT t FROM TentativaQuestionario t WHERE t.atividadeQuestionario.idAtividade = :questionarioId AND t.aluno.id = :alunoId")
public abstract List<TentativaQuestionario> findByAtividadeQuestionario_IdAndAluno_Id(
    @Param("questionarioId") Long questionarioId, 
    @Param("alunoId") Long alunoId
);
}
