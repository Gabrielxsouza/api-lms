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

@WebMvcTest(AtividadeTextoController.class) 
class AtividadeTextoControllerTest {

    @Autowired
    private MockMvc mockMvc; 

    @Autowired
    private ObjectMapper objectMapper; 

    @MockBean 
    private AtividadeTextoService atividadeTextoService;

    private AtividadeTextoResponseDto responseDto;

    @BeforeEach
    void setUp() {
        responseDto = new AtividadeTextoResponseDto();
        responseDto.setIdAtividade(1L);
        responseDto.setTituloAtividade("Teste de Atividade de Texto");
        responseDto.setNumeroMaximoCaracteres(500L);
        
        objectMapper.findAndRegisterModules(); 
    }

    @Test
    void testCreate_Success() throws Exception {
        AtividadeTextoRequestDto requestDto = new AtividadeTextoRequestDto();
        requestDto.setTituloAtividade("Nova Atividade");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(7));
        requestDto.setStatusAtividade(true);
        requestDto.setNumeroMaximoCaracteres(500L);

        when(atividadeTextoService.createAtividadeTexto(any(AtividadeTextoRequestDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(post("/atividades-texto") 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))) 
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.idAtividade").value(1L))
                .andExpect(jsonPath("$.tituloAtividade").value("Teste de Atividade de Texto"));
        
        verify(atividadeTextoService, times(1)).createAtividadeTexto(any(AtividadeTextoRequestDto.class));
    }

    @Test
    void testGetAll_Success() throws Exception {
        PagedResponse<AtividadeTextoResponseDto> pagedResponse = mock(PagedResponse.class);
        List<AtividadeTextoResponseDto> content = List.of(responseDto);

        when(pagedResponse.getContent()).thenReturn(content);
        when(pagedResponse.getPage()).thenReturn(0);
        when(pagedResponse.getSize()).thenReturn(10);
        when(pagedResponse.getTotalElements()).thenReturn(1L);

        when(atividadeTextoService.getAllAtividadesTexto(any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/atividades-texto")) 
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.content[0].idAtividade").value(1L))
                .andExpect(jsonPath("$.page").value(0)) 
                .andExpect(jsonPath("$.totalElements").value(1));
        
        verify(atividadeTextoService, times(1)).getAllAtividadesTexto(any(Pageable.class));
    }

    @Test
    void testUpdate_Success() throws Exception {
        Long id = 1L;
        
        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Título Atualizado"));
        updateDto.setNumeroMaximoCaracteres(Optional.of(1000L)); 

        when(atividadeTextoService.updateAtividadeTexto(eq(id), any(AtividadeTextoUpdateDto.class)))
            .thenReturn(responseDto); 

        mockMvc.perform(patch("/atividades-texto/{id}", id) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.idAtividade").value(1L));
    
        verify(atividadeTextoService, times(1)).updateAtividadeTexto(eq(id), any(AtividadeTextoUpdateDto.class));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        Long idInexistente = 99L;
        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Título Atualizado"));

        String errorMessage = String.format("Atividade de Texto com ID %d não encontrada.", idInexistente);
        
        when(atividadeTextoService.updateAtividadeTexto(eq(idInexistente), any(AtividadeTextoUpdateDto.class)))
            .thenThrow(new ResourceNotFoundException(errorMessage));
    
        mockMvc.perform(patch("/atividades-texto/{id}", idInexistente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound()); 
        
        verify(atividadeTextoService, times(1)).updateAtividadeTexto(eq(idInexistente), any(AtividadeTextoUpdateDto.class));
    }

    @Test
    void testDelete_Success() throws Exception {
        Long id = 1L;
        
        doNothing().when(atividadeTextoService).deleteAtividadeTexto(id);
    
        mockMvc.perform(delete("/atividades-texto/{id}", id)) 
                .andExpect(status().isNoContent()); 
        
        verify(atividadeTextoService, times(1)).deleteAtividadeTexto(id);
    }
}
