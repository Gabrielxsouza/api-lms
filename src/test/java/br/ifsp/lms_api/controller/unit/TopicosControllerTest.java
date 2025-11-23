package br.ifsp.lms_api.controller.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.ifsp.lms_api.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.controller.TopicosController;
import br.ifsp.lms_api.dto.TopicosDto.TopicosRequestDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosResponseDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosUpdateDto;
import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.TopicosService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebMvcTest(TopicosController.class)
class TopicosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TopicosService topicosService;

    private TopicosResponseDto responseDto;

    @BeforeEach
    void setUp() {
        responseDto = new TopicosResponseDto();
        responseDto.setIdTopico(1L);
        responseDto.setTituloTopico("Tópico de Teste");
        responseDto.setConteudoHtml("<p>Conteúdo</p>");
        responseDto.setAtividades(new ArrayList<AtividadesResponseDto>());

        objectMapper.findAndRegisterModules();
    }

    @Test
    @WithMockUser(roles = "PROFESSOR") 
    void testCreateTopico_Success() throws Exception {
        TopicosRequestDto requestDto = new TopicosRequestDto();
        requestDto.setTituloTopico("Novo Tópico");
        requestDto.setIdTurma(1L);
        requestDto.setConteudoHtml("<p>HTML</p>");
        requestDto.setIdAtividade(List.of(10L, 11L));

        when(topicosService.createTopico(any(TopicosRequestDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(post("/topicos")
                .with(csrf()) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.idTopico").value(1L))
                .andExpect(jsonPath("$.tituloTopico").value("Tópico de Teste"))
                .andExpect(jsonPath("$.atividades").exists());

        verify(topicosService, times(1)).createTopico(any(TopicosRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN") 
    void testGetAllTopicos_Success() throws Exception {
        PagedResponse<TopicosResponseDto> pagedResponse = mock(PagedResponse.class);
        List<TopicosResponseDto> content = List.of(responseDto);

        when(pagedResponse.getContent()).thenReturn(content);
        when(pagedResponse.getPage()).thenReturn(0);
        when(pagedResponse.getSize()).thenReturn(10);
        when(pagedResponse.getTotalElements()).thenReturn(1L);

        when(topicosService.getAllTopicos(any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/topicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idTopico").value(1L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].atividades").exists());
    }

    @Test
    @WithMockUser 
    void testGetTopicoById_Success() throws Exception {
        when(topicosService.getTopicoById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/topicos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTopico").value(1L))
                .andExpect(jsonPath("$.atividades").exists());
    }

    @Test
    @WithMockUser
    void testGetTopicoById_NotFound_404() throws Exception {
        when(topicosService.getTopicoById(99L))
           .thenThrow(new ResourceNotFoundException("Topico com ID 99 nao encontrado"));

        mockMvc.perform(get("/topicos/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser 
    void testGetTopicosByTurmaId_Success() throws Exception {
        Long idTurma = 5L;

        PagedResponse<TopicosResponseDto> pagedResponse = mock(PagedResponse.class);
        List<TopicosResponseDto> content = List.of(responseDto);

        when(pagedResponse.getContent()).thenReturn(content);
        when(pagedResponse.getTotalElements()).thenReturn(1L);

        when(topicosService.getTopicosByIdTurma(eq(idTurma), any(Pageable.class)))
            .thenReturn(pagedResponse);

        mockMvc.perform(get("/topicos/turma/{idTurma}", idTurma))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].idTopico").value(1L))
                .andExpect(jsonPath("$.content[0].atividades").exists());

        verify(topicosService, times(1)).getTopicosByIdTurma(eq(idTurma), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR") 
    void testDeleteTopico_Success() throws Exception {
        when(topicosService.deleteTopico(1L)).thenReturn(responseDto);

        mockMvc.perform(delete("/topicos/{id}", 1L)
                .with(csrf())) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTopico").value(1L));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR") 
    void testUpdateTopico_Success() throws Exception {
        Long id = 1L;
        TopicosUpdateDto updateDto = new TopicosUpdateDto();
        updateDto.setTituloTopico(Optional.of("Título Novo"));
        updateDto.setConteudoHtml(Optional.of("<p>Novo</p>"));

        when(topicosService.updateTopico(eq(id), any(TopicosUpdateDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(patch("/topicos/{id}", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTopico").value(1L));

        verify(topicosService, times(1)).updateTopico(eq(id), any(TopicosUpdateDto.class));
    }
}
