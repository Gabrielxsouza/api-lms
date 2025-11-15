package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import org.springframework.data.domain.Pageable;

import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.model.Tag;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.repository.AtividadeTextoRepository;
import br.ifsp.lms_api.repository.TagRepository; // 1. MOCK FALTANDO
import br.ifsp.lms_api.repository.TopicosRepository; // 2. MOCK FALTANDO

@ExtendWith(MockitoExtension.class)
class AtividadeTextoServiceTest {

    @Mock
    private AtividadeTextoRepository atividadeTextoRepository;

    @Mock
    private TopicosRepository topicosRepository; // ADICIONADO

    @Mock
    private TagRepository tagRepository; // ADICIONADO

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private AtividadeTextoService atividadeTextoService;

    // --- Objetos de Teste ---
    private AtividadeTextoRequestDto requestDto;
    private AtividadeTexto atividadeEntity;
    private AtividadeTextoResponseDto responseDto;
    private Topicos topico;
    private Tag tag;

    @BeforeEach
    void setUp() {
        // Mock de Entidades
        topico = new Topicos();
        topico.setIdTopico(1L);

        tag = new Tag();
        tag.setIdTag(1L);
        tag.setNome("Cálculo");

        atividadeEntity = new AtividadeTexto();
        atividadeEntity.setIdAtividade(1L);
        atividadeEntity.setTituloAtividade("Nova Atividade");
        atividadeEntity.setNumeroMaximoCaracteres(1000L); // 3. CORRIGIDO (usando Long)
        atividadeEntity.setTopico(topico);
        atividadeEntity.setTags(Set.of(tag));

        // Mock de DTOs
        requestDto = new AtividadeTextoRequestDto();
        requestDto.setTituloAtividade("Nova Atividade");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        requestDto.setStatusAtividade(true);
        requestDto.setNumeroMaximoCaracteres(1000L); // 3. CORRIGIDO (usando Long)
        requestDto.setTagIds(List.of(1L));
        
        responseDto = new AtividadeTextoResponseDto();
        responseDto.setIdAtividade(1L);
        responseDto.setTituloAtividade("Nova Atividade");
        responseDto.setNumeroMaximoCaracteres(1000L); // 3. CORRIGIDO (usando Long)
    }

    @Test
    void testCreateAtividadeTexto_Success() {
        // Arrange
        // (Isso testa a lógica de mapeamento manual)
        when(topicosRepository.findById(1L)).thenReturn(Optional.of(topico));
        when(tagRepository.findAllById(anyList())).thenReturn(List.of(tag));
        
        // Mock do save (captura o objeto antes de salvar)
        ArgumentCaptor<AtividadeTexto> atividadeCaptor = ArgumentCaptor.forClass(AtividadeTexto.class);
        when(atividadeTextoRepository.save(atividadeCaptor.capture())).thenReturn(atividadeEntity);
        
        when(modelMapper.map(atividadeEntity, AtividadeTextoResponseDto.class)).thenReturn(responseDto);

        // Act
        AtividadeTextoResponseDto result = atividadeTextoService.createAtividadeTexto(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdAtividade());
        assertEquals("Nova Atividade", result.getTituloAtividade());
        assertEquals(1000L, result.getNumeroMaximoCaracteres()); // Verifica o campo corrigido

        // Verifica se a entidade foi montada corretamente ANTES de salvar
        AtividadeTexto capturedAtividade = atividadeCaptor.getValue();
        assertNull(capturedAtividade.getIdAtividade()); // Confirma que setId(null) foi chamado
        assertEquals(topico, capturedAtividade.getTopico()); // Confirma que o Tópico foi vinculado
        assertTrue(capturedAtividade.getTags().contains(tag)); // Confirma que a Tag foi vinculada
        assertEquals(1000L, capturedAtividade.getNumeroMaximoCaracteres()); // Confirma que o campo foi mapeado

        // Verify
        verify(topicosRepository, times(1)).findById(1L);
        verify(tagRepository, times(1)).findAllById(anyList());
        verify(atividadeTextoRepository, times(1)).save(any(AtividadeTexto.class));
        verify(modelMapper, times(1)).map(atividadeEntity, AtividadeTextoResponseDto.class);
    }

