package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import br.ifsp.lms_api.dto.matriculaDto.MatriculaRequestDto;
import br.ifsp.lms_api.dto.matriculaDto.MatriculaResponseDto;
import br.ifsp.lms_api.dto.matriculaDto.MatriculaUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.Matricula;
import br.ifsp.lms_api.model.Status;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.MatriculaRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock private MatriculaRepository matriculaRepository;
    @Mock private AlunoRepository alunoRepository;
    @Mock private TurmaRepository turmaRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private PagedResponseMapper pagedResponseMapper;

    @InjectMocks private MatriculaService matriculaService;

    private Matricula matricula;
    private Aluno aluno;
    private Turma turma;
    private MatriculaRequestDto requestDto;

    @BeforeEach
    void setUp() {
        aluno = new Aluno();
        aluno.setIdUsuario(1L);
        aluno.setNome("João Aluno");
        aluno.setRa("123456");

        turma = new Turma();
        turma.setIdTurma(2L);
        turma.setNomeTurma("Turma Teste");

        matricula = new Matricula();
        matricula.setIdMatricula(10L);
        matricula.setAluno(aluno);
        matricula.setTurma(turma);
        matricula.setStatusMatricula(Status.ATIVA);

        requestDto = new MatriculaRequestDto(1L, 2L, Status.ATIVA);
    }

    @Test
    void createMatricula_Success() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(turmaRepository.findById(2L)).thenReturn(Optional.of(turma));
        when(matriculaRepository.save(any(Matricula.class))).thenReturn(matricula);

        MatriculaResponseDto response = matriculaService.createMatricula(requestDto);

        assertNotNull(response);
        assertEquals(10L, response.getIdMatricula());
        assertEquals("João Aluno", response.getNomeAluno());
        verify(matriculaRepository, times(1)).save(any(Matricula.class));
    }

    @Test
    void createMatricula_AlunoNotFound() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> matriculaService.createMatricula(requestDto));
    }

   @Test
    void getAllMatriculas_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Matricula> page = new PageImpl<>(List.of(matricula));
        
    
        PagedResponse<MatriculaResponseDto> pagedResponse = new PagedResponse<>(
            List.of(), 
            0,        
            10,       
            1,     
            1,        
            true      
        );

        when(matriculaRepository.findAll(pageable)).thenReturn(page);
        when(pagedResponseMapper.toPagedResponse(page, MatriculaResponseDto.class)).thenReturn(pagedResponse);

        var result = matriculaService.getAllMatriculas(pageable);
        
        assertNotNull(result);
        verify(matriculaRepository).findAll(pageable);
    }

    @Test
    void updateMatricula_Success() {
        MatriculaUpdateDto updateDto = new MatriculaUpdateDto(Optional.of("PENDENTE"));
        
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));
        when(matriculaRepository.save(any(Matricula.class))).thenReturn(matricula);
        when(modelMapper.map(any(), eq(MatriculaResponseDto.class))).thenReturn(new MatriculaResponseDto());

        matriculaService.updateMatricula(10L, updateDto);

        assertEquals(Status.PENDENTE, matricula.getStatusMatricula());
        verify(matriculaRepository).save(matricula);
    }

    @Test
    void deleteMatricula_Success() {
        when(matriculaRepository.findById(10L)).thenReturn(Optional.of(matricula));
        doNothing().when(matriculaRepository).delete(matricula);

        matriculaService.deleteMatricula(10L);

        verify(matriculaRepository).delete(matricula);
    }
}