package br.ifsp.lms_api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.model.AtividadeQuestionario;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.AtividadeQuestionarioRepository;
import br.ifsp.lms_api.repository.QuestoesRepository;
import br.ifsp.lms_api.mapper.PagedResponseMapper;

@ExtendWith(MockitoExtension.class)
class AtividadeQuestionarioServiceTest {

    @Mock
    private AtividadeQuestionarioRepository atividadeQuestionarioRepository;

    @Mock
    private QuestoesRepository questoesRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private AtividadeQuestionarioService atividadeQuestionarioService;

    @Test
    void testFindEntityById_WhenNotFound_ShouldThrowException() {
        long idInexistente = 99L;
        long idProfessorQualquer = 1L; 
        String expectedMessage = "Atividade de Texto com ID 99 não encontrada.";

        when(atividadeQuestionarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
    
            atividadeQuestionarioService.updateAtividadeQuestionario(idInexistente, null, idProfessorQualquer);
        });

        assertEquals(expectedMessage, exception.getMessage());

        verify(atividadeQuestionarioRepository).findById(idInexistente);
    }

    @Test
    void testAdicionarQuestoes_Success() {
        Long idQuestionario = 1L;
        Long idProfessor = 50L;
        List<Long> idsDasQuestoes = List.of(10L, 11L);

        Professor professorMock = mock(Professor.class);

        when(professorMock.getIdUsuario()).thenReturn(idProfessor);

        Turma turmaMock = new Turma();
        turmaMock.setProfessor(professorMock);

        Topicos topicoMock = new Topicos();
        topicoMock.setTurma(turmaMock);

        AtividadeQuestionario questionarioMock = new AtividadeQuestionario();
        questionarioMock.setIdAtividade(idQuestionario);
        questionarioMock.setTopico(topicoMock); 
        questionarioMock.setQuestoes(new ArrayList<>());


        Questoes questao1 = new Questoes();
        questao1.setIdQuestao(10L);
        Questoes questao2 = new Questoes();
        questao2.setIdQuestao(11L);
        List<Questoes> questoesListMock = List.of(questao1, questao2);


        AtividadeQuestionarioResponseDto responseDtoMock = new AtividadeQuestionarioResponseDto();
        responseDtoMock.setIdAtividade(idQuestionario);


        when(atividadeQuestionarioRepository.findById(idQuestionario)).thenReturn(Optional.of(questionarioMock));
        when(questoesRepository.findAllById(idsDasQuestoes)).thenReturn(questoesListMock);
        when(atividadeQuestionarioRepository.save(any(AtividadeQuestionario.class))).thenReturn(questionarioMock);

  
        when(modelMapper.map(any(AtividadeQuestionario.class), eq(AtividadeQuestionarioResponseDto.class)))
                .thenReturn(responseDtoMock);

  
       
        when(modelMapper.map(any(Questoes.class), eq(QuestoesResponseDto.class)))
                .thenReturn(new QuestoesResponseDto());


        AtividadeQuestionarioResponseDto result = atividadeQuestionarioService.adicionarQuestoes(idQuestionario,
                idsDasQuestoes, idProfessor);

     
        assertNotNull(result);
        assertEquals(idQuestionario, result.getIdAtividade());

        assertEquals(2, questionarioMock.getQuestoes().size());
        assertTrue(questionarioMock.getQuestoes().contains(questao1));

        verify(atividadeQuestionarioRepository).findById(idQuestionario);
        verify(questoesRepository).findAllById(idsDasQuestoes);
        verify(atividadeQuestionarioRepository).save(questionarioMock);
    }

    @Test
    void testAdicionarQuestoes_WhenQuestionarioNotFound_ShouldThrowException() {
        Long idInexistente = 99L;
        Long idProfessorQualquer = 1L;
        List<Long> idsDasQuestoes = List.of(10L, 11L);

        when(atividadeQuestionarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      
            atividadeQuestionarioService.adicionarQuestoes(idInexistente, idsDasQuestoes, idProfessorQualquer);
        });

        assertEquals("Questionário não encontrado com ID: " + idInexistente, exception.getMessage());

        verify(atividadeQuestionarioRepository).findById(idInexistente);
        verify(questoesRepository, never()).findAllById(any());
        verify(atividadeQuestionarioRepository, never()).save(any());
    }
}
