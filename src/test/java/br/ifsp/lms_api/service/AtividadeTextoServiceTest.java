package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Tag;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.AtividadeTextoRepository;
import br.ifsp.lms_api.repository.TagRepository;

@ExtendWith(MockitoExtension.class)
class AtividadeTextoServiceTest {

    @Mock
    private AtividadeTextoRepository atividadeTextoRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private AtividadeTextoService atividadeTextoService;


    private AtividadeTextoRequestDto requestDto;
    private AtividadeTexto atividadeEntity;
    private AtividadeTextoResponseDto responseDto;
    private Topicos topico;
    private Turma turma;
    private Professor professorMock;
    private Tag tag;

    private final Long USER_ID_VALIDO = 50L;

    @BeforeEach
    void setUp() {

        professorMock = mock(Professor.class);

        lenient().when(professorMock.getIdUsuario()).thenReturn(USER_ID_VALIDO);

        turma = new Turma();
        turma.setProfessor(professorMock);

        topico = new Topicos();
        topico.setIdTopico(1L);
        topico.setTurma(turma);

        tag = new Tag();
        tag.setIdTag(1L);
        tag.setNome("Cálculo");

        atividadeEntity = new AtividadeTexto();
        atividadeEntity.setIdAtividade(1L);
        atividadeEntity.setTituloAtividade("Nova Atividade");
        atividadeEntity.setNumeroMaximoCaracteres(1000L);
        atividadeEntity.setTopico(topico);
        atividadeEntity.setTags(new HashSet<>(Set.of(tag)));

        requestDto = new AtividadeTextoRequestDto();
        requestDto.setTituloAtividade("Nova Atividade");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        requestDto.setStatusAtividade(true);
        requestDto.setNumeroMaximoCaracteres(1000L);
        requestDto.setTagIds(List.of(1L));

        responseDto = new AtividadeTextoResponseDto();
        responseDto.setIdAtividade(1L);
        responseDto.setTituloAtividade("Nova Atividade");
        responseDto.setNumeroMaximoCaracteres(1000L);
    }

    @Test
    void testCreateAtividadeTexto_Success() {

        when(modelMapper.map(requestDto, AtividadeTexto.class)).thenReturn(atividadeEntity);
        when(tagRepository.findAllById(anyList())).thenReturn(List.of(tag));
        when(atividadeTextoRepository.save(any(AtividadeTexto.class))).thenReturn(atividadeEntity);
        when(modelMapper.map(atividadeEntity, AtividadeTextoResponseDto.class)).thenReturn(responseDto);

        AtividadeTextoResponseDto result = atividadeTextoService.createAtividadeTexto(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getIdAtividade());
        assertEquals("Nova Atividade", result.getTituloAtividade());

        verify(tagRepository).findAllById(anyList());
        verify(atividadeTextoRepository).save(atividadeEntity);
    }

    @Test
    void testGetAllAtividadesTexto_Success() {

        Pageable pageable = Pageable.unpaged();
        Page<AtividadeTexto> atividadePage = new PageImpl<>(List.of(atividadeEntity), pageable, 1);
        PagedResponse<AtividadeTextoResponseDto> pagedResponseMock = new PagedResponse<>(
            List.of(responseDto), 0, 1, 1L, 1, true
        );

        when(atividadeTextoRepository.findAll(pageable)).thenReturn(atividadePage);
        when(pagedResponseMapper.toPagedResponse(atividadePage, AtividadeTextoResponseDto.class))
            .thenReturn(pagedResponseMock);

        PagedResponse<AtividadeTextoResponseDto> result = atividadeTextoService.getAllAtividadesTexto(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetAtividadeTextoById_Success() {

        when(atividadeTextoRepository.findById(1L)).thenReturn(Optional.of(atividadeEntity));
        when(modelMapper.map(atividadeEntity, AtividadeTextoResponseDto.class)).thenReturn(responseDto);

        AtividadeTextoResponseDto result = atividadeTextoService.getAtividadeTextoById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdAtividade());
    }

    @Test
    void testGetAtividadeTextoById_NotFound() {

        Long idInexistente = 99L;
        when(atividadeTextoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            atividadeTextoService.getAtividadeTextoById(idInexistente);
        });
    }

    @Test
    void testUpdateAtividadeTexto_Success() {

        Long idAtividade = 1L;

        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Novo Título"));
        updateDto.setNumeroMaximoCaracteres(Optional.of(2000L));
        updateDto.setTagIds(Optional.of(List.of()));

        when(atividadeTextoRepository.findById(idAtividade)).thenReturn(Optional.of(atividadeEntity));

        when(atividadeTextoRepository.save(any(AtividadeTexto.class))).thenReturn(atividadeEntity);

        AtividadeTextoResponseDto updatedResponseDto = new AtividadeTextoResponseDto();
        updatedResponseDto.setTituloAtividade("Novo Título");
        updatedResponseDto.setNumeroMaximoCaracteres(2000L);
        when(modelMapper.map(atividadeEntity, AtividadeTextoResponseDto.class)).thenReturn(updatedResponseDto);

        AtividadeTextoResponseDto result = atividadeTextoService.updateAtividadeTexto(idAtividade, updateDto, USER_ID_VALIDO);

        assertNotNull(result);
        assertEquals("Novo Título", result.getTituloAtividade());
        assertEquals(2000L, result.getNumeroMaximoCaracteres());

        verify(atividadeTextoRepository).save(atividadeEntity);
        assertTrue(atividadeEntity.getTags().isEmpty());

        verify(professorMock).getIdUsuario();
    }

    @Test
    void testUpdateAtividadeTexto_AccessDenied() {

        Long idAtividade = 1L;
        Long usuarioInvalido = 999L;
        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();

        when(atividadeTextoRepository.findById(idAtividade)).thenReturn(Optional.of(atividadeEntity));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            atividadeTextoService.updateAtividadeTexto(idAtividade, updateDto, usuarioInvalido);
        });

        assertEquals("Acesso negado", exception.getMessage());
        verify(atividadeTextoRepository, never()).save(any());
    }

    @Test
    void testDeleteAtividadeTexto_Success() {

        when(atividadeTextoRepository.findById(1L)).thenReturn(Optional.of(atividadeEntity));
        doNothing().when(atividadeTextoRepository).delete(atividadeEntity);

        assertDoesNotThrow(() -> {
            atividadeTextoService.deleteAtividadeTexto(1L);
        });

        verify(atividadeTextoRepository).delete(atividadeEntity);
    }
}
