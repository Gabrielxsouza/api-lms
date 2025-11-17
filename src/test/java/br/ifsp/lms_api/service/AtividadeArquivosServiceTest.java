package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;

import br.ifsp.lms_api.repository.AtividadeArquivosRepository;
import br.ifsp.lms_api.repository.TagRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.AtividadeArquivos;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Tag;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;

@ExtendWith(MockitoExtension.class)
public class AtividadeArquivosServiceTest {

    @Mock
    private AtividadeArquivosRepository atividadeArquivosRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private TopicosRepository topicosRepository;

    @Mock
    private PagedResponseMapper pagedResponseMapper;
    
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private AtividadeArquivosService atividadeArquivosService;

    private AtividadeArquivos atividadeArquivo; 
    private AtividadeArquivosRequestDto requestDto;
    private AtividadeArquivosResponseDto responseDto;
    private AtividadeArquivosUpdateDto updateDto; 
    private LocalDate dataInicio;
    private LocalDate dataFechamento;
    private Professor professorDono;
    private Topicos topico;
    private Turma turma;
    private Long idProfessorDono = 1L;
    private Long idOutroProfessor = 99L;

    @BeforeEach
    void setUp() {
        dataInicio = LocalDate.of(2025, 11, 1);
        dataFechamento = LocalDate.of(2025, 11, 30);
        
        professorDono = new Professor();
        professorDono.setIdUsuario(idProfessorDono);
        
        turma = new Turma();
        turma.setProfessor(professorDono);
        
        topico = new Topicos();
        topico.setTurma(turma);

        atividadeArquivo = new AtividadeArquivos();
        atividadeArquivo.setIdAtividade(1L);
        atividadeArquivo.setTituloAtividade("Trabalho de Java");
        atividadeArquivo.setDescricaoAtividade("Entregar um CRUD");
        atividadeArquivo.setDataInicioAtividade(dataInicio);
        atividadeArquivo.setDataFechamentoAtividade(dataFechamento);
        atividadeArquivo.setStatusAtividade(true);
        atividadeArquivo.setArquivosPermitidos(List.of(".pdf", ".zip"));
        atividadeArquivo.setTopico(topico);

        requestDto = new AtividadeArquivosRequestDto();
        requestDto.setTituloAtividade("Trabalho de Java");
        requestDto.setDescricaoAtividade("Entregar um CRUD");
        requestDto.setDataInicioAtividade(dataInicio);
        requestDto.setDataFechamentoAtividade(dataFechamento);
        requestDto.setStatusAtividade(true);
        requestDto.setArquivosPermitidos(List.of(".pdf", ".zip"));
        requestDto.setTagIds(List.of(1L, 2L));
        requestDto.setIdTopico(1L);

        responseDto = new AtividadeArquivosResponseDto();
        responseDto.setIdAtividade(1L);
        responseDto.setTituloAtividade("Trabalho de Java");
        responseDto.setDescricaoAtividade("Entregar um CRUD");
        responseDto.setDataInicioAtividade(dataInicio);
        responseDto.setDataFechamentoAtividade(dataFechamento);
        responseDto.setStatusAtividade(true);
        responseDto.setArquivosPermitidos(List.of(".pdf", ".zip"));

        updateDto = new AtividadeArquivosUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Trabalho de Java V2"));
        updateDto.setDescricaoAtividade(Optional.empty());
        updateDto.setDataInicioAtividade(Optional.empty());
        updateDto.setDataFechamentoAtividade(Optional.empty());
        updateDto.setStatusAtividade(Optional.empty());
        updateDto.setArquivosPermitidos(Optional.of(List.of(".pdf", ".docx")));
        updateDto.setTagIds(Optional.of(List.of(3L)));
    }

 @Test
    void testCreateAtividadeArquivos_Success() {
        Tag tag1 = new Tag(); tag1.setIdTag(1L);
        Tag tag2 = new Tag(); tag2.setIdTag(2L);
        List<Tag> tags = List.of(tag1, tag2);
        
        when(tagRepository.findAllById(requestDto.getTagIds())).thenReturn(tags);
        
        when(topicosRepository.findById(anyLong())).thenReturn(Optional.of(topico));
        
        AtividadeArquivos atividadeVazia = new AtividadeArquivos();
        when(modelMapper.map(requestDto, AtividadeArquivos.class)).thenReturn(atividadeVazia);
        
        when(atividadeArquivosRepository.save(any(AtividadeArquivos.class))).thenReturn(atividadeArquivo);
        
        when(modelMapper.map(atividadeArquivo, AtividadeArquivosResponseDto.class)).thenReturn(responseDto);

        AtividadeArquivosResponseDto result = atividadeArquivosService.createAtividadeArquivos(requestDto, idProfessorDono);

        assertNotNull(result);
        

        ArgumentCaptor<AtividadeArquivos> captor = ArgumentCaptor.forClass(AtividadeArquivos.class);
        verify(atividadeArquivosRepository, times(1)).save(captor.capture());
        
        assertEquals(2, captor.getValue().getTags().size()); 
    }

