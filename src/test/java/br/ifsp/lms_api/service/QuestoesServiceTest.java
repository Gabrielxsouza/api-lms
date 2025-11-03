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

    @Mock
    private QuestoesRepository questoesRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private QuestoesService questoesService;

    @Test
    void testCreateQuestao_Success() {
        
        QuestoesRequestDto requestDto = new QuestoesRequestDto();

            Alternativas alt1 = new Alternativas();
        alt1.setAlternativa("Alternativa 1"); 
        List<Alternativas> alternativasList = new ArrayList<>(List.of(alt1));   
        
        Questoes questaoEntity = new Questoes();
        questaoEntity.setEnunciado("Enunciado da Questão");
        questaoEntity.setAlternativas(alternativasList); 

        Questoes savedQuestaoEntity = new Questoes();
        savedQuestaoEntity.setIdQuestao(1L);
        savedQuestaoEntity.setEnunciado("Enunciado da Questão");
        savedQuestaoEntity.setAlternativas(alternativasList);

        QuestoesResponseDto responseDto = new QuestoesResponseDto();
        responseDto.setIdQuestao(1L);
        responseDto.setEnunciado("Enunciado da Questão");

        when(modelMapper.map(requestDto, Questoes.class)).thenReturn(questaoEntity);
        when(questoesRepository.save(any(Questoes.class))).thenReturn(savedQuestaoEntity);
        when(modelMapper.map(savedQuestaoEntity, QuestoesResponseDto.class)).thenReturn(responseDto);

        QuestoesResponseDto result = questoesService.createQuestao(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getIdQuestao());

        assertNotNull(questaoEntity.getAlternativas().get(0).getQuestoes());
        assertEquals(questaoEntity, questaoEntity.getAlternativas().get(0).getQuestoes());
        
        verify(modelMapper, times(1)).map(requestDto, Questoes.class);
        verify(questoesRepository, times(1)).save(questaoEntity);
        verify(modelMapper, times(1)).map(savedQuestaoEntity, QuestoesResponseDto.class);
    }
    
    @Test
    void testGetAllQuestoes_Success() {
        Pageable pageable = Pageable.unpaged(); 
        
        Questoes questao = new Questoes();
        questao.setIdQuestao(1L);
        Page<Questoes> questoesPage = new PageImpl<>(List.of(questao), pageable, 1);
        
      PagedResponse<QuestoesResponseDto> pagedResponseMock = mock(PagedResponse.class);

        when(questoesRepository.findAll(pageable)).thenReturn(questoesPage);
        when(pagedResponseMapper.toPagedResponse(questoesPage, QuestoesResponseDto.class))
            .thenReturn(pagedResponseMock);

        PagedResponse<QuestoesResponseDto> result = questoesService.getAllQuestoes(pageable);

        assertNotNull(result);
        assertEquals(pagedResponseMock, result); 
        
        verify(questoesRepository, times(1)).findAll(pageable);
        verify(pagedResponseMapper, times(1)).toPagedResponse(questoesPage, QuestoesResponseDto.class);
    }

    @Test
    void testUpdateQuestao_WhenNotFound_ShouldThrowException() {
        Long idInexistente = 99L;
        QuestoesUpdateDto updateDto = new QuestoesUpdateDto(); 
        updateDto.setEnunciado(Optional.of("Novo Enunciado"));
        
        String expectedMessage = "Questão not found with id: " + idInexistente;

        when(questoesRepository.findById(idInexistente)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            questoesService.updateQuestao(idInexistente, updateDto);
        });

        assertEquals(expectedMessage, exception.getMessage());
        
        verify(questoesRepository, times(1)).findById(idInexistente);
        
        verify(questoesRepository, never()).save(any());
    }

    @Test
    void testDeleteQuestao_Success() {
        Long idExistente = 1L;
        Questoes questaoMock = new Questoes(); 
        questaoMock.setIdQuestao(idExistente);

        when(questoesRepository.findById(idExistente)).thenReturn(Optional.of(questaoMock));
        
        doNothing().when(questoesRepository).delete(questaoMock);

        assertDoesNotThrow(() -> {
            questoesService.deleteQuestao(idExistente);
        });

        verify(questoesRepository, times(1)).findById(idExistente);
        
        verify(questoesRepository, times(1)).delete(questaoMock);
    }

    @Test
    void testDeleteQuestao_WhenNotFound_ShouldThrowException() {
        Long idInexistente = 99L;
        String expectedMessage = "Questão not found with id: " + idInexistente;

        when(questoesRepository.findById(idInexistente)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            questoesService.deleteQuestao(idInexistente);
        });

        assertEquals(expectedMessage, exception.getMessage());
        
        verify(questoesRepository, times(1)).findById(idInexistente);
        
        verify(questoesRepository, never()).delete(any());
    }
}
