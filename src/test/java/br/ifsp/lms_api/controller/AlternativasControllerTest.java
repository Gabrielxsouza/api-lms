package br.ifsp.lms_api.controller;

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

import br.ifsp.lms_api.dto.alternativasDto.AlternativasRequestDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasResponseDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasUpdateDto;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.AlternativasService;



@WebMvcTest(AlternativasController.class)
public class AlternativasControllerTest {

    @Autowired
    private MockMvc mockMvc; // 2. Objeto para simular requisições HTTP

    @Autowired
    private ObjectMapper objectMapper; // 3. Para converter objetos Java -> JSON e vice-versa

    @MockBean // 4. Usa @MockBean (do Spring) para mockar o Service
    private AlternativasService alternativasService;

    private AlternativasResponseDto responseDto;
    private AlternativasRequestDto requestDto;

    @BeforeEach
    void setUp() {
        // Objeto de resposta padrão para os testes
        responseDto = new AlternativasResponseDto();
        responseDto.setIdAlternativa(1L);
        responseDto.setAlternativa("Teste de Alternativa");
        responseDto.setAlternativaCorreta(true);

        requestDto = new AlternativasRequestDto();
        requestDto.setAlternativa("Teste de Alternativa");
        requestDto.setAlternativaCorreta(true);
        requestDto.setIdQuestao(1L); // <-- ESTA É A CORREÇÃO

        // Configura o ObjectMapper para lidar com LocalDate
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testCreate_Success() throws Exception {
        // O requestDto agora é criado no setUp()

        when(alternativasService.createAlternativa(any(AlternativasRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                // Use o 'requestDto' da classe, que agora é válido
                .content(objectMapper.writeValueAsString(this.requestDto)))
                .andExpect(status().isCreated()) // Agora deve ser 201
                .andExpect(jsonPath("$.idAlternativa").value(1L));

        verify(alternativasService, times(1)).createAlternativa(any(AlternativasRequestDto.class));
    }

    @Test
    void testCreate_InvalidInput() throws Exception {
        // Agora vamos testar um DTO inválido DE VERDADE
        AlternativasRequestDto invalidDto = new AlternativasRequestDto();
        invalidDto.setAlternativa(null); // Campo @NotBlank faltando
        invalidDto.setAlternativaCorreta(true);
        invalidDto.setIdQuestao(1L); // O ID da questão está OK

        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest()); // Deve ser 400

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

        // Crie o DTO de update APENAS para o mock do service
        AlternativasUpdateDto updateDto = new AlternativasUpdateDto(
            Optional.of("Nova Alternativa"),
            Optional.of(true) // <-- Corrigi para true, baseado no seu teste original
        );

        // Crie a String JSON que o cliente enviaria
        String updateJson = """
        {
            "alternativa": "Nova Alternativa",
            "alternativaCorreta": true
        }
        """;

        // Crie o DTO de resposta esperado
        AlternativasResponseDto updatedResponseDto = new AlternativasResponseDto();
        updatedResponseDto.setIdAlternativa(1L);
        updatedResponseDto.setAlternativa("Nova Alternativa");
        updatedResponseDto.setAlternativaCorreta(true);

        // Configure o mock do service para esperar o DTO com Optional
        when(alternativasService.updateAlternativa(eq(id), any(AlternativasUpdateDto.class)))
                .thenReturn(updatedResponseDto);

        // Execute o perform usando a String JSON manual
        mockMvc.perform(patch("/alternativas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)) // <-- MUDANÇA PRINCIPAL AQUI
                .andExpect(status().isOk()) // Agora deve ser 200
                .andExpect(jsonPath("$.idAlternativa").value(1L))
                .andExpect(jsonPath("$.alternativa").value("Nova Alternativa"))
                .andExpect(jsonPath("$.alternativaCorreta").value(true));

        // Verifique se o service foi chamado
        verify(alternativasService, times(1)).updateAlternativa(eq(id), any(AlternativasUpdateDto.class));
    }

    @Test
    void testUpdateAlternativa_NotFound() throws Exception {
        Long id = 1L;

        // Crie o DTO de update APENAS para o mock do service
        AlternativasUpdateDto updateDto = new AlternativasUpdateDto(
            Optional.of("Nova Alternativa"),
            Optional.of(true)
        );

        // Crie a String JSON que o cliente enviaria
        String updateJson = """
        {
            "alternativa": "Nova Alternativa",
            "alternativaCorreta": true
        }
        """;

        // Configure o mock do service para lançar a exceção
        when(alternativasService.updateAlternativa(eq(id), any(AlternativasUpdateDto.class)))
                .thenThrow(new ResourceNotFoundException("Alternativa not found with id: " + id));

        // Execute o perform usando a String JSON manual
        mockMvc.perform(patch("/alternativas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)) // <-- MUDANÇA PRINCIPAL AQUI
                .andExpect(status().isNotFound()); // Agora deve ser 404

        // Verifique se o service foi chamado
        verify(alternativasService, times(1)).updateAlternativa(eq(id), any(AlternativasUpdateDto.class));
    }
}
