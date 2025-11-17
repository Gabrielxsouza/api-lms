package br.ifsp.lms_api.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaResponseDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaRequestDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper; // IMPORT ADICIONADO
import br.ifsp.lms_api.model.Curso;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.CursoRepository;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.ProfessorRepository;
import br.ifsp.lms_api.repository.TurmaRepository;

@ExtendWith(MockitoExtension.class)
public class TurmaServiceTest {

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository; 

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private ProfessorRepository professorRepository; // MOCK ADICIONADO

    @Mock
    private AutentificacaoService autentificacaoService; // Mantido para o getMinhasTurmas

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private TurmaService turmaService;

    private Disciplina disciplina;
    private Curso curso;
    private Professor professor;
    private Turma turma;
    private TurmaRequestDto requestDto;
    private TurmaResponseDto responseDto;
    private TurmaUpdateDto updateDto;
    private TurmaResponseDto responseDtoAtualizado;

    @BeforeEach
    void setUp() {
        disciplina = new Disciplina();
        disciplina.setIdDisciplina(1L);
        disciplina.setNomeDisciplina("Engenharia de Software");

        curso = new Curso();
        curso.setIdCurso(1L);
        curso.setNomeCurso("Ciencia da Computacao");

        professor = new Professor();
        professor.setIdUsuario(1L);
        professor.setNome("Prof. Teste");

        turma = new Turma();
        turma.setIdTurma(1L);
        turma.setNomeTurma("Turma A");
        turma.setSemestre("2025/2");
        turma.setDisciplina(disciplina);
        turma.setCurso(curso);
        turma.setProfessor(professor);

        // CORRIGIDO: Adicionado idProfessor (1L) como 5º argumento
        requestDto = new TurmaRequestDto("Turma A", "2025/2", 1L, 1L, 1L); 

        DisciplinaResponseDto disciplinaResponseDto = new DisciplinaResponseDto(
            1L, "Engenharia de Software", "Testes", "ESL708", null 
        );

        responseDto = new TurmaResponseDto(
            1L, "Turma A", "2025/2", disciplinaResponseDto
        );
        
        updateDto = new TurmaUpdateDto(
            Optional.of("Novo Semestre"),
            Optional.of("Novo Nome Turma")
        );

        responseDtoAtualizado = new TurmaResponseDto(
            1L, "Novo Nome Turma", "Novo Semestre", disciplinaResponseDto
        );
    }

    @Test
    void testCreateTurma_Success() {
        // LÓGICA ATUALIZADA: Mockar os 3 repositórios
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        
        Turma turmaTransient = new Turma();
        when(modelMapper.map(requestDto, Turma.class)).thenReturn(turmaTransient);
        
        when(turmaRepository.save(any(Turma.class))).thenReturn(turma);
        
        when(modelMapper.map(turma, TurmaResponseDto.class)).thenReturn(responseDto);

        TurmaResponseDto result = turmaService.createTurma(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getIdTurma());
        assertEquals(1L, result.getDisciplina().getIdDisciplina());

        ArgumentCaptor<Turma> captor = ArgumentCaptor.forClass(Turma.class);
        verify(turmaRepository, times(1)).save(captor.capture());
        
        Turma turmaSalva = captor.getValue();
        assertNull(turmaSalva.getIdTurma());
        assertEquals(disciplina, turmaSalva.getDisciplina());
        assertEquals(curso, turmaSalva.getCurso());
        assertEquals(professor, turmaSalva.getProfessor());
    }

    @Test
    void testCreateTurma_DisciplinaNotFound() {
        // Não precisamos mais mockar o autentificacaoService aqui
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            turmaService.createTurma(requestDto);
        });

        verify(turmaRepository, never()).save(any());
    }

    @Test
    void testCreateTurma_CursoNotFound() {
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(cursoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            turmaService.createTurma(requestDto);
        });

        verify(turmaRepository, never()).save(any());
    }

    @Test
    void testCreateTurma_ProfessorNotFound() {
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(professorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            turmaService.createTurma(requestDto);
        });

        verify(turmaRepository, never()).save(any());
    }
    
    @Test
    void testGetTurmaById_Success() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(modelMapper.map(turma, TurmaResponseDto.class)).thenReturn(responseDto);

        TurmaResponseDto result = turmaService.getTurmaById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdTurma());
    }

    @Test
    void testGetAllTurmas_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Turma> turmaPage = new PageImpl<>(List.of(turma), pageable, 1L);
        
        PagedResponse<TurmaResponseDto> pagedResponse = new PagedResponse<>(
            List.of(responseDto), 0, 10, 1L, 1, true
        );

        when(turmaRepository.findAll(pageable)).thenReturn(turmaPage);
        when(pagedResponseMapper.toPagedResponse(turmaPage, TurmaResponseDto.class))
            .thenReturn(pagedResponse);

        PagedResponse<TurmaResponseDto> result = turmaService.getAllTurmas(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
    
    @Test
    void testGetMinhasTurmas_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Turma> turmaPage = new PageImpl<>(List.of(turma), pageable, 1L);
        
        PagedResponse<TurmaResponseDto> pagedResponse = new PagedResponse<>(
            List.of(responseDto), 0, 10, 1L, 1, true
        );
        
        // Este teste continua usando o autentificacaoService, o que está correto
        when(autentificacaoService.getUsuarioLogado()).thenReturn(professor);
        when(turmaRepository.findByProfessor(professor, pageable)).thenReturn(turmaPage);
        when(pagedResponseMapper.toPagedResponse(turmaPage, TurmaResponseDto.class))
            .thenReturn(pagedResponse);

        PagedResponse<TurmaResponseDto> result = turmaService.getMinhasTurmas(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(turmaRepository, times(1)).findByProfessor(professor, pageable);
    }
    
    @Test
    void testGetMinhasTurmas_NotProfessor() {
        when(autentificacaoService.getUsuarioLogado()).thenReturn(null); // ou um Aluno

        assertThrows(AccessDeniedException.class, () -> {
            turmaService.getMinhasTurmas(PageRequest.of(0, 10));
        });

        verify(turmaRepository, never()).findByProfessor(any(), any());
    }
    
    @Test
    void testUpdateTurma_Success() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(turmaRepository.save(any(Turma.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(Turma.class), eq(TurmaResponseDto.class))).thenReturn(responseDtoAtualizado);

        TurmaResponseDto result = turmaService.updateTurma(1L, updateDto);

        assertNotNull(result);
        assertEquals("Novo Nome Turma", result.getNomeTurma());
        assertEquals("Novo Semestre", result.getSemestre());
        
        ArgumentCaptor<Turma> captor = ArgumentCaptor.forClass(Turma.class);
        verify(turmaRepository, times(1)).save(captor.capture());
        Turma turmaSalva = captor.getValue();
        
        assertEquals("Novo Nome Turma", turmaSalva.getNomeTurma());
        assertEquals("Novo Semestre", turmaSalva.getSemestre());
    }
    
    @Test
    void testDeleteTurma_Success() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        doNothing().when(turmaRepository).delete(turma);

        turmaService.deleteTurma(1L);

        verify(turmaRepository, times(1)).delete(turma);
    }
}