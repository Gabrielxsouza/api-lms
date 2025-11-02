// src/test/java/br/ifsp/lms_api/service/AtividadeQuestionarioServiceTest.java
package br.ifsp.lms_api.service;

import static org.mockito.Mockito.*; // Para when, verify, any, etc.
import static org.junit.jupiter.api.Assertions.*; // Para assertEquals, assertNotNull, assertThrows, etc.

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.model.AtividadeQuestionario;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.repository.AtividadeQuestionarioRepository;
import br.ifsp.lms_api.repository.QuestoesRepository;
import br.ifsp.lms_api.mapper.PagedResponseMapper;

@ExtendWith(MockitoExtension.class)
class AtividadeQuestionarioServiceTest {

    // 1. Crie mocks para TODAS as dependências do Service
    @Mock
    private AtividadeQuestionarioRepository atividadeQuestionarioRepository;

    @Mock
    private QuestoesRepository questoesRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper; // Mesmo que não usado nestes métodos,
                                                     // o @InjectMocks precisa dele

    // 2. Injete os mocks na classe que estamos testando
    @InjectMocks
    private AtividadeQuestionarioService atividadeQuestionarioService;

    
    // Teste para um método que usa findEntityById (como o update)
    @Test
    void testFindEntityById_WhenNotFound_ShouldThrowException() {
        // --- 1. Arrange (Arrumar) ---
        long idInexistente = 99L;
        String expectedMessage = "Atividade de Texto com ID 99 não encontrada.";
        
        // Simula o repositório retornando "vazio"
        when(atividadeQuestionarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        // Verifica se a exceção correta é lançada
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            // Este método é privado, mas é chamado por 'update', por exemplo.
            // Para testá-lo diretamente, você teria que torná-lo 'protected'
            // ou testar o 'update' com um ID inexistente.
            // Vamos testar o 'update' para verificar a exceção:
            atividadeQuestionarioService.updateAtividadeQuestionario(idInexistente, null); 
        });

        // Verifica a mensagem da exceção
        assertEquals(expectedMessage, exception.getMessage());
        
        // Verifica se o findById foi chamado
        verify(atividadeQuestionarioRepository).findById(idInexistente);
    }

    @Test
    void testAdicionarQuestoes_Success() {
        // --- 1. Arrange (Arrumar) ---
        Long idQuestionario = 1L;
        List<Long> idsDasQuestoes = List.of(10L, 11L);

        // Cria o questionário mockado que será "encontrado"
        AtividadeQuestionario questionarioMock = new AtividadeQuestionario();
        questionarioMock.setIdAtividade(idQuestionario);
        questionarioMock.setQuestoes(new ArrayList<>()); // Inicializa a lista

        // Cria as questões mockadas que serão "encontradas"
        Questoes questao1 = new Questoes();
        questao1.setIdQuestao(10L);
        Questoes questao2 = new Questoes();
        questao2.setIdQuestao(11L);
        List<Questoes> questoesListMock = List.of(questao1, questao2);
        
        // Cria o DTO de resposta que o ModelMapper deve retornar
        AtividadeQuestionarioResponseDto responseDtoMock = new AtividadeQuestionarioResponseDto();
        responseDtoMock.setIdAtividade(idQuestionario);

        // Configura os mocks
        when(atividadeQuestionarioRepository.findById(idQuestionario)).thenReturn(Optional.of(questionarioMock));
        when(questoesRepository.findAllById(idsDasQuestoes)).thenReturn(questoesListMock);
        
        // Quando salvar, retorne o próprio objeto salvo
        when(atividadeQuestionarioRepository.save(any(AtividadeQuestionario.class))).thenReturn(questionarioMock); 
        
        // Simula o ModelMapper
        when(modelMapper.map(any(AtividadeQuestionario.class), eq(AtividadeQuestionarioResponseDto.class)))
            .thenReturn(responseDtoMock);

        // --- 2. Act (Agir) ---
        AtividadeQuestionarioResponseDto result = atividadeQuestionarioService.adicionarQuestoes(idQuestionario, idsDasQuestoes);

        // --- 3. Assert (Verificar) ---
        assertNotNull(result);
        assertEquals(idQuestionario, result.getIdAtividade());
        
        // Verifica se as questões foram realmente adicionadas ao objeto antes de salvar
        assertEquals(2, questionarioMock.getQuestoes().size());
        assertTrue(questionarioMock.getQuestoes().contains(questao1));

        // Verifica se os métodos corretos dos mocks foram chamados
        verify(atividadeQuestionarioRepository).findById(idQuestionario);
        verify(questoesRepository).findAllById(idsDasQuestoes);
        verify(atividadeQuestionarioRepository).save(questionarioMock);
    }

    @Test
    void testAdicionarQuestoes_WhenQuestionarioNotFound_ShouldThrowException() {
        // --- 1. Arrange (Arrumar) ---
        Long idInexistente = 99L;
        List<Long> idsDasQuestoes = List.of(10L, 11L);

        when(atividadeQuestionarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            atividadeQuestionarioService.adicionarQuestoes(idInexistente, idsDasQuestoes);
        });

        assertEquals("Questionário não encontrado com ID: " + idInexistente, exception.getMessage());

        // Garante que o service parou antes de tentar buscar questões ou salvar
        verify(atividadeQuestionarioRepository).findById(idInexistente);
        verify(questoesRepository, never()).findAllById(any());
        verify(atividadeQuestionarioRepository, never()).save(any());
    }
}