package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.controller.AlternativasController;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasRequestDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasResponseDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasUpdateDto;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.AlternativasService;



@WebMvcTest(AlternativasController.class)
public class AlternativasControllerTest {

    @Autowired
    private MockMvc mockMvc; 

    @Autowired
    private ObjectMapper objectMapper; 

    @MockBean 
    private AlternativasService alternativasService;

    private AlternativasResponseDto responseDto;
    private AlternativasRequestDto requestDto;

    @BeforeEach
    void setUp() {
        responseDto = new AlternativasResponseDto();
        responseDto.setIdAlternativa(1L);
        responseDto.setAlternativa("Teste de Alternativa");
        responseDto.setAlternativaCorreta(true);

        requestDto = new AlternativasRequestDto();
        requestDto.setAlternativa("Teste de Alternativa");
        requestDto.setAlternativaCorreta(true);
        requestDto.setIdQuestao(1L); 

        objectMapper.findAndRegisterModules();
    }

    @Test
    void testCreate_Success() throws Exception {
        when(alternativasService.createAlternativa(any(AlternativasRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.requestDto)))
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.idAlternativa").value(1L));

        verify(alternativasService, times(1)).createAlternativa(any(AlternativasRequestDto.class));
    }

    @Test
    void testCreate_InvalidInput() throws Exception {
        AlternativasRequestDto invalidDto = new AlternativasRequestDto();
        invalidDto.setAlternativa(null); 
        invalidDto.setAlternativaCorreta(true);
        invalidDto.setIdQuestao(1L); 

        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest()); 

        verify(alternativasService, times(0)).createAlternativa(any(AlternativasRequestDto.class));
    }

    @Test
    void testGetAlternativasById_Success() throws Exception {
        Long id = 1L;
        when(alternativasService.getAlternativaById(id)).thenReturn(responseDto);

        mockMvc.perform(get("/alternativas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlternativa").value(1L))
                .andExpect(jsonPath("$.alternativa").value("Teste de Alternativa"))
                .andExpect(jsonPath("$.alternativaCorreta").value(true));

        verify(alternativasService, times(1)).getAlternativaById(id);
    }

    @Test
    void testGetAlternativasById_NotFound() throws Exception {
        Long id = 1L;
        when(alternativasService.getAlternativaById(id)).thenThrow(new ResourceNotFoundException("Alternativa not found with id: " + id));

        mockMvc.perform(get("/alternativas/{id}", id))
                .andExpect(status().isNotFound());

        verify(alternativasService, times(1)).getAlternativaById(id);
    }

    @Test
    void testGetlAllAlternativas_Success() throws Exception {
        mockMvc.perform(get("/alternativas"))
                .andExpect(status().isOk());

        verify(alternativasService, times(1)).getAllAlternativas(any());
    }

    @Test
    void testGetAllAlternativas_Empty() throws Exception {
        mockMvc.perform(get("/alternativas"))
                .andExpect(status().isOk());

        verify(alternativasService, times(1)).getAllAlternativas(any());
    }

    @Test
    void testDeleteAlternativa_Success() throws Exception {
        Long id = 1L;
        doNothing().when(alternativasService).deleteAlternativa(id);

        mockMvc.perform(delete("/alternativas/{id}", id))
                .andExpect(status().isNoContent());

        verify(alternativasService, times(1)).deleteAlternativa(id);
    }

    @Test
    void testDeleteAlternativa_NotFound() throws Exception {
        Long id = 1L;
        doThrow(new ResourceNotFoundException("Alternativa not found with id: " + id))
                .when(alternativasService).deleteAlternativa(id);

        mockMvc.perform(delete("/alternativas/{id}", id))
                .andExpect(status().isNotFound());

        verify(alternativasService, times(1)).deleteAlternativa(id);
    }

    @Test
    void testUpdateAlternativa_Success() throws Exception {
        Long id = 1L;

        AlternativasUpdateDto updateDto = new AlternativasUpdateDto(
            Optional.of("Nova Alternativa"),
            Optional.of(true) 
        );

        String updateJson = """
        {
            "alternativa": "Nova Alternativa",
            "alternativaCorreta": true
        }
        """;

        AlternativasResponseDto updatedResponseDto = new AlternativasResponseDto();
        updatedResponseDto.setIdAlternativa(1L);
        updatedResponseDto.setAlternativa("Nova Alternativa");
        updatedResponseDto.setAlternativaCorreta(true);

        when(alternativasService.updateAlternativa(eq(id), any(AlternativasUpdateDto.class)))
                .thenReturn(updatedResponseDto);

        mockMvc.perform(patch("/alternativas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)) 
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.idAlternativa").value(1L))
                .andExpect(jsonPath("$.alternativa").value("Nova Alternativa"))
                .andExpect(jsonPath("$.alternativaCorreta").value(true));

        verify(alternativasService, times(1)).updateAlternativa(eq(id), any(AlternativasUpdateDto.class));
    }

    @Test
    void testUpdateAlternativa_NotFound() throws Exception {
        Long id = 1L;

        AlternativasUpdateDto updateDto = new AlternativasUpdateDto(
            Optional.of("Nova Alternativa"),
            Optional.of(true)
        );

        String updateJson = """
        {
            "alternativa": "Nova Alternativa",
            "alternativaCorreta": true
        }
        """;

        when(alternativasService.updateAlternativa(eq(id), any(AlternativasUpdateDto.class)))
                .thenThrow(new ResourceNotFoundException("Alternativa not found with id: " + id));

        mockMvc.perform(patch("/alternativas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)) 
                .andExpect(status().isNotFound()); 

        verify(alternativasService, times(1)).updateAlternativa(eq(id), any(AlternativasUpdateDto.class));
    }
}