    @Test
    void testCreateAtividadeTexto_TopicoNotFound() {
        // Arrange
        when(topicosRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            atividadeTextoService.createAtividadeTexto(requestDto);
        });

        // Garante que nada foi salvo se o tópico não existe
        verify(atividadeTextoRepository, never()).save(any());
    }

    @Test
    void testGetAllAtividadesTexto_Success() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Page<AtividadeTexto> atividadePage = new PageImpl<>(List.of(atividadeEntity), pageable, 1);
        PagedResponse<AtividadeTextoResponseDto> pagedResponseMock = new PagedResponse<>(
            List.of(responseDto), 0, 1, 1L, 1, true
        );

        when(atividadeTextoRepository.findAll(pageable)).thenReturn(atividadePage);
        when(pagedResponseMapper.toPagedResponse(atividadePage, AtividadeTextoResponseDto.class))
            .thenReturn(pagedResponseMock);

        // Act
        PagedResponse<AtividadeTextoResponseDto> result = atividadeTextoService.getAllAtividadesTexto(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1000L, result.getContent().get(0).getNumeroMaximoCaracteres());
    }

    @Test
    void testGetAtividadeTextoById_Success() {
        // Arrange
        when(atividadeTextoRepository.findById(1L)).thenReturn(Optional.of(atividadeEntity));
        when(modelMapper.map(atividadeEntity, AtividadeTextoResponseDto.class)).thenReturn(responseDto);

        // Act
        AtividadeTextoResponseDto result = atividadeTextoService.getAtividadeTextoById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdAtividade());
    }
    
    @Test
    void testUpdateAtividadeTexto_Success() {
        // Arrange
        Long id = 1L;
        
        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Novo Título"));
        updateDto.setNumeroMaximoCaracteres(Optional.of(2000L)); // 3. CORRIGIDO (usando Long)
        updateDto.setTagIds(Optional.of(List.of())); // Testando a remoção de tags

        when(atividadeTextoRepository.findById(id)).thenReturn(Optional.of(atividadeEntity));
        when(atividadeTextoRepository.save(any(AtividadeTexto.class))).thenReturn(atividadeEntity);
        
        AtividadeTextoResponseDto updatedResponseDto = new AtividadeTextoResponseDto();
        updatedResponseDto.setTituloAtividade("Novo Título");
        updatedResponseDto.setNumeroMaximoCaracteres(2000L); // 3. CORRIGIDO (usando Long)
        
        when(modelMapper.map(atividadeEntity, AtividadeTextoResponseDto.class)).thenReturn(updatedResponseDto);

        // Act
        AtividadeTextoResponseDto result = atividadeTextoService.updateAtividadeTexto(id, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Novo Título", result.getTituloAtividade());
        assertEquals(2000L, result.getNumeroMaximoCaracteres());

        // Verifica (com ArgumentCaptor) se a lógica do applyUpdateFromDto funcionou
        ArgumentCaptor<AtividadeTexto> atividadeCaptor = ArgumentCaptor.forClass(AtividadeTexto.class);
        verify(atividadeTextoRepository).save(atividadeCaptor.capture());
        
        AtividadeTexto capturedAtividade = atividadeCaptor.getValue();
        assertEquals("Novo Título", capturedAtividade.getTituloAtividade()); 
        assertEquals(2000L, capturedAtividade.getNumeroMaximoCaracteres()); 
        assertTrue(capturedAtividade.getTags().isEmpty()); // Verifica se as tags foram limpas
    }
    
    @Test
    void testDeleteAtividadeTexto_Success() {
        // Arrange
        when(atividadeTextoRepository.findById(1L)).thenReturn(Optional.of(atividadeEntity));
        doNothing().when(atividadeTextoRepository).delete(atividadeEntity); 

        // Act & Assert
        assertDoesNotThrow(() -> {
            atividadeTextoService.deleteAtividadeTexto(1L);
        });

        verify(atividadeTextoRepository).delete(atividadeEntity);
    }
}