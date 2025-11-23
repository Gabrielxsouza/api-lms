package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioRequestDto;
import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioResponseDto;
import br.ifsp.lms_api.exception.LimiteTentativasException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.AtividadeQuestionario;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.model.TentativaQuestionario;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.AtividadeQuestionarioRepository;
import br.ifsp.lms_api.repository.TentativaQuestionarioRepository;

@ExtendWith(MockitoExtension.class)
class TentativaQuestionarioServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private TentativaQuestionarioRepository tentativaQuestionarioRepository;

    @Mock
    private AtividadeQuestionarioRepository questionarioRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private TentativaQuestionarioService service;

    @Test
    void createTentativaQuestionario_Success() {
        Long idAluno = 1L;
        Long idQuestionario = 10L;
        List<Long> respostasAluno = Arrays.asList(100L, 201L);

        TentativaQuestionarioRequestDto request = new TentativaQuestionarioRequestDto();
        request.setIdQuestionario(idQuestionario);
        request.setIdAluno(idAluno);
        request.setRespostas(respostasAluno);

        AtividadeQuestionario questionario = new AtividadeQuestionario();
        questionario.setIdAtividade(idQuestionario);
        questionario.setNumeroTentativas(3);

        Alternativas alt1Correta = new Alternativas();
        alt1Correta.setIdAlternativa(100L);
        alt1Correta.setAlternativaCorreta(true);

        Alternativas alt1Errada = new Alternativas();
        alt1Errada.setIdAlternativa(101L);
        alt1Errada.setAlternativaCorreta(false);

        Questoes q1 = new Questoes();
        q1.setAlternativas(Arrays.asList(alt1Correta, alt1Errada));

        Alternativas alt2Correta = new Alternativas();
        alt2Correta.setIdAlternativa(200L);
        alt2Correta.setAlternativaCorreta(true);

        Alternativas alt2Errada = new Alternativas();
        alt2Errada.setIdAlternativa(201L);
        alt2Errada.setAlternativaCorreta(false);

        Questoes q2 = new Questoes();
        q2.setAlternativas(Arrays.asList(alt2Correta, alt2Errada));

        questionario.setQuestoes(Arrays.asList(q1, q2));

        Aluno aluno = new Aluno();
        aluno.setIdUsuario(idAluno);

        TentativaQuestionario tentativaSalva = new TentativaQuestionario();
        tentativaSalva.setAtividadeQuestionario(questionario);
        tentativaSalva.setAluno(aluno);
        tentativaSalva.setNota(5.0);

        TentativaQuestionarioResponseDto responseDto = new TentativaQuestionarioResponseDto();

        when(questionarioRepository.findById(idQuestionario)).thenReturn(Optional.of(questionario));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));
        when(tentativaQuestionarioRepository.findByAtividadeQuestionario_IdAndAluno_Id(idQuestionario, idAluno))
                .thenReturn(Collections.emptyList());
        when(tentativaQuestionarioRepository.save(any(TentativaQuestionario.class))).thenReturn(tentativaSalva);
        when(modelMapper.map(tentativaSalva, TentativaQuestionarioResponseDto.class)).thenReturn(responseDto);

        TentativaQuestionarioResponseDto result = service.createTentativaQuestionario(request, idAluno);

        assertNotNull(result);
        verify(tentativaQuestionarioRepository).save(any(TentativaQuestionario.class));
    }

    @Test
    void createTentativaQuestionario_QuestionarioNotFound() {
        Long idAluno = 1L;
        TentativaQuestionarioRequestDto request = new TentativaQuestionarioRequestDto();
        request.setIdQuestionario(99L);
        request.setIdAluno(idAluno);

        when(questionarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.createTentativaQuestionario(request, idAluno));
    }

    @Test
    void createTentativaQuestionario_AlunoNotFound() {
        Long idAluno = 1L;
        Long idQuestionario = 10L;
        TentativaQuestionarioRequestDto request = new TentativaQuestionarioRequestDto();
        request.setIdQuestionario(idQuestionario);
        request.setIdAluno(idAluno);

        when(questionarioRepository.findById(idQuestionario)).thenReturn(Optional.of(new AtividadeQuestionario()));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.createTentativaQuestionario(request, idAluno));
    }

    @Test
    void createTentativaQuestionario_LimitReached() {
        Long idAluno = 1L;
        Long idQuestionario = 10L;
        TentativaQuestionarioRequestDto request = new TentativaQuestionarioRequestDto();
        request.setIdQuestionario(idQuestionario);
        request.setIdAluno(idAluno);

        AtividadeQuestionario questionario = new AtividadeQuestionario();
        questionario.setNumeroTentativas(1);

        Aluno aluno = new Aluno();
        List<TentativaQuestionario> tentativasAnteriores = new ArrayList<>();
        tentativasAnteriores.add(new TentativaQuestionario());

        when(questionarioRepository.findById(idQuestionario)).thenReturn(Optional.of(questionario));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));
        when(tentativaQuestionarioRepository.findByAtividadeQuestionario_IdAndAluno_Id(idQuestionario, idAluno))
                .thenReturn(tentativasAnteriores);

        assertThrows(LimiteTentativasException.class, () -> service.createTentativaQuestionario(request, idAluno));
    }

    @Test
    void getAllTentativasQuestionario_Success() {
        Pageable pageable = Pageable.unpaged();
        Page<TentativaQuestionario> page = new PageImpl<>(Collections.emptyList());

        PagedResponse<TentativaQuestionarioResponseDto> pagedResponse = new PagedResponse<>(
            Collections.emptyList(), 0, 10, 0, 0, true
        );

        when(tentativaQuestionarioRepository.findAll(pageable)).thenReturn(page);
        when(pagedResponseMapper.toPagedResponse(page, TentativaQuestionarioResponseDto.class)).thenReturn(pagedResponse);

        PagedResponse<TentativaQuestionarioResponseDto> result = service.getAllTentativasQuestionario(pageable);

        assertNotNull(result);
        verify(tentativaQuestionarioRepository).findAll(pageable);
    }

    @Test
    void getTentativasQuestionarioByAlunoId_Success() {
        Long idAluno = 1L;
        Pageable pageable = Pageable.unpaged();
        Page<TentativaQuestionario> page = new PageImpl<>(Collections.emptyList());

        PagedResponse<TentativaQuestionarioResponseDto> pagedResponse = new PagedResponse<>(
            Collections.emptyList(), 0, 10, 0, 0, true
        );

        when(tentativaQuestionarioRepository.findByAluno_IdUsuario(idAluno, pageable)).thenReturn(page);
        when(pagedResponseMapper.toPagedResponse(page, TentativaQuestionarioResponseDto.class)).thenReturn(pagedResponse);

        PagedResponse<TentativaQuestionarioResponseDto> result = service.getTentativasQuestionarioByAlunoId(idAluno, pageable);

        assertNotNull(result);
        verify(tentativaQuestionarioRepository).findByAluno_IdUsuario(idAluno, pageable);
    }

    @Test
    void deleteTentativaQuestionario_Success() {
        Long idTentativa = 1L;
        TentativaQuestionario attempt = new TentativaQuestionario();
        TentativaQuestionarioResponseDto responseDto = new TentativaQuestionarioResponseDto();

        when(tentativaQuestionarioRepository.findById(idTentativa)).thenReturn(Optional.of(attempt));
        when(modelMapper.map(attempt, TentativaQuestionarioResponseDto.class)).thenReturn(responseDto);

        TentativaQuestionarioResponseDto result = service.deleteTentativaQuestionario(idTentativa);

        assertNotNull(result);
        verify(tentativaQuestionarioRepository).delete(attempt);
    }

    @Test
    void deleteTentativaQuestionario_NotFound() {
        Long idTentativa = 1L;
        when(tentativaQuestionarioRepository.findById(idTentativa)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.deleteTentativaQuestionario(idTentativa));
    }
}
