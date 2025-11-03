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

import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaRequestDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaResponseDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaUpdateDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaParaDisciplinaDTO;
import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.DisciplinaRepository;

@ExtendWith(MockitoExtension.class)
public class DisciplinaServiceTest {

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private DisciplinaService disciplinaService;

    // --- Objetos de Teste ---
    private Disciplina disciplina;
    private Turma turma;
    private DisciplinaRequestDto requestDto;
    private TurmaParaDisciplinaDTO turmaRequestDto;
    private DisciplinaResponseDto responseDto;
    private TurmaResponseDto turmaResponseDto;
    private DisciplinaUpdateDto updateDto;
    private DisciplinaResponseDto responseDtoAtualizado;

    @BeforeEach
    void setUp() {
        // --- Entidades ---
        disciplina = new Disciplina();
        disciplina.setIdDisciplina(1L);
        disciplina.setNomeDisciplina("Engenharia de Software");
        disciplina.setCodigoDisciplina("ESL708");
        disciplina.setDescricaoDisciplina("Testes");

        turma = new Turma();
        turma.setIdTurma(1L);
        turma.setNomeTurma("Turma A");
        turma.setSemestre("2025/2");
        turma.setDisciplina(disciplina); // Link bidirecional

        disciplina.setTurmas(List.of(turma));

        // --- DTOs ---
        turmaRequestDto = new TurmaParaDisciplinaDTO("Turma A", "2025/2");

        requestDto = new DisciplinaRequestDto(
            "Engenharia de Software",
            "Testes",
            "ESL708",
            List.of(turmaRequestDto)
        );

        turmaResponseDto = new TurmaResponseDto(1L, "Turma A", "2025/2", null); // O DTO de disciplina é null por causa do @JsonBackReference

        responseDto = new DisciplinaResponseDto(
            1L,
            "Engenharia de Software",
            "Testes",
            "ESL708",
            List.of(turmaResponseDto)
        );
        
        // --- Update DTOs ---
        updateDto = new DisciplinaUpdateDto(
            Optional.of("Novo Nome"),
            Optional.of("Nova Descricao"),
            Optional.of("ESL709")
        );

        responseDtoAtualizado = new DisciplinaResponseDto(
            1L,
            "Novo Nome",
            "Nova Descricao",
            "ESL709",
            List.of(turmaResponseDto) // Lista de turmas não muda no update
        );
    }

    @Test
    void testCreateDisciplina_Success() {
        // Arrange
        
        // 1. Mocka o mapeamento do DTO de Disciplina para a Entidade Disciplina
        //    (Ignoramos a lista de turmas por enquanto, ela será setada manualmente no service)
        Disciplina disciplinaSemTurmas = new Disciplina();
        disciplinaSemTurmas.setNomeDisciplina(requestDto.getNomeDisciplina());
        disciplinaSemTurmas.setCodigoDisciplina(requestDto.getCodigoDisciplina());
        disciplinaSemTurmas.setDescricaoDisciplina(requestDto.getDescricaoDisciplina());
        
        when(modelMapper.map(eq(requestDto), eq(Disciplina.class))).thenReturn(disciplinaSemTurmas);

        // 2. Mocka o mapeamento do DTO de Turma (aninhado) para a Entidade Turma
        //    (O service fará isso dentro de um stream)
        when(modelMapper.map(eq(turmaRequestDto), eq(Turma.class))).thenReturn(new Turma(null, "Turma A", "2025/2", null, null));

        // 3. Mocka o save (usando ArgumentCaptor para garantir que a relação bidirecional foi setada)
        ArgumentCaptor<Disciplina> disciplinaCaptor = ArgumentCaptor.forClass(Disciplina.class);
        when(disciplinaRepository.save(disciplinaCaptor.capture())).thenReturn(disciplina); // Retorna a disciplina completa com ID e turmas

        // 4. Mocka o mapeamento final da Entidade salva para o DTO de Resposta
        when(modelMapper.map(eq(disciplina), eq(DisciplinaResponseDto.class))).thenReturn(responseDto);

        // Act
        DisciplinaResponseDto result = disciplinaService.createDisciplina(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdDisciplina());
        assertEquals("Engenharia de Software", result.getNomeDisciplina());
        assertEquals(1, result.getTurmas().size());
        assertEquals("Turma A", result.getTurmas().get(0).getNomeTurma());

        // Verifica se a entidade capturada pelo save() tinha a referência da disciplina na turma
        Disciplina disciplinaSalva = disciplinaCaptor.getValue();
        assertNotNull(disciplinaSalva.getTurmas());
        assertEquals(1, disciplinaSalva.getTurmas().size());
        // A linha abaixo é o teste mais importante: confirma que a relação foi setada
        assertEquals(disciplinaSalva, disciplinaSalva.getTurmas().get(0).getDisciplina()); 

        // Verify
        verify(disciplinaRepository, times(1)).save(any(Disciplina.class));
        verify(modelMapper, times(1)).map(eq(requestDto), eq(Disciplina.class));
        verify(modelMapper, times(1)).map(eq(turmaRequestDto), eq(Turma.class));
        verify(modelMapper, times(1)).map(eq(disciplina), eq(DisciplinaResponseDto.class));
    }
    