    @Test
    void testGetAllAtividadesArquivos_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AtividadeArquivos> entityPage = new PageImpl<>(List.of(atividadeArquivo), pageable, 1L);

        PagedResponse<AtividadeArquivosResponseDto> pagedResponse = new PagedResponse<>(
            List.of(responseDto), 0, 10, 1L, 1, true
        );

        when(atividadeArquivosRepository.findAll(pageable)).thenReturn(entityPage);
        when(pagedResponseMapper.toPagedResponse(entityPage, AtividadeArquivosResponseDto.class))
            .thenReturn(pagedResponse);

        PagedResponse<AtividadeArquivosResponseDto> result = atividadeArquivosService.getAllAtividadesArquivos(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(responseDto.getTituloAtividade(), result.getContent().get(0).getTituloAtividade());
        verify(atividadeArquivosRepository, times(1)).findAll(pageable);
        verify(pagedResponseMapper, times(1)).toPagedResponse(entityPage, AtividadeArquivosResponseDto.class);
    }

    @Test
    void testUpdateAtividadeArquivos_Success() {
        Long id = 1L;
        
        Tag tag3 = new Tag();
        tag3.setIdTag(3L);
        tag3.setNome("Nova Tag");
        List<Tag> newTags = List.of(tag3);
        
        AtividadeArquivosResponseDto updatedResponse = new AtividadeArquivosResponseDto();
        updatedResponse.setIdAtividade(id);
        updatedResponse.setTituloAtividade("Trabalho de Java V2"); 
        updatedResponse.setArquivosPermitidos(List.of(".pdf", ".docx")); 

        when(atividadeArquivosRepository.findById(id)).thenReturn(Optional.of(atividadeArquivo));
        when(tagRepository.findAllById(updateDto.getTagIds().get())).thenReturn(newTags);
        when(atividadeArquivosRepository.save(any(AtividadeArquivos.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(AtividadeArquivos.class), eq(AtividadeArquivosResponseDto.class)))
            .thenReturn(updatedResponse);

        // CORRIGIDO: Adicionado idProfessorDono
        AtividadeArquivosResponseDto result = atividadeArquivosService.updateAtividadeArquivos(id, updateDto, idProfessorDono);

        assertNotNull(result);
        assertEquals("Trabalho de Java V2", result.getTituloAtividade());
        assertEquals(".docx", result.getArquivosPermitidos().get(1));

        ArgumentCaptor<AtividadeArquivos> entityCaptor = ArgumentCaptor.forClass(AtividadeArquivos.class);
        verify(atividadeArquivosRepository, times(1)).save(entityCaptor.capture());

        AtividadeArquivos savedEntity = entityCaptor.getValue();
        assertEquals("Trabalho de Java V2", savedEntity.getTituloAtividade()); 
        assertEquals(List.of(".pdf", ".docx"), savedEntity.getArquivosPermitidos()); 
        assertEquals(1, savedEntity.getTags().size());
    }

    @Test
    void testUpdateAtividadeArquivos_AccessDenied() {
        Long id = 1L;
        when(atividadeArquivosRepository.findById(id)).thenReturn(Optional.of(atividadeArquivo));
        
        assertThrows(AccessDeniedException.class, () -> {
            // CORRIGIDO: Adicionado idOutroProfessor
            atividadeArquivosService.updateAtividadeArquivos(id, updateDto, idOutroProfessor);
        });
        
        verify(atividadeArquivosRepository, never()).save(any());
    }


    @Test
    void testUpdateAtividadeArquivos_NotFound() {
        Long id = 1L;
        when(atividadeArquivosRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            // CORRIGIDO: Adicionado idProfessorDono
            atividadeArquivosService.updateAtividadeArquivos(id, updateDto, idProfessorDono);
        });

        verify(atividadeArquivosRepository, times(1)).findById(id);
        verify(atividadeArquivosRepository, never()).save(any());
    }

    @Test
    void testDeleteAtividadeArquivos_Success() {
        Long id = 1L;
        when(atividadeArquivosRepository.findById(id)).thenReturn(Optional.of(atividadeArquivo));
        doNothing().when(atividadeArquivosRepository).delete(atividadeArquivo);

        atividadeArquivosService.deleteAtividadeArquivos(id, idProfessorDono);

        verify(atividadeArquivosRepository, times(1)).findById(id);
        verify(atividadeArquivosRepository, times(1)).delete(atividadeArquivo);
    }

    @Test
    void testDeleteAtividadeArquivos_NotFound() {
        Long id = 1L;
        when(atividadeArquivosRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            atividadeArquivosService.deleteAtividadeArquivos(id, idProfessorDono);
        });

        verify(atividadeArquivosRepository, times(1)).findById(id);
        verify(atividadeArquivosRepository, never()).delete(any());
    }
}