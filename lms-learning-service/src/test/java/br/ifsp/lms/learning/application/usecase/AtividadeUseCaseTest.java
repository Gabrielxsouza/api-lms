package br.ifsp.lms.learning.application.usecase;

import br.ifsp.lms.learning.application.port.out.AtividadeRepositoryPort;
import br.ifsp.lms.learning.domain.model.Atividade;
import br.ifsp.lms.learning.domain.model.AtividadeQuestionario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtividadeUseCaseTest {

    @Mock
    private AtividadeRepositoryPort repository;

    @InjectMocks
    private CriarAtividadeUseCase criarAtividadeUseCase;

    @InjectMocks
    private BuscarAtividadeUseCase buscarAtividadeUseCase;

    @Test
    void criarAtividade_ShouldReturnSavedAtividade() {
        AtividadeQuestionario atividade = AtividadeQuestionario.builder()
                .titulo("New Activity")
                .build();

        when(repository.save(any(Atividade.class))).thenReturn(atividade);

        Atividade result = criarAtividadeUseCase.execute(atividade);

        assertThat(result).isNotNull();
        assertThat(result.getTitulo()).isEqualTo("New Activity");
        verify(repository).save(atividade);
    }

    @Test
    void buscarAtividade_ShouldReturnAtividade_WhenExists() {
        AtividadeQuestionario atividade = AtividadeQuestionario.builder()
                .id(1L)
                .titulo("Found Activity")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(atividade));

        Optional<Atividade> result = buscarAtividadeUseCase.execute(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitulo()).isEqualTo("Found Activity");
    }

    @Test
    void buscarAtividade_ShouldReturnEmpty_WhenNotExists() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<Atividade> result = buscarAtividadeUseCase.execute(99L);

        assertThat(result).isEmpty();
    }
}
