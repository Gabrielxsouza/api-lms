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

    @Mock
    private TopicosRepository topicosRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TopicosService topicosService;

    private Turma turmaPadrao;
    private TopicosRequestDto requestDto;
    private String htmlSuja;
    private String htmlLimpa;

    @BeforeEach
    void setUp() {
        turmaPadrao = new Turma();
        turmaPadrao.setIdTurma(1L);

        htmlSuja = "<p onclick='alert(1)'>Conteúdo</p><script>alert('XSS')</script>";
        htmlLimpa = "<p>Conteúdo</p>"; 

        requestDto = new TopicosRequestDto();
        requestDto.setIdTurma(1L);
        requestDto.setTituloTopico("Teste de Tópico");
        requestDto.setConteudoHtml(htmlSuja);
    }

    @Test
    void testCreateTopico_Success_And_SanitizeHtml() {
        Topicos topicoMapeado = new Topicos(); 
        Topicos topicoSalvo = new Topicos();   
        topicoSalvo.setIdTopico(10L);
        topicoSalvo.setConteudoHtml(htmlLimpa); 

        TopicosResponseDto responseDto = new TopicosResponseDto(); 
        responseDto.setIdTopico(10L);

        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turmaPadrao));
        when(modelMapper.map(requestDto, Topicos.class)).thenReturn(topicoMapeado);
        when(topicosRepository.save(any(Topicos.class))).thenReturn(topicoSalvo);
        when(modelMapper.map(topicoSalvo, TopicosResponseDto.class)).thenReturn(responseDto);

        TopicosResponseDto result = topicosService.createTopico(requestDto);

        assertNotNull(result);
        assertEquals(10L, result.getIdTopico());

        verify(turmaRepository).findById(1L);
        verify(topicosRepository).save(any(Topicos.class));

        ArgumentCaptor<Topicos> captor = ArgumentCaptor.forClass(Topicos.class);
        verify(topicosRepository).save(captor.capture());
        
        Topicos topicoCapturado = captor.getValue();
        assertEquals(htmlLimpa, topicoCapturado.getConteudoHtml());
        assertEquals(turmaPadrao, topicoCapturado.getTurma());
        assertNull(topicoCapturado.getIdTopico()); 
    }

    @Test
    void testCreateTopico_TurmaNotFound_ShouldThrowException() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            topicosService.createTopico(requestDto);
        });

        assertEquals("Turma com ID 1 não encontrada", exception.getMessage());
        verify(topicosRepository, never()).save(any()); 
    }

    @Test
    void testGetTopicoById_NotFound_ShouldThrowException() {
        when(topicosRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            topicosService.getTopicoById(99L);
        });
        
        assertEquals("Topico com ID 99 nao encontrado", exception.getMessage());
    }

    @Test
    void testUpdateTopico_Success_And_SanitizeHtml() {
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

        topicosService.updateTopico(idTopico, updateDto);

        ArgumentCaptor<Topicos> captor = ArgumentCaptor.forClass(Topicos.class);
        verify(topicosRepository).save(captor.capture());
        
        Topicos topicoSalvo = captor.getValue();
        
        assertEquals("Título Novo", topicoSalvo.getTituloTopico());
        assertEquals(htmlLimpa, topicoSalvo.getConteudoHtml());
    }
}
