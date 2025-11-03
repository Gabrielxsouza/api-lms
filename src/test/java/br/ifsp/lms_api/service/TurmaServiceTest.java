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

import br.ifsp.lms_api.dto.TurmaDto.TurmaRequestDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaUpdateDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.TurmaRepository;

@ExtendWith(MockitoExtension.class)
public class TurmaServiceTest {

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository; // Dependência externa

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private TurmaService turmaService;

    // --- Objetos de Teste ---
    private Disciplina disciplina;
    private Turma turma;
    private TurmaRequestDto requestDto;
    private TurmaResponseDto responseDto;
    private TurmaUpdateDto updateDto;
    private TurmaResponseDto responseDtoAtualizado;

    @BeforeEach
    void setUp() {
        // Entidade "Pai"
        disciplina = new Disciplina();
        disciplina.setIdDisciplina(1L);
        disciplina.setNomeDisciplina("Engenharia de Software");

        // Entidade Principal
        turma = new Turma();
        turma.setIdTurma(1L);
        turma.setNomeTurma("Turma A");
        turma.setSemestre("2025/2");
        turma.setDisciplina(disciplina);

        // DTO de Requisição
        requestDto = new TurmaRequestDto("Turma A", "2025/2", 1L); // Pede o ID da disciplina

        // DTO de Resposta (Mãe)
        DisciplinaResponseDto disciplinaResponseDto = new DisciplinaResponseDto(
            1L, "Engenharia de Software", "Testes", "ESL708", null // turmas null
        );

        // DTO de Resposta (Principal)
        responseDto = new TurmaResponseDto(
            1L, "Turma A", "2025/2", disciplinaResponseDto
        );
        
        // --- Update DTOs ---
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
        // Arrange
        // 1. Mocka a busca da Disciplina "Pai"
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        
        // 2. Mocka o mapeamento do DTO para Entidade
        // (SetId(null) é feito no service, então o mapper pode retornar o ID)
        when(modelMapper.map(requestDto, Turma.class)).thenReturn(turma);
        
        // 3. Mocka o save
        when(turmaRepository.save(any(Turma.class))).thenReturn(turma);
        
        // 4. Mocka o mapeamento final para o DTO de Resposta
        when(modelMapper.map(turma, TurmaResponseDto.class)).thenReturn(responseDto);

        // Act
        TurmaResponseDto result = turmaService.createTurma(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdTurma());
        assertEquals(1L, result.getDisciplina().getIdDisciplina());

        // Captura o que foi salvo
        ArgumentCaptor<Turma> captor = ArgumentCaptor.forClass(Turma.class);
        verify(turmaRepository, times(1)).save(captor.capture());
        
        // Verifica se o service setou o ID para null (como no seu código)
        // e setou a disciplina correta
        assertNull(captor.getValue().getIdTurma());
        assertEquals(disciplina, captor.getValue().getDisciplina());
    }

    @Test
    void testCreateTurma_DisciplinaNotFound() {
        // Arrange
        // 1. Mocka a busca da Disciplina "Pai" para falhar
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            turmaService.createTurma(requestDto);
        });

        // Verifica que nada foi salvo
        verify(turmaRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }
    
    @Test
    void testGetTurmaById_Success() {
        // Arrange
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(modelMapper.map(turma, TurmaResponseDto.class)).thenReturn(responseDto);

        // Act
        TurmaResponseDto result = turmaService.getTurmaById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdTurma());
    }

    @Test
    void testGetAllTurmas_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Turma> turmaPage = new PageImpl<>(List.of(turma), pageable, 1L);
        
        PagedResponse<TurmaResponseDto> pagedResponse = new PagedResponse<>(
            List.of(responseDto), 0, 10, 1L, 1, true
        );

        when(turmaRepository.findAll(pageable)).thenReturn(turmaPage);
        when(pagedResponseMapper.toPagedResponse(turmaPage, TurmaResponseDto.class))
            .thenReturn(pagedResponse);

        // Act
        PagedResponse<TurmaResponseDto> result = turmaService.getAllTurmas(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
    
    @Test
    void testUpdateTurma_Success() {
        // Arrange
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(turmaRepository.save(any(Turma.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(Turma.class), eq(TurmaResponseDto.class))).thenReturn(responseDtoAtualizado);

        // Act
        TurmaResponseDto result = turmaService.updateTurma(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Novo Nome Turma", result.getNomeTurma());
        assertEquals("Novo Semestre", result.getSemestre());
        
        // Verifica se a lógica "ifPresent" do serviço funcionou
        ArgumentCaptor<Turma> captor = ArgumentCaptor.forClass(Turma.class);
        verify(turmaRepository, times(1)).save(captor.capture());
        Turma turmaSalva = captor.getValue();
        
        assertEquals("Novo Nome Turma", turmaSalva.getNomeTurma());
        assertEquals("Novo Semestre", turmaSalva.getSemestre());
    }
    
    @Test
    void testDeleteTurma_Success() {
        // Arrange
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        doNothing().when(turmaRepository).delete(turma);

        // Act
        turmaService.deleteTurma(1L);

        // Assert
        verify(turmaRepository, times(1)).delete(turma);
    }
}