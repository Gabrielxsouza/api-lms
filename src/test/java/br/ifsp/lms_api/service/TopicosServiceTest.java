// Salve este arquivo em:
// src/test/java/br/ifsp/lms_api/service/TopicosServiceTest.java

package br.ifsp.lms_api.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import br.ifsp.lms_api.dto.TopicosDto.TopicosRequestDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosResponseDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosUpdateDto;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class TopicosServiceTest {

    // 1. Crie mocks para TODAS as dependências
    @Mock
    private TopicosRepository topicosRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    // 2. Use @Spy para o ModelMapper se você for usá-lo de verdade, 
    //    ou @Mock se você for mockar todas as chamadas. Vamos mockar.
    @Mock
    private ModelMapper modelMapper;

    // 3. Injete os mocks na classe que estamos testando
    @InjectMocks
    private TopicosService topicosService;

    // --- Variáveis de Teste ---
    private Turma turmaPadrao;
    private TopicosRequestDto requestDto;
    private String htmlSuja;
    private String htmlLimpa;

    @BeforeEach
    void setUp() {
        // Setup de dados comuns
        turmaPadrao = new Turma();
        turmaPadrao.setIdTurma(1L);

        htmlSuja = "<p onclick='alert(1)'>Conteúdo</p><script>alert('XSS')</script>";
        // O que esperamos que o sanitizador faça:
        htmlLimpa = "<p>Conteúdo</p>"; 

        requestDto = new TopicosRequestDto();
        requestDto.setIdTurma(1L);
        requestDto.setTituloTopico("Teste de Tópico");
        requestDto.setConteudoHtml(htmlSuja);
    }

    @Test
    void testCreateTopico_Success_And_SanitizeHtml() {
        // --- 1. Arrange (Arrumar) ---
        Topicos topicoMapeado = new Topicos(); // O que o mapper(dto) retorna
        Topicos topicoSalvo = new Topicos();   // O que o repository.save() retorna
        topicoSalvo.setIdTopico(10L);
        topicoSalvo.setConteudoHtml(htmlLimpa); // O service deve ter limpado

        TopicosResponseDto responseDto = new TopicosResponseDto(); // O que o mapper(entity) retorna
        responseDto.setIdTopico(10L);

        // Configura os mocks
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turmaPadrao));
        when(modelMapper.map(requestDto, Topicos.class)).thenReturn(topicoMapeado);
        when(topicosRepository.save(any(Topicos.class))).thenReturn(topicoSalvo);
        when(modelMapper.map(topicoSalvo, TopicosResponseDto.class)).thenReturn(responseDto);

        // --- 2. Act (Agir) ---
        TopicosResponseDto result = topicosService.createTopico(requestDto);

        // --- 3. Assert (Verificar) ---
        assertNotNull(result);
        assertEquals(10L, result.getIdTopico());

        // Verifica se os métodos corretos foram chamados
        verify(turmaRepository).findById(1L);
        verify(topicosRepository).save(any(Topicos.class));

        // **Teste Crucial de Segurança**: Verifica se o HTML foi limpo ANTES de salvar
        ArgumentCaptor<Topicos> captor = ArgumentCaptor.forClass(Topicos.class);
        verify(topicosRepository).save(captor.capture());
        
        Topicos topicoCapturado = captor.getValue();
        assertEquals(htmlLimpa, topicoCapturado.getConteudoHtml());
        assertEquals(turmaPadrao, topicoCapturado.getTurma());
        assertNull(topicoCapturado.getIdTopico()); // Verifica se o ID foi nulificado
    }

    @Test
    void testCreateTopico_TurmaNotFound_ShouldThrowException() {
        // --- 1. Arrange (Arrumar) ---
        when(turmaRepository.findById(1L)).thenReturn(Optional.empty());

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            topicosService.createTopico(requestDto);
        });

        assertEquals("Turma com ID 1 não encontrada", exception.getMessage());
        verify(topicosRepository, never()).save(any()); // Garante que não tentou salvar
    }

    @Test
    void testGetTopicoById_NotFound_ShouldThrowException() {
        // --- 1. Arrange (Arrumar) ---
        when(topicosRepository.findById(99L)).thenReturn(Optional.empty());

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            topicosService.getTopicoById(99L);
        });
        
        assertEquals("Topico com ID 99 nao encontrado", exception.getMessage());
    }

    @Test
    void testUpdateTopico_Success_And_SanitizeHtml() {
        // --- 1. Arrange (Arrumar) ---
        Long idTopico = 1L;
        TopicosUpdateDto updateDto = new TopicosUpdateDto();
        updateDto.setConteudoHtml(Optional.of(htmlSuja));
        updateDto.setTituloTopico(Optional.of("Título Novo"));
        
        Topicos topicoExistente = new Topicos();
        topicoExistente.setIdTopico(idTopico);
        topicoExistente.setTituloTopico("Título Antigo");
        topicoExistente.setConteudoHtml("HTML Antigo");
        
        TopicosResponseDto responseDto = new TopicosResponseDto();
        responseDto.setIdTopico(idTopico);

        when(topicosRepository.findById(idTopico)).thenReturn(Optional.of(topicoExistente));
        when(topicosRepository.save(any(Topicos.class))).thenReturn(topicoExistente);
        when(modelMapper.map(topicoExistente, TopicosResponseDto.class)).thenReturn(responseDto);

        // --- 2. Act (Agir) ---
        topicosService.updateTopico(idTopico, updateDto);

        // --- 3. Assert (Verificar) ---
        // Captura o que foi salvo
        ArgumentCaptor<Topicos> captor = ArgumentCaptor.forClass(Topicos.class);
        verify(topicosRepository).save(captor.capture());
        
        Topicos topicoSalvo = captor.getValue();
        
        // Verifica se os campos foram atualizados CORRETAMENTE
        assertEquals("Título Novo", topicoSalvo.getTituloTopico());
        assertEquals(htmlLimpa, topicoSalvo.getConteudoHtml());
    }
}