    @Test
    void testGetDisciplinaById_Success() {
        // Arrange
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(modelMapper.map(disciplina, DisciplinaResponseDto.class)).thenReturn(responseDto);

        // Act
        DisciplinaResponseDto result = disciplinaService.getDisciplinaById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdDisciplina());
        verify(disciplinaRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(disciplina, DisciplinaResponseDto.class);
    }

    @Test
    void testGetDisciplinaById_NotFound() {
        // Arrange
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            disciplinaService.getDisciplinaById(1L);
        });
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testGetAllDisciplinas_Success() {
        // Arrange (Lógica de paginação copiada do seu exemplo)
        Pageable pageable = PageRequest.of(0, 10);
        Page<Disciplina> disciplinaPage = new PageImpl<>(List.of(disciplina), pageable, 1L);
        
        PagedResponse<DisciplinaResponseDto> pagedResponse = new PagedResponse<>(
            List.of(responseDto), 0, 10, 1L, 1, true
        );

        when(disciplinaRepository.findAll(pageable)).thenReturn(disciplinaPage);
        when(pagedResponseMapper.toPagedResponse(disciplinaPage, DisciplinaResponseDto.class))
            .thenReturn(pagedResponse);

        // Act
        PagedResponse<DisciplinaResponseDto> result = disciplinaService.getAllDisciplinas(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Engenharia de Software", result.getContent().get(0).getNomeDisciplina());
        verify(disciplinaRepository, times(1)).findAll(pageable);
        verify(pagedResponseMapper, times(1)).toPagedResponse(disciplinaPage, DisciplinaResponseDto.class);
    }
    
    @Test
    void testUpdateDisciplina_Success() {
        // Arrange (Lógica do ArgumentCaptor copiada do seu exemplo)
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(disciplinaRepository.save(any(Disciplina.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(Disciplina.class), eq(DisciplinaResponseDto.class))).thenReturn(responseDtoAtualizado);

        // Act
        DisciplinaResponseDto result = disciplinaService.updateDisciplina(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Novo Nome", result.getNomeDisciplina());
        assertEquals("ESL709", result.getCodigoDisciplina());

        // Verifica se a lógica "ifPresent" do serviço funcionou
        ArgumentCaptor<Disciplina> captor = ArgumentCaptor.forClass(Disciplina.class);
        verify(disciplinaRepository, times(1)).save(captor.capture());
        Disciplina disciplinaSalva = captor.getValue();
        
        assertEquals("Novo Nome", disciplinaSalva.getNomeDisciplina());
        assertEquals("Nova Descricao", disciplinaSalva.getDescricaoDisciplina());
        assertEquals("ESL709", disciplinaSalva.getCodigoDisciplina());
    }
    
    @Test
    void testDeleteDisciplina_Success() {
        // Arrange
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        doNothing().when(disciplinaRepository).delete(disciplina);

        // Act
        disciplinaService.deleteDisciplina(1L);

        // Assert
        verify(disciplinaRepository, times(1)).findById(1L);
        verify(disciplinaRepository, times(1)).delete(disciplina);
    }
}