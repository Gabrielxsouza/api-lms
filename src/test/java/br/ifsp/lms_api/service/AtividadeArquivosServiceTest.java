package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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

import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;

import br.ifsp.lms_api.repository.AtividadeArquivosRepository;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.AtividadeArquivos;


@ExtendWith(MockitoExtension.class)
public class AtividadeArquivosServiceTest {

    @Mock
    private AtividadeArquivosRepository atividadeArquivosRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private AtividadeArquivosService atividadeArquivosService;

    private AtividadeArquivos atividadeArquivo; 
    private AtividadeArquivosRequestDto requestDto;
    private AtividadeArquivosResponseDto responseDto;
    private AtividadeArquivosUpdateDto updateDto; 
    private LocalDate dataInicio;
    private LocalDate dataFechamento;

    @BeforeEach
    void setUp() {
        dataInicio = LocalDate.of(2025, 11, 1);
        dataFechamento = LocalDate.of(2025, 11, 30);

        atividadeArquivo = new AtividadeArquivos();
        atividadeArquivo.setIdAtividade(1L);
        atividadeArquivo.setTituloAtividade("Trabalho de Java");
        atividadeArquivo.setDescricaoAtividade("Entregar um CRUD");
        atividadeArquivo.setDataInicioAtividade(dataInicio);
        atividadeArquivo.setDataFechamentoAtividade(dataFechamento);
        atividadeArquivo.setStatusAtividade(true);
        atividadeArquivo.setArquivosPermitidos(List.of(".pdf", ".zip"));

        requestDto = new AtividadeArquivosRequestDto();
        requestDto.setTituloAtividade("Trabalho de Java");
        requestDto.setDescricaoAtividade("Entregar um CRUD");
        requestDto.setDataInicioAtividade(dataInicio);
        requestDto.setDataFechamentoAtividade(dataFechamento);
        requestDto.setStatusAtividade(true);
        requestDto.setArquivosPermitidos(List.of(".pdf", ".zip"));

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
    }

    @Test
    void testCreateAtividadeArquivos_Success() {
        when(modelMapper.map(requestDto, AtividadeArquivos.class)).thenReturn(atividadeArquivo);
        when(atividadeArquivosRepository.save(atividadeArquivo)).thenReturn(atividadeArquivo);
        when(modelMapper.map(atividadeArquivo, AtividadeArquivosResponseDto.class)).thenReturn(responseDto);

        AtividadeArquivosResponseDto result = atividadeArquivosService.createAtividadeArquivos(requestDto);

        assertNotNull(result);
        assertEquals(responseDto.getTituloAtividade(), result.getTituloAtividade());
        assertEquals(".pdf", result.getArquivosPermitidos().get(0));
        verify(modelMapper, times(1)).map(requestDto, AtividadeArquivos.class);
        verify(atividadeArquivosRepository, times(1)).save(atividadeArquivo);
        verify(modelMapper, times(1)).map(atividadeArquivo, AtividadeArquivosResponseDto.class);
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

        AtividadeArquivosResponseDto updatedResponse = new AtividadeArquivosResponseDto();
        updatedResponse.setIdAtividade(id);
        updatedResponse.setTituloAtividade("Trabalho de Java V2"); 
        updatedResponse.setDescricaoAtividade("Entregar um CRUD"); 
        updatedResponse.setDataInicioAtividade(dataInicio);
        updatedResponse.setDataFechamentoAtividade(dataFechamento);
        updatedResponse.setStatusAtividade(true);
        updatedResponse.setArquivosPermitidos(List.of(".pdf", ".docx")); 

        when(atividadeArquivosRepository.findById(id)).thenReturn(Optional.of(atividadeArquivo));
        when(atividadeArquivosRepository.save(any(AtividadeArquivos.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(AtividadeArquivos.class), eq(AtividadeArquivosResponseDto.class)))
            .thenReturn(updatedResponse);

        AtividadeArquivosResponseDto result = atividadeArquivosService.updateAtividadeArquivos(id, updateDto);

        assertNotNull(result);
        assertEquals("Trabalho de Java V2", result.getTituloAtividade());
        assertEquals(".docx", result.getArquivosPermitidos().get(1));

        ArgumentCaptor<AtividadeArquivos> entityCaptor = ArgumentCaptor.forClass(AtividadeArquivos.class);
        verify(atividadeArquivosRepository, times(1)).save(entityCaptor.capture());

        AtividadeArquivos savedEntity = entityCaptor.getValue();
        assertEquals("Trabalho de Java V2", savedEntity.getTituloAtividade()); 
        assertEquals(List.of(".pdf", ".docx"), savedEntity.getArquivosPermitidos()); 
        assertEquals("Entregar um CRUD", savedEntity.getDescricaoAtividade()); 
    }

    @Test
    void testUpdateAtividadeArquivos_NotFound() {
        Long id = 1L;
        when(atividadeArquivosRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            atividadeArquivosService.updateAtividadeArquivos(id, updateDto);
        });

        verify(atividadeArquivosRepository, times(1)).findById(id);
        verify(atividadeArquivosRepository, never()).save(any());
    }

    @Test
    void testDeleteAtividadeArquivos_Success() {
        Long id = 1L;
        when(atividadeArquivosRepository.findById(id)).thenReturn(Optional.of(atividadeArquivo));
        doNothing().when(atividadeArquivosRepository).delete(atividadeArquivo);

        atividadeArquivosService.deleteAtividadeArquivos(id);

        verify(atividadeArquivosRepository, times(1)).findById(id);
        verify(atividadeArquivosRepository, times(1)).delete(atividadeArquivo);
    }

    @Test
    void testDeleteAtividadeArquivos_NotFound() {
        Long id = 1L;
        when(atividadeArquivosRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            atividadeArquivosService.deleteAtividadeArquivos(id);
        });

        verify(atividadeArquivosRepository, times(1)).findById(id);
        verify(atividadeArquivosRepository, never()).delete(any());
    }
}
