package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

@ExtendWith(MockitoExtension.class)
public class AlternativasServiceTest {

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

    private Alternativas alternativas;
    private AlternativasRequestDto requestDto;
    private AlternativasResponseDto responseDto;
    private AlternativasUpdateDto updateDto;
    private AlternativasResponseDto responseDtoAtualizado;
    private Questoes questao;

    @BeforeEach
    void setUp() {
        alternativas = new Alternativas();
        alternativas.setIdAlternativa(1L);
        alternativas.setAlternativa("Teste de Alternativa");
        alternativas.setAlternativaCorreta(true);

        requestDto = new AlternativasRequestDto();
        requestDto.setAlternativa("Teste de Alternativa");
        requestDto.setAlternativaCorreta(true);
        requestDto.setIdQuestao(1L);

        responseDto = new AlternativasResponseDto();
        responseDto.setIdAlternativa(1L);
        responseDto.setAlternativa("Teste de Alternativa");
        responseDto.setAlternativaCorreta(true);

        updateDto = new AlternativasUpdateDto(
                Optional.of("Nova Alternativa"),
                Optional.of(false) 
        );

        questao = new Questoes();
        questao.setIdQuestao(1L);
        questao.setEnunciado("Teste");

        responseDtoAtualizado = new AlternativasResponseDto();
        responseDtoAtualizado.setIdAlternativa(1L);
        responseDtoAtualizado.setAlternativa("Nova Alternativa");
        responseDtoAtualizado.setAlternativaCorreta(false);
    }

    @Test
    void testCreateAlternativa_Success() {

        when(questoesRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(modelMapper.map(requestDto, Alternativas.class)).thenReturn(alternativas);
        when(alternativasRepository.save(alternativas)).thenReturn(alternativas);
        when(modelMapper.map(alternativas, AlternativasResponseDto.class)).thenReturn(responseDto);

        AlternativasResponseDto result = alternativasService.createAlternativa(requestDto);

        assertNotNull(result);
        assertEquals(responseDto.getIdAlternativa(), result.getIdAlternativa());
        assertEquals(responseDto.getAlternativa(), result.getAlternativa());

        verify(questoesRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(requestDto, Alternativas.class);
        verify(alternativasRepository, times(1)).save(alternativas);
        verify(modelMapper, times(1)).map(alternativas, AlternativasResponseDto.class);
    }

    @Test
    void testGetAlternativaById_Success() {
        Long id = 1L;
        when(alternativasRepository.findById(id)).thenReturn(Optional.of(alternativas));
        when(modelMapper.map(alternativas, AlternativasResponseDto.class)).thenReturn(responseDto);

        AlternativasResponseDto result = alternativasService.getAlternativaById(id);

        assertNotNull(result);
        assertEquals(responseDto.getIdAlternativa(), result.getIdAlternativa());
        verify(alternativasRepository, times(1)).findById(id);
        verify(modelMapper, times(1)).map(alternativas, AlternativasResponseDto.class);
    }

    @Test
    void testGetAlternativaById_NotFound() {
        Long id = 1L;
        when(alternativasRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            alternativasService.getAlternativaById(id);
        });

        verify(alternativasRepository, times(1)).findById(id);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testGetAllAlternativas_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        long totalElements = 1L;

        List<AlternativasResponseDto> dtoList = List.of(responseDto);

        Page<Alternativas> alternativaPage = new PageImpl<>(List.of(alternativas), pageable, totalElements);

        PagedResponse<AlternativasResponseDto> pagedResponse = new PagedResponse<>(
                dtoList, 
                alternativaPage.getNumber(), 
                alternativaPage.getSize(), 
                alternativaPage.getTotalElements(), 
                alternativaPage.getTotalPages(), 
                alternativaPage.isLast() 
        );

        when(alternativasRepository.findAll(pageable)).thenReturn(alternativaPage);

        when(pagedResponseMapper.toPagedResponse(alternativaPage, AlternativasResponseDto.class))
                .thenReturn(pagedResponse); 

        PagedResponse<AlternativasResponseDto> resultPage = alternativasService.getAllAlternativas(pageable);

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(0, resultPage.getPage());
        assertTrue(resultPage.isLast());
        assertEquals(responseDto.getAlternativa(), resultPage.getContent().get(0).getAlternativa());

        verify(alternativasRepository, times(1)).findAll(pageable);
        verify(pagedResponseMapper, times(1)).toPagedResponse(alternativaPage, AlternativasResponseDto.class);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testDeleteAlternativa_Success() {
        Long id = 1L;
        when(alternativasRepository.findById(id)).thenReturn(Optional.of(alternativas));
        doNothing().when(alternativasRepository).delete(alternativas);

        alternativasService.deleteAlternativa(id);

        verify(alternativasRepository, times(1)).findById(id);
        verify(alternativasRepository, times(1)).delete(alternativas);
    }

    @Test
    void testDeleteAlternativa_NotFound() {
        Long id = 1L;
        when(alternativasRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            alternativasService.deleteAlternativa(id);
        });

        verify(alternativasRepository, times(1)).findById(id);
        verify(alternativasRepository, never()).delete(any());
    }

    @Test
    void testUpdateAlternativa_Success() {
        Long id = 1L;

        when(alternativasRepository.findById(id)).thenReturn(Optional.of(alternativas));

        when(alternativasRepository.save(any(Alternativas.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(modelMapper.map(any(Alternativas.class), eq(AlternativasResponseDto.class)))
                .thenReturn(responseDtoAtualizado); 

        AlternativasResponseDto result = alternativasService.updateAlternativa(id, updateDto);

        assertNotNull(result);
        assertEquals("Nova Alternativa", result.getAlternativa());
        assertEquals(false, result.getAlternativaCorreta());

        ArgumentCaptor<Alternativas> alternativaCaptor = ArgumentCaptor.forClass(Alternativas.class);

        verify(alternativasRepository, times(1)).findById(id);
        verify(alternativasRepository, times(1)).save(alternativaCaptor.capture());
        verify(modelMapper, times(1)).map(any(Alternativas.class), eq(AlternativasResponseDto.class));

        Alternativas savedAlternativa = alternativaCaptor.getValue();
        assertEquals("Nova Alternativa", savedAlternativa.getAlternativa());
        assertEquals(false, savedAlternativa.getAlternativaCorreta());
        assertEquals(1L, savedAlternativa.getIdAlternativa());
    }

    @Test
    void testUpdateAlternativa_NotFound() {
        Long id = 1L;
        when(alternativasRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            alternativasService.updateAlternativa(id, updateDto);
        });

        verify(alternativasRepository, times(1)).findById(id);
        verify(modelMapper, never()).map(any(), any());
        verify(alternativasRepository, never()).save(any());
    }
}
