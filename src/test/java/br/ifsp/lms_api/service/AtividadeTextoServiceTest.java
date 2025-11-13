package br.ifsp.lms_api.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.repository.AtividadeTextoRepository;

@ExtendWith(MockitoExtension.class)
class AtividadeTextoServiceTest {

    @Mock
    private AtividadeTextoRepository atividadeTextoRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private AtividadeTextoService atividadeTextoService;

    @Test
    void testCreateAtividadeTexto_Success() {
        AtividadeTextoRequestDto requestDto = new AtividadeTextoRequestDto();
        requestDto.setTituloAtividade("Nova Atividade");

        AtividadeTexto atividadeEntity = new AtividadeTexto(); 
        atividadeEntity.setTituloAtividade("Nova Atividade");

        AtividadeTexto savedEntity = new AtividadeTexto(); 
        savedEntity.setIdAtividade(1L);
        savedEntity.setTituloAtividade("Nova Atividade");

        AtividadeTextoResponseDto responseDto = new AtividadeTextoResponseDto(); 
        responseDto.setIdAtividade(1L);
        responseDto.setTituloAtividade("Nova Atividade");

        when(modelMapper.map(requestDto, AtividadeTexto.class)).thenReturn(atividadeEntity);
        when(atividadeTextoRepository.save(atividadeEntity)).thenReturn(savedEntity);
        when(modelMapper.map(savedEntity, AtividadeTextoResponseDto.class)).thenReturn(responseDto);

        AtividadeTextoResponseDto result = atividadeTextoService.createAtividadeTexto(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getIdAtividade());
        assertEquals("Nova Atividade", result.getTituloAtividade());

        verify(modelMapper).map(requestDto, AtividadeTexto.class);
        verify(atividadeTextoRepository).save(atividadeEntity);
        verify(modelMapper).map(savedEntity, AtividadeTextoResponseDto.class);
    }

    @Test
    void testGetAllAtividadesTexto_Success() {
        Pageable pageable = Pageable.unpaged();

        AtividadeTexto atividade = new AtividadeTexto();
        atividade.setIdAtividade(1L);
        Page<AtividadeTexto> atividadePage = new PageImpl<>(List.of(atividade), pageable, 1);
        
        PagedResponse<AtividadeTextoResponseDto> pagedResponseMock = mock(PagedResponse.class);

        when(atividadeTextoRepository.findAll(pageable)).thenReturn(atividadePage);
        when(pagedResponseMapper.toPagedResponse(atividadePage, AtividadeTextoResponseDto.class))
            .thenReturn(pagedResponseMock);

        PagedResponse<AtividadeTextoResponseDto> result = atividadeTextoService.getAllAtividadesTexto(pageable);

        assertNotNull(result);
        assertEquals(pagedResponseMock, result); 
        
        verify(atividadeTextoRepository).findAll(pageable);
        verify(pagedResponseMapper).toPagedResponse(atividadePage, AtividadeTextoResponseDto.class);
    }

    @Test
    void testGetAtividadeTextoById_Success() {
        Long id = 1L;
        AtividadeTexto atividadeEntity = new AtividadeTexto();
        atividadeEntity.setIdAtividade(id);
        
        AtividadeTextoResponseDto responseDto = new AtividadeTextoResponseDto();
        responseDto.setIdAtividade(id);

        when(atividadeTextoRepository.findById(id)).thenReturn(Optional.of(atividadeEntity));
        when(modelMapper.map(atividadeEntity, AtividadeTextoResponseDto.class)).thenReturn(responseDto);

        AtividadeTextoResponseDto result = atividadeTextoService.getAtividadeTextoById(id);

        assertNotNull(result);
        assertEquals(id, result.getIdAtividade());
        verify(atividadeTextoRepository).findById(id);
        verify(modelMapper).map(atividadeEntity, AtividadeTextoResponseDto.class);
    }

    @Test
    void testGetAtividadeTextoById_NotFound() {
        Long id = 99L;
        String expectedMessage = String.format("Atividade de Texto com ID %d não encontrada.", id);
        
        when(atividadeTextoRepository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            atividadeTextoService.getAtividadeTextoById(id);
        });

        assertEquals(expectedMessage, exception.getMessage());
        verify(atividadeTextoRepository).findById(id);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testUpdateAtividadeTexto_Success() {
        Long id = 1L;
        
AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Novo Título"));
        updateDto.setNumeroMaximoCaracteres(Optional.of(1000L)); 

        AtividadeTexto existingAtividade = new AtividadeTexto();
        existingAtividade.setIdAtividade(id);
        existingAtividade.setTituloAtividade("Título Antigo");
        existingAtividade.setNumeroMaximoCaracteres(Long.valueOf(500));
        existingAtividade.setDescricaoAtividade("Descrição Antiga");

        AtividadeTextoResponseDto responseDto = new AtividadeTextoResponseDto();
        responseDto.setIdAtividade(id);
        responseDto.setTituloAtividade("Novo Título");

        when(atividadeTextoRepository.findById(id)).thenReturn(Optional.of(existingAtividade));
        when(atividadeTextoRepository.save(any(AtividadeTexto.class))).thenReturn(existingAtividade); 
        when(modelMapper.map(existingAtividade, AtividadeTextoResponseDto.class)).thenReturn(responseDto);

        AtividadeTextoResponseDto result = atividadeTextoService.updateAtividadeTexto(id, updateDto);

        assertNotNull(result);
        assertEquals("Novo Título", result.getTituloAtividade());

        ArgumentCaptor<AtividadeTexto> atividadeCaptor = ArgumentCaptor.forClass(AtividadeTexto.class);
        verify(atividadeTextoRepository).save(atividadeCaptor.capture());
        
        AtividadeTexto capturedAtividade = atividadeCaptor.getValue();
        assertEquals("Novo Título", capturedAtividade.getTituloAtividade()); 
        assertEquals(1000L, capturedAtividade.getNumeroMaximoCaracteres()); 
        assertEquals("Descrição Antiga", capturedAtividade.getDescricaoAtividade()); 
        
        verify(atividadeTextoRepository).findById(id);
    }
    
    @Test
    void testDeleteAtividadeTexto_Success() {
        Long id = 1L;
        AtividadeTexto existingAtividade = new AtividadeTexto();
        existingAtividade.setIdAtividade(id);
        
        when(atividadeTextoRepository.findById(id)).thenReturn(Optional.of(existingAtividade));
        doNothing().when(atividadeTextoRepository).delete(existingAtividade); 

        assertDoesNotThrow(() -> {
            atividadeTextoService.deleteAtividadeTexto(id);
        });

        verify(atividadeTextoRepository).findById(id);
        verify(atividadeTextoRepository).delete(existingAtividade);
    }
    
    @Test
    void testDeleteAtividadeTexto_NotFound() {
        Long id = 99L;
        String expectedMessage = String.format("Atividade de Texto com ID %d não encontrada.", id);
        
        when(atividadeTextoRepository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            atividadeTextoService.deleteAtividadeTexto(id);
        });

        assertEquals(expectedMessage, exception.getMessage());
        verify(atividadeTextoRepository).findById(id);
        verify(atividadeTextoRepository, never()).delete(any());
    }
}
