// Salve este arquivo em:
// src/test/java/br/ifsp/lms_api/service/QuestoesServiceTest.java

package br.ifsp.lms_api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.questoesDto.QuestoesRequestDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesUpdateDto;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.repository.QuestoesRepository;

@ExtendWith(MockitoExtension.class)
class QuestoesServiceTest {

    // 1. Crie mocks para TODAS as dependências do Service
    @Mock
    private QuestoesRepository questoesRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    // 2. Injete os mocks na classe que estamos testando
    @InjectMocks
    private QuestoesService questoesService;

    @Test
    void testCreateQuestao_Success() {
        // --- 1. Arrange (Arrumar) ---
        
        // DTO de requisição (entrada)
        QuestoesRequestDto requestDto = new QuestoesRequestDto();

        // Lista de alternativas que viriam no DTO
            Alternativas alt1 = new Alternativas();
        alt1.setAlternativa("Alternativa 1"); // <-- CORRIGIDO
        List<Alternativas> alternativasList = new ArrayList<>(List.of(alt1));   
        
        // Entidade que o ModelMapper "criará"
        Questoes questaoEntity = new Questoes();
        questaoEntity.setEnunciado("Enunciado da Questão");
        questaoEntity.setAlternativas(alternativasList); // Esta lista será processada

        // Entidade "salva" que o repositório retornará
        Questoes savedQuestaoEntity = new Questoes();
        savedQuestaoEntity.setIdQuestao(1L);
        savedQuestaoEntity.setEnunciado("Enunciado da Questão");
        savedQuestaoEntity.setAlternativas(alternativasList);

        // DTO de resposta (saída)
        QuestoesResponseDto responseDto = new QuestoesResponseDto();
        responseDto.setIdQuestao(1L);
        responseDto.setEnunciado("Enunciado da Questão");

        // Configuração dos Mocks
        when(modelMapper.map(requestDto, Questoes.class)).thenReturn(questaoEntity);
        when(questoesRepository.save(any(Questoes.class))).thenReturn(savedQuestaoEntity);
        when(modelMapper.map(savedQuestaoEntity, QuestoesResponseDto.class)).thenReturn(responseDto);

        // --- 2. Act (Agir) ---
        QuestoesResponseDto result = questoesService.createQuestao(requestDto);

        // --- 3. Assert (Verificar) ---
        assertNotNull(result);
        assertEquals(1L, result.getIdQuestao());

        // **Verifica a lógica principal do método**: o "bind reverso" foi feito?
        // O `setQuestoes(questao)` foi chamado na alternativa?
        assertNotNull(questaoEntity.getAlternativas().get(0).getQuestoes());
        assertEquals(questaoEntity, questaoEntity.getAlternativas().get(0).getQuestoes());
        
        // Verifica se os mocks foram chamados
        verify(modelMapper, times(1)).map(requestDto, Questoes.class);
        verify(questoesRepository, times(1)).save(questaoEntity);
        verify(modelMapper, times(1)).map(savedQuestaoEntity, QuestoesResponseDto.class);
    }
    
    @Test
    void testGetAllQuestoes_Success() {
        // --- 1. Arrange (Arrumar) ---
        Pageable pageable = Pageable.unpaged(); // Um Pageable simples para o teste
        
        Questoes questao = new Questoes();
        questao.setIdQuestao(1L);
        Page<Questoes> questoesPage = new PageImpl<>(List.of(questao), pageable, 1);
        
        // Cria um mock da resposta final do PagedResponseMapper
      PagedResponse<QuestoesResponseDto> pagedResponseMock = mock(PagedResponse.class);
        // (Aqui você poderia popular o pagedResponseMock se precisasse
        // verificar os campos dele, mas para este teste, basta verificar a chamada)

        // Configuração dos Mocks
        when(questoesRepository.findAll(pageable)).thenReturn(questoesPage);
        when(pagedResponseMapper.toPagedResponse(questoesPage, QuestoesResponseDto.class))
            .thenReturn(pagedResponseMock);

        // --- 2. Act (Agir) ---
        PagedResponse<QuestoesResponseDto> result = questoesService.getAllQuestoes(pageable);

        // --- 3. Assert (Verificar) ---
        assertNotNull(result);
        assertEquals(pagedResponseMock, result); // Verifica se o resultado é o objeto mockado
        
        verify(questoesRepository, times(1)).findAll(pageable);
        verify(pagedResponseMapper, times(1)).toPagedResponse(questoesPage, QuestoesResponseDto.class);
    }

    @Test
    void testUpdateQuestao_WhenNotFound_ShouldThrowException() {
        // --- 1. Arrange (Arrumar) ---
        Long idInexistente = 99L;
        QuestoesUpdateDto updateDto = new QuestoesUpdateDto(); // DTO de entrada
        updateDto.setEnunciado(Optional.of("Novo Enunciado"));
        
        String expectedMessage = "Questão not found with id: " + idInexistente;

        // Simula o repositório não encontrando nada
        when(questoesRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            // Tenta executar o método que deve falhar
            questoesService.updateQuestao(idInexistente, updateDto);
        });

        // Verifica a mensagem da exceção
        assertEquals(expectedMessage, exception.getMessage());
        
        // Verifica se o método do repositório foi chamado
        verify(questoesRepository, times(1)).findById(idInexistente);
        
        // Garante que o service parou e NÃO tentou salvar nada
        verify(questoesRepository, never()).save(any());
    }

    @Test
    void testDeleteQuestao_Success() {
        // --- 1. Arrange (Arrumar) ---
        Long idExistente = 1L;
        Questoes questaoMock = new Questoes(); // A entidade que será encontrada
        questaoMock.setIdQuestao(idExistente);

        // Simula o repositório encontrando a entidade
        when(questoesRepository.findById(idExistente)).thenReturn(Optional.of(questaoMock));
        
        // Para métodos 'void', usamos doNothing()
        doNothing().when(questoesRepository).delete(questaoMock);

        // --- 2. Act (Agir) ---
        // Verifica se o método executa sem lançar exceções
        assertDoesNotThrow(() -> {
            questoesService.deleteQuestao(idExistente);
        });

        // --- 3. Assert (Verificar) ---
        // Verifica se o findById foi chamado
        verify(questoesRepository, times(1)).findById(idExistente);
        
        // Verifica se o delete foi chamado com a entidade correta
        verify(questoesRepository, times(1)).delete(questaoMock);
    }

    @Test
    void testDeleteQuestao_WhenNotFound_ShouldThrowException() {
        // --- 1. Arrange (Arrumar) ---
        Long idInexistente = 99L;
        String expectedMessage = "Questão not found with id: " + idInexistente;

        // Simula o repositório não encontrando nada
        when(questoesRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            questoesService.deleteQuestao(idInexistente);
        });

        assertEquals(expectedMessage, exception.getMessage());
        
        verify(questoesRepository, times(1)).findById(idInexistente);
        
        // Garante que o delete NUNCA foi chamado
        verify(questoesRepository, never()).delete(any());
    }
}