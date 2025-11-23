package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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

import br.ifsp.lms_api.dto.CursoDto.CursoRequestDto;
import br.ifsp.lms_api.dto.CursoDto.CursoResponseDto;
import br.ifsp.lms_api.dto.CursoDto.CursoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Curso;
import br.ifsp.lms_api.repository.CursoRepository;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private CursoService cursoService;

    private Curso curso;
    private CursoRequestDto cursoRequestDto;
    private CursoResponseDto cursoResponseDto;
    private CursoUpdateDto cursoUpdateDto;

    @BeforeEach
    void setUp() {
        curso = new Curso();
        curso.setIdCurso(1L);
        curso.setNomeCurso("Ciência da Computação");
        curso.setCodigoCurso("BCC");
        curso.setDescricaoCurso("Bacharelado");
        curso.setTurmas(Collections.emptyList());

        cursoRequestDto = new CursoRequestDto();
        cursoRequestDto.setNomeCurso("Ciência da Computação");
        cursoRequestDto.setCodigoCurso("BCC");
        cursoRequestDto.setDescricaoCurso("Bacharelado");

        cursoResponseDto = new CursoResponseDto();
        cursoResponseDto.setIdCurso(1L);
        cursoResponseDto.setNomeCurso("Ciência da Computação");
        cursoResponseDto.setCodigoCurso("BCC");
        cursoResponseDto.setDescricaoCurso("Bacharelado");
        cursoResponseDto.setTurmas(Collections.emptyList());
        
        cursoUpdateDto = new CursoUpdateDto(
            Optional.of("Engenharia de Software"),
            Optional.empty(),
            Optional.empty()
        );
    }

    @Test
    void createCurso_Success() {
        when(modelMapper.map(cursoRequestDto, Curso.class)).thenReturn(curso);
        when(cursoRepository.save(curso)).thenReturn(curso);

        CursoResponseDto response = cursoService.createCurso(cursoRequestDto);

        assertNotNull(response);
        assertEquals(curso.getIdCurso(), response.getIdCurso());
        assertEquals(curso.getNomeCurso(), response.getNomeCurso());
        verify(cursoRepository, times(1)).save(curso);
    }

    @Test
    void getAllCursos_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Curso> page = new PageImpl<>(List.of(curso));

        when(cursoRepository.findAll(pageable)).thenReturn(page);

        PagedResponse<CursoResponseDto> response = cursoService.getAllCursos(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Ciência da Computação", response.getContent().get(0).getNomeCurso());
    }

    @Test
    void getCursoById_Success() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        CursoResponseDto response = cursoService.getCursoById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getIdCurso());
    }

    @Test
    void getCursoById_NotFound() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cursoService.getCursoById(1L));
    }

    @Test
    void updateCurso_Success() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.save(any(Curso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CursoResponseDto response = cursoService.updateCurso(1L, cursoUpdateDto);

        assertNotNull(response);
        assertEquals("Engenharia de Software", response.getNomeCurso());
        assertEquals("BCC", response.getCodigoCurso()); 
        verify(cursoRepository, times(1)).save(curso);
    }

    @Test
    void deleteCurso_Success() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        doNothing().when(cursoRepository).delete(curso);

        cursoService.deleteCurso(1L);

        verify(cursoRepository, times(1)).delete(curso);
    }
}