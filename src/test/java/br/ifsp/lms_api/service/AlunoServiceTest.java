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
import org.springframework.security.crypto.password.PasswordEncoder;

import br.ifsp.lms_api.dto.alunoDto.AlunoRequestDto;
import br.ifsp.lms_api.dto.alunoDto.AlunoResponseDto;
import br.ifsp.lms_api.dto.alunoDto.AlunoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.repository.AlunoRepository;

@ExtendWith(MockitoExtension.class)
public class AlunoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AlunoService alunoService;

    private Aluno aluno;
    private AlunoRequestDto requestDto;
    private AlunoResponseDto responseDto;
    private AlunoUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        aluno = new Aluno();
        aluno.setIdUsuario(1L);
        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno@test.com");
        aluno.setSenha("senhaHash");
        aluno.setCpf("123.456.789-00");
        aluno.setRa("RA123456");

        requestDto = new AlunoRequestDto(
            "Aluno Teste",
            "aluno@test.com",
            "senha123",
            "123.456.789-00",
            "RA123456"
        );

        responseDto = new AlunoResponseDto(
            1L,
            "Aluno Teste",
            "aluno@test.com",
            "123.456.789-00",
            "RA123456"
        );

        updateDto = new AlunoUpdateDto(
            Optional.of("Aluno Atualizado"),
            Optional.empty(),
            Optional.of("novaSenha123"),
            Optional.empty()
        );
    }

    @Test
    void testCreateAluno_Success() {
        when(modelMapper.map(requestDto, Aluno.class)).thenReturn(aluno);
        when(passwordEncoder.encode(requestDto.getSenha())).thenReturn("senhaHash");
        when(alunoRepository.save(aluno)).thenReturn(aluno);
        when(modelMapper.map(aluno, AlunoResponseDto.class)).thenReturn(responseDto);

        AlunoResponseDto result = alunoService.createAluno(requestDto);

        assertNotNull(result);
        assertEquals("Aluno Teste", result.getNome());
        
        verify(passwordEncoder).encode(requestDto.getSenha());
        verify(alunoRepository).save(aluno);
    }

    @Test
    void testGetAlunoById_Success() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(modelMapper.map(aluno, AlunoResponseDto.class)).thenReturn(responseDto);

        AlunoResponseDto result = alunoService.getAlunoById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdUsuario());
    }

    @Test
    void testGetAlunoById_NotFound() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            alunoService.getAlunoById(1L);
        });
    }

    @Test
    void testUpdateAluno_Success() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novaSenhaHash");
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(inv -> inv.getArgument(0));
        
        // Simulando o retorno atualizado
        AlunoResponseDto updatedResponse = new AlunoResponseDto(1L, "Aluno Atualizado", "aluno@test.com", "123", "RA123");
        when(modelMapper.map(any(Aluno.class), eq(AlunoResponseDto.class))).thenReturn(updatedResponse);

        AlunoResponseDto result = alunoService.updateAluno(1L, updateDto);

        assertNotNull(result);
        assertEquals("Aluno Atualizado", result.getNome());
        
        ArgumentCaptor<Aluno> captor = ArgumentCaptor.forClass(Aluno.class);
        verify(alunoRepository).save(captor.capture());
        
        assertEquals("Aluno Atualizado", captor.getValue().getNome());
        assertEquals("novaSenhaHash", captor.getValue().getSenha());
    }

    @Test
    void testDeleteAluno_Success() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        doNothing().when(alunoRepository).delete(aluno);

        alunoService.deleteAluno(1L);

        verify(alunoRepository).delete(aluno);
    }
}