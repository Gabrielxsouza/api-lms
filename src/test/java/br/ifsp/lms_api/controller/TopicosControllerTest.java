// Caminho: src/test/java/br/ifsp/lms_api/controller/TopicosControllerTest.java

package br.ifsp.lms_api.controller;

// Imports do JUnit
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Imports do Spring Test
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
// Imports Estáticos (para get(), post(), status(), etc.)
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
// Imports de DTOs, Service e Exceções
import com.fasterxml.jackson.databind.ObjectMapper;
import br.ifsp.lms_api.dto.TopicosDto.TopicosRequestDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosResponseDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.TopicosService;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@WebMvcTest(TopicosController.class) // 1. Indica qual Controller testar
class TopicosControllerTest {

    @Autowired
    private MockMvc mockMvc; // 2. Objeto para simular requisições HTTP

    @Autowired
    private ObjectMapper objectMapper; // 3. Para converter objetos Java <-> JSON

    @MockBean // 4. Usa @MockBean (do Spring) para mockar o Service
    private TopicosService topicosService;

    private TopicosResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Objeto de resposta padrão para os testes
        responseDto = new TopicosResponseDto();
        responseDto.setIdTopico(1L);
        responseDto.setTituloTopico("Tópico de Teste");
        responseDto.setConteudoHtml("<p>Conteúdo</p>");
        
        objectMapper.findAndRegisterModules(); 
    }

    @Test
    void testCreateTopico_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        TopicosRequestDto requestDto = new TopicosRequestDto();
        requestDto.setTituloTopico("Novo Tópico");
        requestDto.setIdTurma(1L);
        requestDto.setConteudoHtml("<p>HTML</p>");

        // Simula o Service
        when(topicosService.createTopico(any(TopicosRequestDto.class)))
            .thenReturn(responseDto);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(post("/topicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                // O seu controller não usa ResponseEntity.status(CREATED),
                // então o padrão do Spring é 200 OK
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.idTopico").value(1L))
                .andExpect(jsonPath("$.tituloTopico").value("Tópico de Teste"));
        
        verify(topicosService, times(1)).createTopico(any(TopicosRequestDto.class));
    }

    @Test
    void testGetAllQuestoes_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        // 1. Cria o mock 100% falso do PagedResponse
        PagedResponse<TopicosResponseDto> pagedResponse = mock(PagedResponse.class);
        List<TopicosResponseDto> content = List.of(responseDto);

        // 2. Configura os getters do mock
        when(pagedResponse.getContent()).thenReturn(content);
        when(pagedResponse.getPage()).thenReturn(0);
        when(pagedResponse.getSize()).thenReturn(10);
        when(pagedResponse.getTotalElements()).thenReturn(1L);

        // 3. Simula o service
        when(topicosService.getAllTopicos(any(Pageable.class))).thenReturn(pagedResponse);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(get("/topicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idTopico").value(1L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetTopicoById_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        when(topicosService.getTopicoById(1L)).thenReturn(responseDto);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(get("/topicos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTopico").value(1L));
    }

    @Test
    void testGetTopicoById_NotFound_404() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        // Simula o service lançando a exceção que seu @ControllerAdvice deve pegar
        when(topicosService.getTopicoById(99L))
           .thenThrow(new ResourceNotFoundException("Topico com ID 99 nao encontrado"));

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(get("/topicos/{id}", 99L))
                .andExpect(status().isNotFound()); // Espera 404
    }

    @Test
    void testGetTopicosByTurmaId_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long idTurma = 5L;
        
        // 1. Cria o mock do PagedResponse
        PagedResponse<TopicosResponseDto> pagedResponse = mock(PagedResponse.class);
        List<TopicosResponseDto> content = List.of(responseDto);

        // 2. Configura os getters
        when(pagedResponse.getContent()).thenReturn(content);
        when(pagedResponse.getTotalElements()).thenReturn(1L);

        // 3. Simula o service
        when(topicosService.getTopicosByIdTurma(eq(idTurma), any(Pageable.class)))
            .thenReturn(pagedResponse);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(get("/topicos/turma/{idTurma}", idTurma))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].idTopico").value(1L));
        
        verify(topicosService, times(1)).getTopicosByIdTurma(eq(idTurma), any(Pageable.class));
    }

    @Test
    void testDeleteTopico_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        when(topicosService.deleteTopico(1L)).thenReturn(responseDto);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(delete("/topicos/{id}", 1L))
                .andExpect(status().isOk())
                // O seu endpoint retorna o objeto deletado, então verificamos ele
                .andExpect(jsonPath("$.idTopico").value(1L)); 
    }

    @Test
    void testUpdateTopico_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long id = 1L;
        TopicosUpdateDto updateDto = new TopicosUpdateDto();
        updateDto.setTituloTopico(Optional.of("Título Novo"));
        updateDto.setConteudoHtml(Optional.of("<p>Novo</p>"));

        when(topicosService.updateTopico(eq(id), any(TopicosUpdateDto.class)))
            .thenReturn(responseDto);

        // --- 2. Act & 3. Assert (Agir e Verificar) ---
        mockMvc.perform(patch("/topicos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTopico").value(1L));
        
        verify(topicosService, times(1)).updateTopico(eq(id), any(TopicosUpdateDto.class));
    }
}