// Salve este arquivo em:
// src/test/java/br/ifsp/lms_api/controller/QuestoesControllerTest.java

package br.ifsp.lms_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.questoesDto.QuestoesRequestDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesUpdateDto;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.QuestoesService;

// 1. Indica qual Controller testar
@WebMvcTest(QuestoesController.class)
class QuestoesControllerTest {

    @Autowired
    private MockMvc mockMvc; // 2. Objeto para simular requisições HTTP

    @Autowired
    private ObjectMapper objectMapper; // 3. Para converter objetos Java <-> JSON

    @MockBean // 4. Usa @MockBean (do Spring) para mockar o Service
    private QuestoesService questoesService;

    private QuestoesResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Objeto de resposta padrão para os testes
        responseDto = new QuestoesResponseDto();
        responseDto.setIdQuestao(1L);
        responseDto.setEnunciado("Qual a capital do Brasil?");
        
        // Configura o ObjectMapper (igual ao seu exemplo, boa prática)
        objectMapper.findAndRegisterModules(); 
    }

    @Test
    void testCreateQuestao_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        // Cria o DTO de requisição que enviaremos no "body"
        QuestoesRequestDto requestDto = new QuestoesRequestDto();
        requestDto.setEnunciado("Qual a capital do Brasil?");
        //... outros campos do requestDto

        // Simula o Service: "Quando o service.create for chamado, retorne o responseDto"
        when(questoesService.createQuestao(any(QuestoesRequestDto.class)))
            .thenReturn(responseDto);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(post("/questoes") // Simula um POST na URL base
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))) // Converte o DTO para JSON
                .andExpect(status().isCreated()) // Verifica o Status HTTP 201 (Created)
                .andExpect(jsonPath("$.idQuestao").value(1L))
                .andExpect(jsonPath("$.enunciado").value("Qual a capital do Brasil?"));
        
        verify(questoesService, times(1)).createQuestao(any(QuestoesRequestDto.class));
    }

   @Test
    void testGetAllQuestoes_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        
        // 1. Crie um mock 100% falso (como fizemos no outro teste)
        PagedResponse<QuestoesResponseDto> pagedResponse = mock(PagedResponse.class);

        // 2. Crie os dados que o mock deve "fingir" que tem
        List<QuestoesResponseDto> content = List.of(responseDto);

        // 3. Configure os MÉTODOS GETTER do mock para retornar os dados falsos
        // (O MockMvc usa os getters para criar o JSON)
        when(pagedResponse.getContent()).thenReturn(content);
        when(pagedResponse.getPage()).thenReturn(0);      // <-- CORRIGIDO
        when(pagedResponse.getSize()).thenReturn(10);
        when(pagedResponse.getTotalElements()).thenReturn(1L);
        when(pagedResponse.getTotalPages()).thenReturn(1);
        
        // 4. Configure o service para retornar o mock
        when(questoesService.getAllQuestoes(any(Pageable.class))).thenReturn(pagedResponse);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(get("/questoes")) // Simula um GET na URL base
                .andExpect(status().isOk()) // Verifica o Status HTTP 200
                .andExpect(jsonPath("$.content[0].idQuestao").value(1L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
        
        verify(questoesService, times(1)).getAllQuestoes(any(Pageable.class));
    }

     }