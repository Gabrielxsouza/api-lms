package br.ifsp.lms_api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioRequestDto;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.AtividadeQuestionarioService;

@WebMvcTest(AtividadeQuestionarioController.class)
class AtividadeQuestionarioControllerTest {

    @Autowired
    private MockMvc mockMvc; 

    @Autowired
    private ObjectMapper objectMapper; 

    @MockBean 
    private AtividadeQuestionarioService atividadeQuestionarioService;

    private AtividadeQuestionarioResponseDto responseDto;

    @BeforeEach
    void setUp() {
        responseDto = new AtividadeQuestionarioResponseDto();
        responseDto.setIdAtividade(1L);
        responseDto.setTituloAtividade("Teste de Questionário");
        responseDto.setNumeroTentativas(3);

        objectMapper.findAndRegisterModules();
    }

    @Test
    void testCreate_Success() throws Exception {
        AtividadeQuestionarioRequestDto requestDto = new AtividadeQuestionarioRequestDto();
        requestDto.setTituloAtividade("Novo Questionário");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(7));
        requestDto.setStatusAtividade(true);
        requestDto.setNumeroTentativas(3);

        when(atividadeQuestionarioService.createAtividadeQuestionario(any(AtividadeQuestionarioRequestDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(post("/atividades-questionario") 
                .contentType(MediaType.APPLICATION_JSON) 
                .content(objectMapper.writeValueAsString(requestDto))) 
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.idAtividade").value(1L)) 
                .andExpect(jsonPath("$.tituloAtividade").value("Teste de Questionário"));

        verify(atividadeQuestionarioService, times(1)).createAtividadeQuestionario(any(AtividadeQuestionarioRequestDto.class));
    }
    

    @Test
    void testGetAtividadeQuestionarioById_Success() throws Exception {
        Long id = 1L;
        when(atividadeQuestionarioService.getAtividadeQuestionarioById(id)).thenReturn(responseDto);

        mockMvc.perform(get("/atividades-questionario/{id}", id)) 
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.idAtividade").value(id))
                .andExpect(jsonPath("$.numeroTentativas").value(3));

        verify(atividadeQuestionarioService).getAtividadeQuestionarioById(id);
    }

    @Test
    void testGetAtividadeQuestionarioById_NotFound() throws Exception {
        Long idInexistente = 99L;
        String errorMessage = "Atividade não encontrada";

        when(atividadeQuestionarioService.getAtividadeQuestionarioById(idInexistente))
            .thenThrow(new ResourceNotFoundException(errorMessage));

        mockMvc.perform(get("/atividades-questionario/{id}", idInexistente))
                .andExpect(status().isNotFound()); 

        verify(atividadeQuestionarioService).getAtividadeQuestionarioById(idInexistente);
    }

    @Test
    void testAdicionarQuestoesAoQuestionario_Success() throws Exception {
        Long idQuestionario = 1L;
        List<Long> idsDasQuestoes = List.of(10L, 11L);

        when(atividadeQuestionarioService.adicionarQuestoes(idQuestionario, idsDasQuestoes))
            .thenReturn(responseDto);

        mockMvc.perform(post("/atividades-questionario/{idQuestionario}/questoes", idQuestionario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(idsDasQuestoes))) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(1L));

        verify(atividadeQuestionarioService).adicionarQuestoes(idQuestionario, idsDasQuestoes);
    }
}
