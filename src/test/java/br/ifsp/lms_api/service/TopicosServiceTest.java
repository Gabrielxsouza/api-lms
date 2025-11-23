package br.ifsp.lms_api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import br.ifsp.lms_api.dto.TopicosDto.TopicosRequestDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosResponseDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Atividade;
import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.AtividadeRepository;
import br.ifsp.lms_api.repository.TagRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.repository.TurmaRepository;

@ExtendWith(MockitoExtension.class)
class TopicosServiceTest {

    @Mock
    private TopicosRepository topicosRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private AtividadeRepository atividadeRepository;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private AutentificacaoService autentificacaoService;

    @InjectMocks
    private TopicosService topicosService;

    private Turma turmaPadrao;
    private Professor professorDono;
    private TopicosRequestDto requestDto;
    private String htmlSuja;
    private String htmlLimpa;

    @BeforeEach
    void setUp() {
   
        professorDono = new Professor();
        professorDono.setIdUsuario(99L);
        professorDono.setNome("Professor Teste");

        turmaPadrao = new Turma();
        turmaPadrao.setIdTurma(1L);
        turmaPadrao.setProfessor(professorDono); 

        htmlSuja = "<p onclick='alert(1)'>Conteúdo</p><script>alert('XSS')</script>";
        htmlLimpa = "<p>Conteúdo</p>";

        requestDto = new TopicosRequestDto();
        requestDto.setIdTurma(1L);
        requestDto.setTituloTopico("Teste de Tópico");
        requestDto.setConteudoHtml(htmlSuja);
        requestDto.setIdAtividade(List.of(10L));
    }

    @Test
    void testCreateTopico_Success_And_SanitizeHtml() {
        Topicos topicoSalvo = new Topicos();
        topicoSalvo.setIdTopico(1L);
        topicoSalvo.setConteudoHtml(htmlLimpa);
        topicoSalvo.setTurma(turmaPadrao);

        Atividade atividadeMock = new AtividadeTexto();
        atividadeMock.setIdAtividade(10L);

        List<Atividade> atividadesAssociadas = new ArrayList<>();
        atividadesAssociadas.add(atividadeMock);

        TopicosResponseDto responseDto = new TopicosResponseDto();
        responseDto.setIdTopico(1L);

        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turmaPadrao));
        when(topicosRepository.save(any(Topicos.class))).thenReturn(topicoSalvo);

        when(atividadeRepository.findById(10L)).thenReturn(Optional.of(atividadeMock));
        when(atividadeRepository.saveAll(any(List.class))).thenReturn(atividadesAssociadas);

        when(modelMapper.map(topicoSalvo, TopicosResponseDto.class)).thenReturn(responseDto);

        TopicosResponseDto result = topicosService.createTopico(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getIdTopico());

        verify(turmaRepository).findById(1L);
        verify(topicosRepository).save(any(Topicos.class));
        verify(atividadeRepository).findById(10L);

        ArgumentCaptor<List<Atividade>> captor = ArgumentCaptor.forClass(List.class);
        verify(atividadeRepository).saveAll(captor.capture());

        assertEquals(topicoSalvo, captor.getValue().get(0).getTopico());
    }

    @Test
    void testCreateTopico_TurmaNotFound_ShouldThrowException() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            topicosService.createTopico(requestDto);
        });

        assertEquals("Turma com ID 1 não encontrada", exception.getMessage());
        verify(topicosRepository, never()).save(any());
    }

    @Test
    void testCreateTopico_AtividadeNotFound_ShouldThrowException() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turmaPadrao));
        when(topicosRepository.save(any(Topicos.class))).thenReturn(new Topicos());

        when(atividadeRepository.findById(10L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            topicosService.createTopico(requestDto);
        });

        assertEquals("Atividade com ID 10 nao encontrada", exception.getMessage());
    }

    @Test
    void testGetTopicoById_NotFound_ShouldThrowException() {
      
        when(topicosRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            topicosService.getTopicoById(99L);
        });

        assertEquals("Topico com ID 99 nao encontrado", exception.getMessage());
    }

    @Test
    void testGetAllTopicos() {
        Pageable pageable = Pageable.unpaged();
        Page<Topicos> page = new PageImpl<>(List.of(new Topicos()));
        when(topicosRepository.findAll(pageable)).thenReturn(page);

        PagedResponse<TopicosResponseDto> pagedResponse = mock(PagedResponse.class);
        when(pagedResponseMapper.toPagedResponse(page, TopicosResponseDto.class)).thenReturn(pagedResponse);

        PagedResponse<TopicosResponseDto> result = topicosService.getAllTopicos(pageable);

        assertNotNull(result);
        verify(topicosRepository).findAll(pageable);
        verify(pagedResponseMapper).toPagedResponse(page, TopicosResponseDto.class);
    }

    @Test
    void testGetTopicosByIdTurma() {
        Pageable pageable = Pageable.unpaged();
        Long idTurma = 1L;
        Page<Topicos> page = new PageImpl<>(List.of(new Topicos()));
        when(topicosRepository.findByTurmaIdTurma(idTurma, pageable)).thenReturn(page);

        PagedResponse<TopicosResponseDto> pagedResponse = mock(PagedResponse.class);
        when(pagedResponseMapper.toPagedResponse(page, TopicosResponseDto.class)).thenReturn(pagedResponse);

        PagedResponse<TopicosResponseDto> result = topicosService.getTopicosByIdTurma(idTurma, pageable);

        assertNotNull(result);
        verify(topicosRepository).findByTurmaIdTurma(idTurma, pageable);
        verify(pagedResponseMapper).toPagedResponse(page, TopicosResponseDto.class);
    }

    @Test
    void testDeleteTopico_Success() {
   
        Topicos topico = new Topicos();
        topico.setTurma(turmaPadrao);

        when(topicosRepository.findById(1L)).thenReturn(Optional.of(topico));

      
        when(autentificacaoService.getUsuarioLogado()).thenReturn(professorDono);

        doNothing().when(topicosRepository).delete(topico);
        when(modelMapper.map(topico, TopicosResponseDto.class)).thenReturn(new TopicosResponseDto());

        TopicosResponseDto result = topicosService.deleteTopico(1L);

        assertNotNull(result);
        verify(topicosRepository).findById(1L);
        verify(topicosRepository).delete(topico);
 
        verify(autentificacaoService).getUsuarioLogado();
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
        topicoExistente.setTurma(turmaPadrao); 

        TopicosResponseDto responseDto = new TopicosResponseDto();
        responseDto.setIdTopico(idTopico);

        when(topicosRepository.findById(idTopico)).thenReturn(Optional.of(topicoExistente));

       
        when(autentificacaoService.getUsuarioLogado()).thenReturn(professorDono);

        when(topicosRepository.save(any(Topicos.class))).thenReturn(topicoExistente);
        when(modelMapper.map(topicoExistente, TopicosResponseDto.class)).thenReturn(responseDto);

        topicosService.updateTopico(idTopico, updateDto);

        ArgumentCaptor<Topicos> captor = ArgumentCaptor.forClass(Topicos.class);
        verify(topicosRepository).save(captor.capture());

        Topicos topicoSalvo = captor.getValue();

        assertEquals("Título Novo", topicoSalvo.getTituloTopico());
        assertEquals(htmlLimpa, topicoSalvo.getConteudoHtml());

        verify(autentificacaoService).getUsuarioLogado();
    }
}
