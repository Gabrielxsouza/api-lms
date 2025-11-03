package br.ifsp.lms_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; 

import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.AtividadeArquivosService;

@WebMvcTest(AtividadeArquivosController.class)
public class AtividadeArquivosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AtividadeArquivosService atividadeArquivosService;

    private AtividadeArquivosResponseDto responseDto;
    private AtividadeArquivosRequestDto requestDto;
    private LocalDate dataInicio;
    private LocalDate dataFechamento;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules(); 

        dataInicio = LocalDate.of(2025, 11, 1);
        dataFechamento = LocalDate.of(2025, 11, 30);

        responseDto = new AtividadeArquivosResponseDto();
        responseDto.setIdAtividade(1L);
        responseDto.setTituloAtividade("Trabalho de Java");
        responseDto.setDescricaoAtividade("Entregar um CRUD");
        responseDto.setDataInicioAtividade(dataInicio);
        responseDto.setDataFechamentoAtividade(dataFechamento);
        responseDto.setStatusAtividade(true);
        responseDto.setArquivosPermitidos(List.of(".pdf", ".zip"));

        requestDto = new AtividadeArquivosRequestDto();
        requestDto.setTituloAtividade("Trabalho de Java");
        requestDto.setDescricaoAtividade("Entregar um CRUD");
        requestDto.setDataInicioAtividade(dataInicio);
        requestDto.setDataFechamentoAtividade(dataFechamento);
        requestDto.setStatusAtividade(true);
        requestDto.setArquivosPermitidos(List.of(".pdf", ".zip"));
    }

    @Test
    void testCreate_Success() throws Exception {
        when(atividadeArquivosService.createAtividadeArquivos(any(AtividadeArquivosRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/atividades-arquivo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAtividade").value(1L))
                .andExpect(jsonPath("$.tituloAtividade").value("Trabalho de Java"))
                .andExpect(jsonPath("$.arquivosPermitidos[0]").value(".pdf"));

        verify(atividadeArquivosService, times(1)).createAtividadeArquivos(any(AtividadeArquivosRequestDto.class));
    }

    @Test
    void testCreate_InvalidInput() throws Exception {
        AtividadeArquivosRequestDto invalidDto = new AtividadeArquivosRequestDto();
        invalidDto.setTituloAtividade(null); 
        invalidDto.setDataInicioAtividade(dataInicio);
        invalidDto.setDataFechamentoAtividade(dataFechamento);
        invalidDto.setStatusAtividade(true);
        invalidDto.setArquivosPermitidos(List.of()); 

        mockMvc.perform(post("/atividades-arquivo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest()); 

        verify(atividadeArquivosService, never()).createAtividadeArquivos(any());
    }

    @Test
    void testGetAll_Success() throws Exception {
        List<AtividadeArquivosResponseDto> dtoList = List.of(responseDto);

        // --- CORREÇÃO AQUI ---
        // 1. Cria o mock 100% falso
        PagedResponse<AtividadeArquivosResponseDto> pagedResponse = mock(PagedResponse.class);

        // 2. Configura os getters do mock
        when(pagedResponse.getContent()).thenReturn(dtoList);
        when(pagedResponse.getPage()).thenReturn(0);
        when(pagedResponse.getSize()).thenReturn(10);
        when(pagedResponse.getTotalElements()).thenReturn(1L);
        when(pagedResponse.getTotalPages()).thenReturn(1);
        when(pagedResponse.isLast()).thenReturn(true);
        // --- FIM DA CORREÇÃO ---

        when(atividadeArquivosService.getAllAtividadesArquivos(any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/atividades-arquivo")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].idAtividade").value(1L))
                .andExpect(jsonPath("$.content[0].tituloAtividade").value("Trabalho de Java"));

        verify(atividadeArquivosService, times(1)).getAllAtividadesArquivos(any(Pageable.class));
    }

    @Test
    void testUpdate_Success() throws Exception {
        Long id = 1L;

        // --- CORREÇÃO AQUI ---
        // 1. Cria o DTO de Update real que o ObjectMapper vai criar a partir do JSON
        AtividadeArquivosUpdateDto updateDto = new AtividadeArquivosUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Trabalho de Java V2"));
        updateDto.setArquivosPermitidos(Optional.of(List.of(".pdf", ".docx", ".zip")));
        // --- FIM DA CORREÇÃO ---
        
        AtividadeArquivosResponseDto updatedResponse = new AtividadeArquivosResponseDto();
        updatedResponse.setIdAtividade(id);
        updatedResponse.setTituloAtividade("Trabalho de Java V2"); 
        updatedResponse.setDescricaoAtividade("Entregar um CRUD"); 
        updatedResponse.setDataInicioAtividade(dataInicio);
        updatedResponse.setDataFechamentoAtividade(dataFechamento);
        updatedResponse.setStatusAtividade(true);
        updatedResponse.setArquivosPermitidos(List.of(".pdf", ".docx", ".zip")); 

        when(atividadeArquivosService.updateAtividadeArquivos(eq(id), any(AtividadeArquivosUpdateDto.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/atividades-arquivo/{id}", id) 
                .contentType(MediaType.APPLICATION_JSON)
                 // 2. Serializa o DTO de Update real, que contém Optionals
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(id))
                .andExpect(jsonPath("$.tituloAtividade").value("Trabalho de Java V2"))
                .andExpect(jsonPath("$.arquivosPermitidos[1]").value(".docx"));

        verify(atividadeArquivosService, times(1)).updateAtividadeArquivos(eq(id), any(AtividadeArquivosUpdateDto.class));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        Long id = 1L;

        // --- CORREÇÃO AQUI ---
        // 1. Cria o DTO de Update real
        AtividadeArquivosUpdateDto updateDto = new AtividadeArquivosUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Trabalho de Java V2"));
        // --- FIM DA CORREÇÃO ---

        when(atividadeArquivosService.updateAtividadeArquivos(eq(id), any(AtividadeArquivosUpdateDto.class)))
                .thenThrow(new ResourceNotFoundException("Atividade não encontrada com id: " + id));

        mockMvc.perform(patch("/atividades-arquivo/{id}", id) 
                .contentType(MediaType.APPLICATION_JSON)
                // 2. Serializa o DTO de Update real
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(atividadeArquivosService, times(1)).updateAtividadeArquivos(eq(id), any(AtividadeArquivosUpdateDto.class));
    }

    @Test
    void testDelete_Success() throws Exception {
        Long id = 1L;
        doNothing().when(atividadeArquivosService).deleteAtividadeArquivos(id);

        mockMvc.perform(delete("/atividades-arquivo/{id}", id))
                .andExpect(status().isNoContent()); 

        verify(atividadeArquivosService, times(1)).deleteAtividadeArquivos(id);
    }

    @Test
    void testDelete_NotFound() throws Exception {
        Long id = 1L;

        doThrow(new ResourceNotFoundException("Atividade não encontrada com id: " + id))
            .when(atividadeArquivosService).deleteAtividadeArquivos(id);

        mockMvc.perform(delete("/atividades-arquivo/{id}", id))
                .andExpect(status().isNotFound());

        verify(atividadeArquivosService, times(1)).deleteAtividadeArquivos(id);
    }
}
