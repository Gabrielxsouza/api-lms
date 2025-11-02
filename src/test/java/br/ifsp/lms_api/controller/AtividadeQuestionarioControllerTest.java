// src/test/java/br/ifsp/lms_api/controller/AtividadeQuestionarioControllerTest.java
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

@WebMvcTest(AtividadeQuestionarioController.class) // 1. Indica qual Controller testar
class AtividadeQuestionarioControllerTest {

    @Autowired
    private MockMvc mockMvc; // 2. Objeto para simular requisições HTTP

    @Autowired
    private ObjectMapper objectMapper; // 3. Para converter objetos Java -> JSON e vice-versa

    @MockBean // 4. Usa @MockBean (do Spring) para mockar o Service
    private AtividadeQuestionarioService atividadeQuestionarioService;

    private AtividadeQuestionarioResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Objeto de resposta padrão para os testes
        responseDto = new AtividadeQuestionarioResponseDto();
        responseDto.setIdAtividade(1L);
        responseDto.setTituloAtividade("Teste de Questionário");
        responseDto.setNumeroTentativas(3);
        
        // Configura o ObjectMapper para lidar com LocalDate
        objectMapper.findAndRegisterModules(); 
    }

    @Test
    void testCreate_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        // Cria o DTO de requisição que enviaremos no "body"
        AtividadeQuestionarioRequestDto requestDto = new AtividadeQuestionarioRequestDto();
        requestDto.setTituloAtividade("Novo Questionário");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(7));
        requestDto.setStatusAtividade(true);
        requestDto.setNumeroTentativas(3);

        // Simula o Service: "Quando o service.create for chamado com QUALQUER
        // AtividadeQuestionarioRequestDto, então retorne o responseDto"
        when(atividadeQuestionarioService.createAtividadeQuestionario(any(AtividadeQuestionarioRequestDto.class)))
            .thenReturn(responseDto);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(post("/atividades-questionario") // Simula um POST
                .contentType(MediaType.APPLICATION_JSON) // Define o tipo de conteúdo
                .content(objectMapper.writeValueAsString(requestDto))) // Converte o DTO para JSON
                .andExpect(status().isCreated()) // Verifica o Status HTTP 201
                .andExpect(jsonPath("$.idAtividade").value(1L)) // Verifica o JSON de resposta
                .andExpect(jsonPath("$.tituloAtividade").value("Teste de Questionário"));
        
        // Verifica se o método do service foi chamado 1 vez
        verify(atividadeQuestionarioService, times(1)).createAtividadeQuestionario(any(AtividadeQuestionarioRequestDto.class));
    }

    @Test
    void testGetAtividadeQuestionarioById_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long id = 1L;
        // Simula o Service: "Quando o service.getById(1L) for chamado, retorne o responseDto"
        when(atividadeQuestionarioService.getAtividadeQuestionarioById(id)).thenReturn(responseDto);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(get("/atividades-questionario/{id}", id)) // Simula um GET com PathVariable
                .andExpect(status().isOk()) // Verifica o Status HTTP 200
                .andExpect(jsonPath("$.idAtividade").value(id))
                .andExpect(jsonPath("$.numeroTentativas").value(3));
        
        verify(atividadeQuestionarioService).getAtividadeQuestionarioById(id);
    }
    
    @Test
    void testGetAtividadeQuestionarioById_NotFound() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long idInexistente = 99L;
        String errorMessage = "Atividade não encontrada";
        
        // Simula o Service: "Quando o service.getById(99L) for chamado, lance uma exceção"
        when(atividadeQuestionarioService.getAtividadeQuestionarioById(idInexistente))
            .thenThrow(new ResourceNotFoundException(errorMessage));

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(get("/atividades-questionario/{id}", idInexistente))
                .andExpect(status().isNotFound()); // Verifica o Status HTTP 404
        
        // (Isso assume que você tem um @ControllerAdvice para tratar ResourceNotFoundException)
        
        verify(atividadeQuestionarioService).getAtividadeQuestionarioById(idInexistente);
    }

    @Test
    void testAdicionarQuestoesAoQuestionario_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long idQuestionario = 1L;
        List<Long> idsDasQuestoes = List.of(10L, 11L);

        // Simula o service
        when(atividadeQuestionarioService.adicionarQuestoes(idQuestionario, idsDasQuestoes))
            .thenReturn(responseDto);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(post("/atividades-questionario/{idQuestionario}/questoes", idQuestionario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(idsDasQuestoes))) // Envia a lista de IDs no body
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(1L));

        verify(atividadeQuestionarioService).adicionarQuestoes(idQuestionario, idsDasQuestoes);
    }
}