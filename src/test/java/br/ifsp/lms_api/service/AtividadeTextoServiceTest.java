// Salve este arquivo em:
// src/test/java/br/ifsp/lms_api/service/AtividadeTextoServiceTest.java

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

    // 1. Crie mocks para TODAS as dependências do Service
    @Mock
    private AtividadeTextoRepository atividadeTextoRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    // 2. Injete os mocks na classe que estamos testando
    @InjectMocks
    private AtividadeTextoService atividadeTextoService;

    @Test
    void testCreateAtividadeTexto_Success() {
        // --- 1. Arrange (Arrumar) ---
        AtividadeTextoRequestDto requestDto = new AtividadeTextoRequestDto();
        requestDto.setTituloAtividade("Nova Atividade");

        AtividadeTexto atividadeEntity = new AtividadeTexto(); // O que o mapper(dto) retorna
        atividadeEntity.setTituloAtividade("Nova Atividade");

        AtividadeTexto savedEntity = new AtividadeTexto(); // O que o repository.save() retorna
        savedEntity.setIdAtividade(1L);
        savedEntity.setTituloAtividade("Nova Atividade");

        AtividadeTextoResponseDto responseDto = new AtividadeTextoResponseDto(); // O que o mapper(entity) retorna
        responseDto.setIdAtividade(1L);
        responseDto.setTituloAtividade("Nova Atividade");

        // Configura os mocks
        when(modelMapper.map(requestDto, AtividadeTexto.class)).thenReturn(atividadeEntity);
        when(atividadeTextoRepository.save(atividadeEntity)).thenReturn(savedEntity);
        when(modelMapper.map(savedEntity, AtividadeTextoResponseDto.class)).thenReturn(responseDto);

        // --- 2. Act (Agir) ---
        AtividadeTextoResponseDto result = atividadeTextoService.createAtividadeTexto(requestDto);

        // --- 3. Assert (Verificar) ---
        assertNotNull(result);
        assertEquals(1L, result.getIdAtividade());
        assertEquals("Nova Atividade", result.getTituloAtividade());

        verify(modelMapper).map(requestDto, AtividadeTexto.class);
        verify(atividadeTextoRepository).save(atividadeEntity);
        verify(modelMapper).map(savedEntity, AtividadeTextoResponseDto.class);
    }

    @Test
    void testGetAllAtividadesTexto_Success() {
        // --- 1. Arrange (Arrumar) ---
        Pageable pageable = Pageable.unpaged();

        // 1. O que o repositório retorna
        AtividadeTexto atividade = new AtividadeTexto();
        atividade.setIdAtividade(1L);
        Page<AtividadeTexto> atividadePage = new PageImpl<>(List.of(atividade), pageable, 1);
        
        // 2. O que o pagedResponseMapper deve retornar (o MOCK 100% FALSO)
        PagedResponse<AtividadeTextoResponseDto> pagedResponseMock = mock(PagedResponse.class);

        // Configura os mocks
        when(atividadeTextoRepository.findAll(pageable)).thenReturn(atividadePage);
        when(pagedResponseMapper.toPagedResponse(atividadePage, AtividadeTextoResponseDto.class))
            .thenReturn(pagedResponseMock);

        // --- 2. Act (Agir) ---
        PagedResponse<AtividadeTextoResponseDto> result = atividadeTextoService.getAllAtividadesTexto(pageable);

        // --- 3. Assert (Verificar) ---
        assertNotNull(result);
        // Verifica se o resultado é exatamente o objeto mockado que o mapper "retornou"
        assertEquals(pagedResponseMock, result); 
        
        verify(atividadeTextoRepository).findAll(pageable);
        verify(pagedResponseMapper).toPagedResponse(atividadePage, AtividadeTextoResponseDto.class);
    }

    @Test
    void testGetAtividadeTextoById_Success() {
        // --- 1. Arrange (Arrumar) ---
        Long id = 1L;
        AtividadeTexto atividadeEntity = new AtividadeTexto();
        atividadeEntity.setIdAtividade(id);
        
        AtividadeTextoResponseDto responseDto = new AtividadeTextoResponseDto();
        responseDto.setIdAtividade(id);

        when(atividadeTextoRepository.findById(id)).thenReturn(Optional.of(atividadeEntity));
        when(modelMapper.map(atividadeEntity, AtividadeTextoResponseDto.class)).thenReturn(responseDto);

        // --- 2. Act (Agir) ---
        AtividadeTextoResponseDto result = atividadeTextoService.getAtividadeTextoById(id);

        // --- 3. Assert (Verificar) ---
        assertNotNull(result);
        assertEquals(id, result.getIdAtividade());
        verify(atividadeTextoRepository).findById(id);
        verify(modelMapper).map(atividadeEntity, AtividadeTextoResponseDto.class);
    }

    @Test
    void testGetAtividadeTextoById_NotFound() {
        // --- 1. Arrange (Arrumar) ---
        Long id = 99L;
        String expectedMessage = String.format("Atividade de Texto com ID %d não encontrada.", id);
        
        when(atividadeTextoRepository.findById(id)).thenReturn(Optional.empty());

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            atividadeTextoService.getAtividadeTextoById(id);
        });

        assertEquals(expectedMessage, exception.getMessage());
        verify(atividadeTextoRepository).findById(id);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testUpdateAtividadeTexto_Success() {
        // --- 1. Arrange (Arrumar) ---
        Long id = 1L;
        
        // DTO de requisição (COM A CORREÇÃO)
AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Novo Título"));
        updateDto.setNumeroMaximoCaracteres(Optional.of(1000L)); // <-- CORRIGIDO (de Optional.of(1000) para 1000L)
        // updateDto.setDescricaoAtividade(null); // (não precisamos setar, pois o getter retornará Optional.empty())

        // Entidade que o findById() retorna
        AtividadeTexto existingAtividade = new AtividadeTexto();
        existingAtividade.setIdAtividade(id);
        existingAtividade.setTituloAtividade("Título Antigo");
        existingAtividade.setNumeroMaximoCaracteres(500); // Vamos assumir que é Long ou Integer
        existingAtividade.setDescricaoAtividade("Descrição Antiga");

        // DTO de resposta
        AtividadeTextoResponseDto responseDto = new AtividadeTextoResponseDto();
        responseDto.setIdAtividade(id);
        responseDto.setTituloAtividade("Novo Título");

        // Simula o service retornando Optional.ofNullable(null) para os campos não preenchidos
        when(atividadeTextoRepository.findById(id)).thenReturn(Optional.of(existingAtividade));
        when(atividadeTextoRepository.save(any(AtividadeTexto.class))).thenReturn(existingAtividade); // Retorna a entidade atualizada
        when(modelMapper.map(existingAtividade, AtividadeTextoResponseDto.class)).thenReturn(responseDto);

        // --- 2. Act (Agir) ---
        AtividadeTextoResponseDto result = atividadeTextoService.updateAtividadeTexto(id, updateDto);

        // --- 3. Assert (Verificar) ---
        assertNotNull(result);
        assertEquals("Novo Título", result.getTituloAtividade());

        // Verifica se a lógica do 'applyUpdateFromDto' (método privado) funcionou:
        ArgumentCaptor<AtividadeTexto> atividadeCaptor = ArgumentCaptor.forClass(AtividadeTexto.class);
        verify(atividadeTextoRepository).save(atividadeCaptor.capture());
        
        AtividadeTexto capturedAtividade = atividadeCaptor.getValue();
        assertEquals("Novo Título", capturedAtividade.getTituloAtividade()); // Mudou
        assertEquals(1000L, capturedAtividade.getNumeroMaximoCaracteres()); // Mudou (use 1000L para ser Long)
        assertEquals("Descrição Antiga", capturedAtividade.getDescricaoAtividade()); // Permaneceu
        
        verify(atividadeTextoRepository).findById(id);
    }
    
    @Test
    void testDeleteAtividadeTexto_Success() {
        // --- 1. Arrange (Arrumar) ---
        Long id = 1L;
        AtividadeTexto existingAtividade = new AtividadeTexto();
        existingAtividade.setIdAtividade(id);
        
        when(atividadeTextoRepository.findById(id)).thenReturn(Optional.of(existingAtividade));
        doNothing().when(atividadeTextoRepository).delete(existingAtividade); // Para métodos 'void'

        // --- 2. Act (Agir) ---
        // Verifica se o método executa sem lançar exceções
        assertDoesNotThrow(() -> {
            atividadeTextoService.deleteAtividadeTexto(id);
        });

        // --- 3. Assert (Verificar) ---
        verify(atividadeTextoRepository).findById(id);
        verify(atividadeTextoRepository).delete(existingAtividade);
    }
    
    @Test
    void testDeleteAtividadeTexto_NotFound() {
        // --- 1. Arrange (Arrumar) ---
        Long id = 99L;
        String expectedMessage = String.format("Atividade de Texto com ID %d não encontrada.", id);
        
        when(atividadeTextoRepository.findById(id)).thenReturn(Optional.empty());

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            atividadeTextoService.deleteAtividadeTexto(id);
        });

        assertEquals(expectedMessage, exception.getMessage());
        verify(atividadeTextoRepository).findById(id);
        verify(atividadeTextoRepository, never()).delete(any());
    }
}