package br.ifsp.lms_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaRequestDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaResponseDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaUpdateDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaParaDisciplinaDTO;
import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.DisciplinaService;

@WebMvcTest(DisciplinaController.class)
public class DisciplinaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // Mocka o Service
    private DisciplinaService disciplinaService;

    private DisciplinaRequestDto requestDto;
    private DisciplinaResponseDto responseDto;
    private DisciplinaUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        // DTOs para a criação aninhada
        TurmaParaDisciplinaDTO turmaDto = new TurmaParaDisciplinaDTO("Turma A", "2025/2");
        TurmaResponseDto turmaResponseDto = new TurmaResponseDto(1L, "Turma A", "2025/2", null);

        requestDto = new DisciplinaRequestDto(
            "Engenharia de Software",
            "Testes",
            "ESL708",
            List.of(turmaDto)
        );

        responseDto = new DisciplinaResponseDto(
            1L,
            "Engenharia de Software",
            "Testes",
            "ESL708",
            List.of(turmaResponseDto)
        );

        updateDto = new DisciplinaUpdateDto(
            Optional.of("Novo Nome"),
            Optional.empty(),
            Optional.empty()
        );

        objectMapper.findAndRegisterModules();
    }

    @Test
    void testCreateDisciplina_Success() throws Exception {
        // Arrange
        when(disciplinaService.createDisciplina(any(DisciplinaRequestDto.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/disciplinas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDisciplina").value(1L))
                .andExpect(jsonPath("$.nomeDisciplina").value("Engenharia de Software"))
                .andExpect(jsonPath("$.turmas[0].nomeTurma").value("Turma A"));

        verify(disciplinaService, times(1)).createDisciplina(any(DisciplinaRequestDto.class));
    }

    @Test
    void testCreateDisciplina_InvalidInput() throws Exception {
        // Arrange
        DisciplinaRequestDto invalidDto = new DisciplinaRequestDto(
            null, // Nome @NotBlank faltando
            "Testes", "ESL708", List.of()
        );

        // Act & Assert
        mockMvc.perform(post("/disciplinas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(disciplinaService, times(0)).createDisciplina(any(DisciplinaRequestDto.class));
    }

    @Test
    void testGetAllDisciplinas_Success() throws Exception {
        // Arrange
        PagedResponse<DisciplinaResponseDto> pagedResponse = new PagedResponse<>(
            List.of(responseDto), 0, 10, 1L, 1, true
        );
        when(disciplinaService.getAllDisciplinas(any(Pageable.class)))
            .thenReturn(pagedResponse);

        // Act & Assert
        mockMvc.perform(get("/disciplinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].nomeDisciplina").value("Engenharia de Software"));

        verify(disciplinaService, times(1)).getAllDisciplinas(any(Pageable.class));
    }

    @Test
    void testUpdateDisciplina_Success() throws Exception {
        // Arrange
        DisciplinaResponseDto updatedResponse = new DisciplinaResponseDto(
            1L, "Novo Nome", "Testes", "ESL708", List.of()
        );
        
        when(disciplinaService.updateDisciplina(eq(1L), any(DisciplinaUpdateDto.class)))
            .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(patch("/disciplinas/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDisciplina").value(1L))
                .andExpect(jsonPath("$.nomeDisciplina").value("Novo Nome"));

        verify(disciplinaService, times(1)).updateDisciplina(eq(1L), any(DisciplinaUpdateDto.class));
    }

    @Test
    void testUpdateDisciplina_NotFound() throws Exception {
        // Arrange
        when(disciplinaService.updateDisciplina(eq(99L), any(DisciplinaUpdateDto.class)))
            .thenThrow(new ResourceNotFoundException("Disciplina não encontrada"));

        // Act & Assert
        mockMvc.perform(patch("/disciplinas/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteDisciplina_Success() throws Exception {
        // Arrange
        doNothing().when(disciplinaService).deleteDisciplina(1L);

        // Act & Assert
        mockMvc.perform(delete("/disciplinas/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(disciplinaService, times(1)).deleteDisciplina(1L);
    }
}