package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.professorDto.ProfessorRequestDto;
import br.ifsp.lms_api.dto.professorDto.ProfessorResponseDto;
import br.ifsp.lms_api.dto.professorDto.ProfessorUpdateDto;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.repository.ProfessorRepository;

@ExtendWith(MockitoExtension.class)
class ProfessorServiceTest {

    @Mock private ProfessorRepository professorRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private PagedResponseMapper pagedResponseMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private ProfessorService professorService;

    private Professor professor;
    private ProfessorRequestDto requestDto;
    private ProfessorResponseDto responseDto;
    private ProfessorUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        professor = new Professor();
        professor.setIdUsuario(1L);
        professor.setNome("Prof. João");
        professor.setEmail("joao@ifsp.edu.br");
        professor.setDepartamento("Computação");
        professor.setSenha("senhaCodificada");

        requestDto = new ProfessorRequestDto("Prof. João", "joao@ifsp.edu.br", "123456", "12345678900", "Computação");
        
        responseDto = new ProfessorResponseDto(1L, "Prof. João", "joao@ifsp.edu.br", "12345678900", "Computação");

        updateDto = new ProfessorUpdateDto(Optional.of("Prof. João Silva"), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Test
    void createProfessor_Success() {
        when(modelMapper.map(requestDto, Professor.class)).thenReturn(professor);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(professorRepository.save(professor)).thenReturn(professor);
        when(modelMapper.map(professor, ProfessorResponseDto.class)).thenReturn(responseDto);

        ProfessorResponseDto result = professorService.createProfessor(requestDto);

        assertNotNull(result);
        assertEquals("Prof. João", result.getNome());
        verify(passwordEncoder).encode("123456");
        verify(professorRepository).save(professor);
    }

    @Test
    void getAllProfessores_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Professor> page = new PageImpl<>(List.of(professor));
        PagedResponse<ProfessorResponseDto> pagedResponse = new PagedResponse<>(List.of(responseDto), 0, 10, 1L, 1, true);

        when(professorRepository.findAll(pageable)).thenReturn(page);
        when(pagedResponseMapper.toPagedResponse(page, ProfessorResponseDto.class)).thenReturn(pagedResponse);

        var result = professorService.getAllProfessores(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getProfessorById_Success() {
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(modelMapper.map(professor, ProfessorResponseDto.class)).thenReturn(responseDto);

        var result = professorService.getProfessorById(1L);
        assertNotNull(result);
    }

    @Test
    void getProfessorById_NotFound() {
        when(professorRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> professorService.getProfessorById(1L));
    }

    @Test
    void updateProfessor_Success() {
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(professorRepository.save(professor)).thenReturn(professor);
        when(modelMapper.map(professor, ProfessorResponseDto.class)).thenReturn(responseDto);

        professorService.updateProfessor(1L, updateDto);

        assertEquals("Prof. João Silva", professor.getNome()); 
        verify(professorRepository).save(professor);
    }

    @Test
    void deleteProfessor_Success() {
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        doNothing().when(professorRepository).delete(professor);

        professorService.deleteProfessor(1L);
        verify(professorRepository).delete(professor);
    }
}