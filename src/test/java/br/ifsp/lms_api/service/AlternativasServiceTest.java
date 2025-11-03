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
import org.modelmapper.ModelMapper; // Importado
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import br.ifsp.lms_api.dto.alternativasDto.AlternativasRequestDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasResponseDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
// Supondo que você tenha esse mapper de paginação
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.repository.AlternativasRepository;
import br.ifsp.lms_api.repository.QuestoesRepository;

@ExtendWith(MockitoExtension.class)
public class AlternativasServiceTest {

    @Mock // 1. Mocka o Repository
    private AlternativasRepository alternativasRepository;

    @Mock
    private QuestoesRepository questoesRepository;

    @Mock // 2. Mocka o ModelMapper genérico
    private ModelMapper modelMapper;

    @Mock // 3. Mocka o Mapper de Paginação
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks // 4. Injeta os Mocks no Serviço
    private AlternativasService alternativasService;

    // --- Objetos de Teste ---
    private Alternativas alternativas;
    private AlternativasRequestDto requestDto;
    private AlternativasResponseDto responseDto;
    private AlternativasUpdateDto updateDto;
    private AlternativasResponseDto responseDtoAtualizado;
    private Questoes questao;

    @BeforeEach
    void setUp() {
        // Entidade
        alternativas = new Alternativas();
        alternativas.setIdAlternativa(1L);
        alternativas.setAlternativa("Teste de Alternativa");
        alternativas.setAlternativaCorreta(true);

        // DTO de Requisição
        requestDto = new AlternativasRequestDto();
        requestDto.setAlternativa("Teste de Alternativa");
        requestDto.setAlternativaCorreta(true);
        requestDto.setIdQuestao(1L);

        // DTO de Resposta
        responseDto = new AlternativasResponseDto();
        responseDto.setIdAlternativa(1L);
        responseDto.setAlternativa("Teste de Alternativa");
        responseDto.setAlternativaCorreta(true);

        // DTO de Update
        updateDto = new AlternativasUpdateDto(
                Optional.of("Nova Alternativa"),
                Optional.of(false) // Atualizando para falso
        );

        // Adicione um objeto Questao
        questao = new Questoes();
        questao.setIdQuestao(1L);
        questao.setEnunciado("Teste");

        // DTO de Resposta (para o update)
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

        // Act (Executar o método)
        AlternativasResponseDto result = alternativasService.createAlternativa(requestDto);

        // Assert (Verificar o resultado)
        assertNotNull(result);
        assertEquals(responseDto.getIdAlternativa(), result.getIdAlternativa());
        assertEquals(responseDto.getAlternativa(), result.getAlternativa());

        // Verificar se os mocks foram chamados
        verify(questoesRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(requestDto, Alternativas.class);
        verify(alternativasRepository, times(1)).save(alternativas);
        verify(modelMapper, times(1)).map(alternativas, AlternativasResponseDto.class);
    }

    @Test
    void testGetAlternativaById_Success() {
        // Arrange
        Long id = 1L;
        when(alternativasRepository.findById(id)).thenReturn(Optional.of(alternativas));
        when(modelMapper.map(alternativas, AlternativasResponseDto.class)).thenReturn(responseDto);

        // Act
        AlternativasResponseDto result = alternativasService.getAlternativaById(id);

        // Assert
        assertNotNull(result);
        assertEquals(responseDto.getIdAlternativa(), result.getIdAlternativa());
        verify(alternativasRepository, times(1)).findById(id);
        verify(modelMapper, times(1)).map(alternativas, AlternativasResponseDto.class);
    }

    @Test
    void testGetAlternativaById_NotFound() {
        // Arrange
        Long id = 1L;
        when(alternativasRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            alternativasService.getAlternativaById(id);
        });

        // Verifica que o mapper nunca foi chamado
        verify(alternativasRepository, times(1)).findById(id);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testGetAllAlternativas_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        long totalElements = 1L;

        // A lista de DTOs que esperamos como conteúdo
        List<AlternativasResponseDto> dtoList = List.of(responseDto);

        // Página de Entidades (retorno do Repository)
        // O PageImpl calcula totalPages, isLast, etc., automaticamente.
        Page<Alternativas> alternativaPage = new PageImpl<>(List.of(alternativas), pageable, totalElements);

        // 1. CRIE o objeto PagedResponse (o tipo de retorno real do mapper)
        // USANDO O @AllArgsConstructor, passando todos os 6 campos
        PagedResponse<AlternativasResponseDto> pagedResponse = new PagedResponse<>(
                dtoList, // private List<T> content;
                alternativaPage.getNumber(), // private int page;
                alternativaPage.getSize(), // private int size;
                alternativaPage.getTotalElements(), // private long totalElements;
                alternativaPage.getTotalPages(), // private int totalPages;
                alternativaPage.isLast() // private boolean last;
        );

        // 2. Mocka o repositório
        when(alternativasRepository.findAll(pageable)).thenReturn(alternativaPage);

        // 3. Mocka o PagedResponseMapper
        when(pagedResponseMapper.toPagedResponse(alternativaPage, AlternativasResponseDto.class))
                .thenReturn(pagedResponse); // Retorna o objeto que acabamos de criar

        // Act
        PagedResponse<AlternativasResponseDto> resultPage = alternativasService.getAllAlternativas(pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(0, resultPage.getPage());
        assertTrue(resultPage.isLast());
        assertEquals(responseDto.getAlternativa(), resultPage.getContent().get(0).getAlternativa());

        // Verify
        verify(alternativasRepository, times(1)).findAll(pageable);
        verify(pagedResponseMapper, times(1)).toPagedResponse(alternativaPage, AlternativasResponseDto.class);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testDeleteAlternativa_Success() {
        // Arrange
        Long id = 1L;
        when(alternativasRepository.findById(id)).thenReturn(Optional.of(alternativas));
        doNothing().when(alternativasRepository).delete(alternativas);

        // Act
        alternativasService.deleteAlternativa(id);

        // Assert
        verify(alternativasRepository, times(1)).findById(id);
        verify(alternativasRepository, times(1)).delete(alternativas);
    }

    @Test
    void testDeleteAlternativa_NotFound() {
        // Arrange
        Long id = 1L;
        when(alternativasRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            alternativasService.deleteAlternativa(id);
        });

        verify(alternativasRepository, times(1)).findById(id);
        verify(alternativasRepository, never()).delete(any());
    }

    @Test
    void testUpdateAlternativa_Success() {
        // Arrange
        Long id = 1L;

        // 1. Encontra a alternativa original
        when(alternativasRepository.findById(id)).thenReturn(Optional.of(alternativas));

        // 2. Mocka o save. O thenAnswer garante que a entidade retornada
        // é a mesma que foi modificada no serviço.
        when(alternativasRepository.save(any(Alternativas.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 3. Mocka o mapper para converter a entidade ATUALIZADA para o DTO de resposta
        when(modelMapper.map(any(Alternativas.class), eq(AlternativasResponseDto.class)))
                .thenReturn(responseDtoAtualizado); // Retorna o DTO com dados atualizados

        // Act
        AlternativasResponseDto result = alternativasService.updateAlternativa(id, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Nova Alternativa", result.getAlternativa());
        assertEquals(false, result.getAlternativaCorreta());

        // Captura a entidade que foi enviada para o método save()
        ArgumentCaptor<Alternativas> alternativaCaptor = ArgumentCaptor.forClass(Alternativas.class);

        verify(alternativasRepository, times(1)).findById(id);
        verify(alternativasRepository, times(1)).save(alternativaCaptor.capture());
        verify(modelMapper, times(1)).map(any(Alternativas.class), eq(AlternativasResponseDto.class));

        // Verifica se os campos da entidade capturada foram atualizados ANTES de salvar
        // (Isso testa a lógica de "ifPresent" dentro do seu serviço)
        Alternativas savedAlternativa = alternativaCaptor.getValue();
        assertEquals("Nova Alternativa", savedAlternativa.getAlternativa());
        assertEquals(false, savedAlternativa.getAlternativaCorreta());
        assertEquals(1L, savedAlternativa.getIdAlternativa());
    }

    @Test
    void testUpdateAlternativa_NotFound() {
        // Arrange
        Long id = 1L;
        when(alternativasRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            alternativasService.updateAlternativa(id, updateDto);
        });

        // Verifica que nada além do findById foi chamado
        verify(alternativasRepository, times(1)).findById(id);
        verify(modelMapper, never()).map(any(), any());
        verify(alternativasRepository, never()).save(any());
    }
}
