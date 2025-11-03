// Salve este arquivo em:
// src/test/java/br/ifsp/lms_api/controller/AtividadeTextoControllerTest.java

package br.ifsp.lms_api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
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

import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.AtividadeTextoService;

@WebMvcTest(AtividadeTextoController.class) // 1. Indica qual Controller testar
class AtividadeTextoControllerTest {

    @Autowired
    private MockMvc mockMvc; // 2. Objeto para simular requisições HTTP

    @Autowired
    private ObjectMapper objectMapper; // 3. Para converter objetos Java <-> JSON

    @MockBean // 4. Usa @MockBean (do Spring) para mockar o Service
    private AtividadeTextoService atividadeTextoService;

    private AtividadeTextoResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Objeto de resposta padrão para os testes
        responseDto = new AtividadeTextoResponseDto();
        responseDto.setIdAtividade(1L);
        responseDto.setTituloAtividade("Teste de Atividade de Texto");
        responseDto.setNumeroMaximoCaracteres(500L);
        
        // Configura o ObjectMapper para lidar com LocalDate (boa prática)
        objectMapper.findAndRegisterModules(); 
    }

    @Test
    void testCreate_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        // Cria o DTO de requisição que enviaremos no "body"
        AtividadeTextoRequestDto requestDto = new AtividadeTextoRequestDto();
        requestDto.setTituloAtividade("Nova Atividade");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(7));
        requestDto.setStatusAtividade(true);
        requestDto.setNumeroMaximoCaracteres(500L);

        // Simula o Service
        when(atividadeTextoService.createAtividadeTexto(any(AtividadeTextoRequestDto.class)))
            .thenReturn(responseDto);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(post("/atividades-texto") // URL Base
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))) // Converte o DTO para JSON
                .andExpect(status().isCreated()) // Verifica o Status HTTP 201 (Created)
                .andExpect(jsonPath("$.idAtividade").value(1L))
                .andExpect(jsonPath("$.tituloAtividade").value("Teste de Atividade de Texto"));
        
        verify(atividadeTextoService, times(1)).createAtividadeTexto(any(AtividadeTextoRequestDto.class));
    }

    @Test
    void testGetAll_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        
        // 1. Crie um mock 100% falso (pois PagedResponse não tem construtor vazio)
        PagedResponse<AtividadeTextoResponseDto> pagedResponse = mock(PagedResponse.class);
        List<AtividadeTextoResponseDto> content = List.of(responseDto);

        // 2. Configure os GETTERs corretos (getPage, getSize)
        when(pagedResponse.getContent()).thenReturn(content);
        when(pagedResponse.getPage()).thenReturn(0);
        when(pagedResponse.getSize()).thenReturn(10);
        when(pagedResponse.getTotalElements()).thenReturn(1L);

        // 3. Configure o service para retornar o mock
        when(atividadeTextoService.getAllAtividadesTexto(any(Pageable.class))).thenReturn(pagedResponse);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(get("/atividades-texto")) // URL Base
                .andExpect(status().isOk()) // Verifica o Status HTTP 200
                .andExpect(jsonPath("$.content[0].idAtividade").value(1L))
                .andExpect(jsonPath("$.page").value(0)) // Verifica o campo 'page'
                .andExpect(jsonPath("$.totalElements").value(1));
        
        verify(atividadeTextoService, times(1)).getAllAtividadesTexto(any(Pageable.class));
    }

    @Test
    void testUpdate_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long id = 1L;
        
        // DTO de requisição para o PATCH
        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        // **CORREÇÃO IMPORTANTE**: Usando Optional.of() nos setters
        updateDto.setTituloAtividade(Optional.of("Título Atualizado"));
        updateDto.setNumeroMaximoCaracteres(Optional.of(1000L)); // 'L' para Long

        // Simula o service
        when(atividadeTextoService.updateAtividadeTexto(eq(id), any(AtividadeTextoUpdateDto.class)))
            .thenReturn(responseDto); // Retorna o DTO de resposta padrão

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(patch("/atividades-texto/{id}", id) // URL com ID
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk()) // Verifica o Status HTTP 200
                .andExpect(jsonPath("$.idAtividade").value(1L));

        verify(atividadeTextoService, times(1)).updateAtividadeTexto(eq(id), any(AtividadeTextoUpdateDto.class));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long idInexistente = 99L;
        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Título Atualizado"));

        String errorMessage = String.format("Atividade de Texto com ID %d não encontrada.", idInexistente);
        
        // Simula o Service: "Quando o service.update for chamado, lance uma exceção"
        when(atividadeTextoService.updateAtividadeTexto(eq(idInexistente), any(AtividadeTextoUpdateDto.class)))
            .thenThrow(new ResourceNotFoundException(errorMessage));

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(patch("/atividades-texto/{id}", idInexistente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound()); // Verifica o Status HTTP 404
        
        verify(atividadeTextoService, times(1)).updateAtividadeTexto(eq(idInexistente), any(AtividadeTextoUpdateDto.class));
    }

    @Test
    void testDelete_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long id = 1L;
        
        // Simula o Service: (Método 'void' é mockado com doNothing())
        doNothing().when(atividadeTextoService).deleteAtividadeTexto(id);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(delete("/atividades-texto/{id}", id)) // URL com ID
                .andExpect(status().isNoContent()); // Verifica o Status HTTP 204
        
        verify(atividadeTextoService, times(1)).deleteAtividadeTexto(id);
    }
}