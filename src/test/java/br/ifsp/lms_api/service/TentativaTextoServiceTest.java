package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoRequestDto;
import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoResponseDto;
import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.AccessDeniedException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.model.TentativaTexto;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.AtividadeTextoRepository;
import br.ifsp.lms_api.repository.TentativaTextoRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class TentativaTextoServiceTest {

    @Mock
    private TentativaTextoRepository tentativaTextoRepository;

    @Mock
    private ModelMapper mapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private AtividadeTextoRepository atividadeTextoRepository;

    @InjectMocks
    private TentativaTextoService service;

    @Test
    void getAllTentativasTexto_Success() {
        Pageable pageable = Pageable.unpaged();
        Page<TentativaTexto> page = new PageImpl<>(java.util.Collections.emptyList());
        PagedResponse<TentativaTextoResponseDto> pagedResponse = new PagedResponse<TentativaTextoResponseDto>(null, 0, 0, 0, 0, false);

        when(tentativaTextoRepository.findAll(pageable)).thenReturn(page);
        when(pagedResponseMapper.toPagedResponse(page, TentativaTextoResponseDto.class)).thenReturn(pagedResponse);

        PagedResponse<TentativaTextoResponseDto> result = service.getAllTentativasTexto(pageable);

        assertNotNull(result);
        verify(tentativaTextoRepository).findAll(pageable);
    }

    @Test
    void createTentativaTexto_Success() {
        Long idAluno = 1L;
        Long idAtividade = 2L;
        TentativaTextoRequestDto request = new TentativaTextoRequestDto();
        request.setTextoResposta("Resposta");

        Aluno aluno = new Aluno();
        aluno.setIdUsuario(idAluno);
        AtividadeTexto atividade = new AtividadeTexto();
        atividade.setIdAtividade(idAtividade);
        TentativaTexto savedTentativa = new TentativaTexto();
        TentativaTextoResponseDto responseDto = new TentativaTextoResponseDto();

        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));
        when(atividadeTextoRepository.findById(idAtividade)).thenReturn(Optional.of(atividade));
        when(tentativaTextoRepository.save(any(TentativaTexto.class))).thenReturn(savedTentativa);
        when(mapper.map(savedTentativa, TentativaTextoResponseDto.class)).thenReturn(responseDto);

        TentativaTextoResponseDto result = service.createTentativaTexto(request, idAluno, idAtividade);

        assertNotNull(result);
        verify(tentativaTextoRepository).save(any(TentativaTexto.class));
    }

    @Test
    void createTentativaTexto_AlunoNotFound_ThrowsException() {
        Long idAluno = 1L;
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.empty());

        TentativaTextoRequestDto request = new TentativaTextoRequestDto();

        assertThrows(EntityNotFoundException.class, () -> service.createTentativaTexto(request, idAluno, 2L));
    }

    @Test
    void createTentativaTexto_AtividadeNotFound_ThrowsException() {
        Long idAluno = 1L;
        Long idAtividade = 2L;
        Aluno aluno = new Aluno();

        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));
        when(atividadeTextoRepository.findById(idAtividade)).thenReturn(Optional.empty());

        TentativaTextoRequestDto request = new TentativaTextoRequestDto();

        assertThrows(EntityNotFoundException.class, () -> service.createTentativaTexto(request, idAluno, idAtividade));
    }

    @Test
    void updateTentativaTextoProfessor_Success() {
        Long idTentativa = 1L;
        TentativaTextoUpdateDto updateDto = new TentativaTextoUpdateDto();
        updateDto.setNota(Optional.of(10.0));
        updateDto.setFeedback(Optional.of("Bom"));

        TentativaTexto tentativa = new TentativaTexto();
        TentativaTextoResponseDto responseDto = new TentativaTextoResponseDto();

        when(tentativaTextoRepository.findById(idTentativa)).thenReturn(Optional.of(tentativa));
        when(tentativaTextoRepository.save(tentativa)).thenReturn(tentativa);
        when(mapper.map(tentativa, TentativaTextoResponseDto.class)).thenReturn(responseDto);

        TentativaTextoResponseDto result = service.updateTentativaTextoProfessor(updateDto, idTentativa);

        assertNotNull(result);
        verify(tentativaTextoRepository).save(tentativa);
    }

    @Test
    void updateTentativaTextoProfessor_NotFound_ThrowsException() {
        Long idTentativa = 1L;
        TentativaTextoUpdateDto updateDto = new TentativaTextoUpdateDto();
        when(tentativaTextoRepository.findById(idTentativa)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateTentativaTextoProfessor(updateDto, idTentativa));
    }

    @Test
    void updateTentativaTextoAluno_Success() {
        Long idTentativa = 1L;
        Long idAluno = 1L;
        TentativaTextoUpdateDto updateDto = new TentativaTextoUpdateDto();
        updateDto.setTextoResposta(Optional.of("Novo texto"));

        Aluno aluno = new Aluno();
        aluno.setIdUsuario(idAluno);
        TentativaTexto tentativa = new TentativaTexto();
        tentativa.setAluno(aluno);
        tentativa.setNota(null);

        TentativaTextoResponseDto responseDto = new TentativaTextoResponseDto();

        when(tentativaTextoRepository.findById(idTentativa)).thenReturn(Optional.of(tentativa));
        when(tentativaTextoRepository.save(tentativa)).thenReturn(tentativa);
        when(mapper.map(tentativa, TentativaTextoResponseDto.class)).thenReturn(responseDto);

        TentativaTextoResponseDto result = service.updateTentativaTextoAluno(updateDto, idTentativa, idAluno);

        assertNotNull(result);
        verify(tentativaTextoRepository).save(tentativa);
    }

    @Test
    void updateTentativaTextoAluno_AccessDenied_NotOwner() {
        Long idTentativa = 1L;
        Long idAluno = 1L;
        Long otherIdAluno = 2L;

        Aluno owner = new Aluno();
        owner.setIdUsuario(otherIdAluno);
        TentativaTexto tentativa = new TentativaTexto();
        tentativa.setAluno(owner);

        when(tentativaTextoRepository.findById(idTentativa)).thenReturn(Optional.of(tentativa));

        TentativaTextoUpdateDto updateDto = new TentativaTextoUpdateDto();

        assertThrows(AccessDeniedException.class, () -> service.updateTentativaTextoAluno(updateDto, idTentativa, idAluno));
    }

    @Test
    void updateTentativaTextoAluno_AccessDenied_AlreadyGraded() {
        Long idTentativa = 1L;
        Long idAluno = 1L;

        Aluno aluno = new Aluno();
        aluno.setIdUsuario(idAluno);
        TentativaTexto tentativa = new TentativaTexto();
        tentativa.setAluno(aluno);
        tentativa.setNota(8.0);

        when(tentativaTextoRepository.findById(idTentativa)).thenReturn(Optional.of(tentativa));

        TentativaTextoUpdateDto updateDto = new TentativaTextoUpdateDto();

        assertThrows(AccessDeniedException.class, () -> service.updateTentativaTextoAluno(updateDto, idTentativa, idAluno));
    }

    @Test
    void deleteTentativaTexto_Success() {
        Long idTentativa = 1L;
        Long idAluno = 1L;

        Aluno aluno = new Aluno();
        aluno.setIdUsuario(idAluno);
        TentativaTexto tentativa = new TentativaTexto();
        tentativa.setAluno(aluno);
        tentativa.setNota(null);

        TentativaTextoResponseDto responseDto = new TentativaTextoResponseDto();

        when(tentativaTextoRepository.findById(idTentativa)).thenReturn(Optional.of(tentativa));
        when(mapper.map(tentativa, TentativaTextoResponseDto.class)).thenReturn(responseDto);

        TentativaTextoResponseDto result = service.deleteTentativaTexto(idTentativa, idAluno);

        assertNotNull(result);
        verify(tentativaTextoRepository).delete(tentativa);
    }

    @Test
    void deleteTentativaTexto_AccessDenied_NotOwner() {
        Long idTentativa = 1L;
        Long idAluno = 1L;
        Long otherId = 2L;

        Aluno owner = new Aluno();
        owner.setIdUsuario(otherId);
        TentativaTexto tentativa = new TentativaTexto();
        tentativa.setAluno(owner);

        when(tentativaTextoRepository.findById(idTentativa)).thenReturn(Optional.of(tentativa));

        assertThrows(AccessDeniedException.class, () -> service.deleteTentativaTexto(idTentativa, idAluno));
    }

    @Test
    void deleteTentativaTexto_AccessDenied_AlreadyGraded() {
        Long idTentativa = 1L;
        Long idAluno = 1L;

        Aluno aluno = new Aluno();
        aluno.setIdUsuario(idAluno);
        TentativaTexto tentativa = new TentativaTexto();
        tentativa.setAluno(aluno);
        tentativa.setNota(5.0);

        when(tentativaTextoRepository.findById(idTentativa)).thenReturn(Optional.of(tentativa));

        assertThrows(AccessDeniedException.class, () -> service.deleteTentativaTexto(idTentativa, idAluno));
    }
}
