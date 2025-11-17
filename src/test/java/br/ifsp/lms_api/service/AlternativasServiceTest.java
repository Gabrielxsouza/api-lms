package br.ifsp.lms_api.service;

import br.ifsp.lms_api.dto.alternativasDto.AlternativasRequestDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasResponseDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.repository.AlternativasRepository;
import br.ifsp.lms_api.repository.QuestoesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlternativasServiceTest {

    @Mock
    private AlternativasRepository alternativasRepository;

    @Mock
    private QuestoesRepository questoesRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private AlternativasService alternativasService;


    @Test
    @DisplayName("Deve criar uma alternativa com sucesso (Happy Path)")
    void shouldCreateAlternativaSuccessfully() {

        Long idQuestao = 1L;
        AlternativasRequestDto requestDto = new AlternativasRequestDto();
        requestDto.setIdQuestao(idQuestao);
        requestDto.setAlternativa("Texto da alternativa");

        Questoes questao = new Questoes();
        questao.setIdQuestao(idQuestao);

        Alternativas alternativaEntity = new Alternativas();
        alternativaEntity.setAlternativa("Texto da alternativa");

        Alternativas savedAlternativa = new Alternativas();
        savedAlternativa.setIdAlternativa(10L);
        savedAlternativa.setAlternativa("Texto da alternativa");
        savedAlternativa.setQuestoes(questao);

        AlternativasResponseDto responseDto = new AlternativasResponseDto();
        responseDto.setIdAlternativa(10L);
        responseDto.setAlternativa("Texto da alternativa");

        when(questoesRepository.findById(idQuestao)).thenReturn(Optional.of(questao));
        when(modelMapper.map(requestDto, Alternativas.class)).thenReturn(alternativaEntity);
        when(alternativasRepository.save(any(Alternativas.class))).thenReturn(savedAlternativa);
        when(modelMapper.map(savedAlternativa, AlternativasResponseDto.class)).thenReturn(responseDto);

        AlternativasResponseDto result = alternativasService.createAlternativa(requestDto);

        assertNotNull(result);
        assertEquals(10L, result.getIdAlternativa());
        assertEquals("Texto da alternativa", result.getAlternativa());

        verify(questoesRepository).findById(idQuestao);
        verify(alternativasRepository).save(any(Alternativas.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar alternativa para questão inexistente (Sad Path)")
    void shouldThrowExceptionWhenCreatingForNonExistentQuestao() {

        Long idQuestao = 99L;
        AlternativasRequestDto requestDto = new AlternativasRequestDto();
        requestDto.setIdQuestao(idQuestao);

        when(questoesRepository.findById(idQuestao)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            alternativasService.createAlternativa(requestDto);
        });

        assertTrue(exception.getMessage().contains("Questão não encontrada"));
        verify(alternativasRepository, never()).save(any());
    }


    @Test
    @DisplayName("Deve retornar todas as alternativas paginadas (Happy Path)")
    void shouldGetAllAlternativas() {
     
        Pageable pageable = PageRequest.of(0, 10);
        Alternativas alternativa = new Alternativas();
        List<Alternativas> lista = Collections.singletonList(alternativa);
        Page<Alternativas> page = new PageImpl<>(lista);
        PagedResponse<AlternativasResponseDto> pagedResponse = new PagedResponse<AlternativasResponseDto>(null, 0, 0, 0, 0, false);

        when(alternativasRepository.findAll(pageable)).thenReturn(page);
        when(pagedResponseMapper.toPagedResponse(page, AlternativasResponseDto.class)).thenReturn(pagedResponse);

        PagedResponse<AlternativasResponseDto> result = alternativasService.getAllAlternativas(pageable);

        assertNotNull(result);
        verify(alternativasRepository).findAll(pageable);
        verify(pagedResponseMapper).toPagedResponse(page, AlternativasResponseDto.class);
    }

    @Test
    @DisplayName("Deve retornar alternativa por ID (Happy Path)")
    void shouldGetAlternativaById() {

        Long id = 1L;
        Alternativas alternativa = new Alternativas();
        alternativa.setIdAlternativa(id);

        AlternativasResponseDto responseDto = new AlternativasResponseDto();
        responseDto.setIdAlternativa(id);

        when(alternativasRepository.findById(id)).thenReturn(Optional.of(alternativa));
        when(modelMapper.map(alternativa, AlternativasResponseDto.class)).thenReturn(responseDto);

        AlternativasResponseDto result = alternativasService.getAlternativaById(id);

        assertNotNull(result);
        assertEquals(id, result.getIdAlternativa());
        verify(alternativasRepository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar ID inexistente (Sad Path)")
    void shouldThrowExceptionWhenGetAlternativaNotFound() {

        Long id = 99L;
        when(alternativasRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            alternativasService.getAlternativaById(id);
        });
    }


    @Test
    @DisplayName("Deve atualizar alternativa existente (Happy Path)")
    void shouldUpdateAlternativaSuccessfully() {

        Long id = 1L;

        AlternativasUpdateDto updateDto = new AlternativasUpdateDto();
        updateDto.setAlternativa(Optional.of("Novo Texto"));
        updateDto.setAlternativaCorreta(Optional.of(true));

        Alternativas existingAlternativa = new Alternativas();
        existingAlternativa.setIdAlternativa(id);
        existingAlternativa.setAlternativa("Texto Antigo");
        existingAlternativa.setAlternativaCorreta(false);

        Alternativas updatedAlternativa = new Alternativas();
        updatedAlternativa.setIdAlternativa(id);
        updatedAlternativa.setAlternativa("Novo Texto");
        updatedAlternativa.setAlternativaCorreta(true);

        AlternativasResponseDto responseDto = new AlternativasResponseDto();
        responseDto.setAlternativa("Novo Texto");

        when(alternativasRepository.findById(id)).thenReturn(Optional.of(existingAlternativa));
        when(alternativasRepository.save(any(Alternativas.class))).thenReturn(updatedAlternativa);
        when(modelMapper.map(updatedAlternativa, AlternativasResponseDto.class)).thenReturn(responseDto);

        AlternativasResponseDto result = alternativasService.updateAlternativa(id, updateDto);

        assertNotNull(result);
        assertEquals("Novo Texto", result.getAlternativa());

        verify(alternativasRepository).save(existingAlternativa);

        assertEquals("Novo Texto", existingAlternativa.getAlternativa());
        assertEquals(true, existingAlternativa.getAlternativaCorreta());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar alternativa inexistente (Sad Path)")
    void shouldThrowExceptionWhenUpdateAlternativaNotFound() {

        Long id = 99L;
        AlternativasUpdateDto updateDto = new AlternativasUpdateDto();
        when(alternativasRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            alternativasService.updateAlternativa(id, updateDto);
        });

        verify(alternativasRepository, never()).save(any());
    }


    @Test
    @DisplayName("Deve deletar alternativa existente (Happy Path)")
    void shouldDeleteAlternativaSuccessfully() {

        Long id = 1L;
        Alternativas alternativa = new Alternativas();
        alternativa.setIdAlternativa(id);

        when(alternativasRepository.findById(id)).thenReturn(Optional.of(alternativa));

        alternativasService.deleteAlternativa(id);

        verify(alternativasRepository).delete(alternativa);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar alternativa inexistente (Sad Path)")
    void shouldThrowExceptionWhenDeleteAlternativaNotFound() {

        Long id = 99L;
        when(alternativasRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            alternativasService.deleteAlternativa(id);
        });

        verify(alternativasRepository, never()).delete(any());
    }
}
