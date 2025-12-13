package br.ifsp.lms.learning.infrastructure.persistence;

import br.ifsp.lms.learning.domain.model.Atividade;
import br.ifsp.lms.learning.domain.model.AtividadeQuestionario;
import br.ifsp.lms.learning.infrastructure.mapper.AtividadeMapper;
import br.ifsp.lms.learning.infrastructure.persistence.repository.AtividadeRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ AtividadeRepositoryAdapter.class, AtividadeMapper.class })
class AtividadeRepositoryAdapterTest {

    @Autowired
    private AtividadeRepositoryAdapter repository;

    @Test
    void save_ShouldPersistAtividadeQuestionario() {
        AtividadeQuestionario atividade = AtividadeQuestionario.builder()
                .titulo("Test Quiz")
                .descricao("Description")
                .dataInicio(LocalDate.now())
                .dataFechamento(LocalDate.now().plusDays(1))
                .status(true)
                .duracaoMinutes(60L)
                .tentativasPermitidas(3)
                .build();

        Atividade saved = repository.save(atividade);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitulo()).isEqualTo("Test Quiz");
        assertThat(saved).isInstanceOf(AtividadeQuestionario.class);
        assertThat(((AtividadeQuestionario) saved).getDuracaoMinutes()).isEqualTo(60L);
    }

    @Test
    void findById_ShouldReturnAtividade() {
        AtividadeQuestionario atividade = AtividadeQuestionario.builder()
                .titulo("Find Me")
                .descricao("Description")
                .dataInicio(LocalDate.now())
                .dataFechamento(LocalDate.now().plusDays(1))
                .status(true)
                .duracaoMinutes(30L)
                .tentativasPermitidas(1)
                .build();

        Atividade saved = repository.save(atividade);

        Optional<Atividade> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getTitulo()).isEqualTo("Find Me");
    }

    @Test
    void deleteById_ShouldRemoveAtividade() {
        AtividadeQuestionario atividade = AtividadeQuestionario.builder()
                .titulo("Delete Me")
                .build();

        Atividade saved = repository.save(atividade);

        repository.deleteById(saved.getId());

        Optional<Atividade> found = repository.findById(saved.getId());
        assertThat(found).isEmpty();
    }
}
