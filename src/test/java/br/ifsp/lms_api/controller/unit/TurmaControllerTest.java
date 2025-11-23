package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.controller.TurmaController;
import br.ifsp.lms_api.dto.CursoDto.CursoParaTurmaResponseDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaParaTurmaResponseDto;
import br.ifsp.lms_api.dto.professorDto.ProfessorParaTurmaResponseDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaRequestDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.TurmaService;

@WebMvcTest(TurmaController.class)
public class TurmaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TurmaService turmaService;

    private TurmaRequestDto requestDto;
    private TurmaResponseDto responseDto;
    private TurmaUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        requestDto = new TurmaRequestDto("Turma A", "2025/2", 1L, 1L, 1L); // Exemplo ajustado para TurmaRequestDto

        responseDto = new TurmaResponseDto(
            1L,
            "Turma A",
            "2025/2",
            (CursoParaTurmaResponseDto) null,
            (ProfessorParaTurmaResponseDto) null,
            (DisciplinaParaTurmaResponseDto) null
        );

        updateDto = new TurmaUpdateDto(
            Optional.of("Novo Semestre"),
            Optional.empty()
        );

        objectMapper.findAndRegisterModules();
    }

    @Test
    @WithMockUser(roles = "ADMIN") 
    void testCreateTurma_Success() throws Exception {
        when(turmaService.createTurma(any(TurmaRequestDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(post("/turmas")
                .with(csrf()) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTurma").value(1L))
                .andExpect(jsonPath("$.nomeTurma").value("Turma A"));

        verify(turmaService, times(1)).createTurma(any(TurmaRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateTurma_DisciplinaNotFound() throws Exception {
        when(turmaService.createTurma(any(TurmaRequestDto.class)))
            .thenThrow(new ResourceNotFoundException("Disciplina não encontrada"));

        mockMvc.perform(post("/turmas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ALUNO", "PROFESSOR"})
    void testGetAllTurmas_Success() throws Exception {
        PagedResponse<TurmaResponseDto> pagedResponse = new PagedResponse<>(
            List.of(responseDto), 0, 10, 1L, 1, true
        );
        when(turmaService.getAllTurmas(any(Pageable.class)))
            .thenReturn(pagedResponse);

        mockMvc.perform(get("/turmas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].nomeTurma").value("Turma A"));

        verify(turmaService, times(1)).getAllTurmas(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR") 
    void testGetMinhasTurmas_Success() throws Exception {
        PagedResponse<TurmaResponseDto> pagedResponse = new PagedResponse<>(
            List.of(responseDto), 0, 10, 1L, 1, true
        );
        when(turmaService.getMinhasTurmas(any(Pageable.class)))
            .thenReturn(pagedResponse);

        mockMvc.perform(get("/turmas/minhas-turmas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nomeTurma").value("Turma A"));

        verify(turmaService, times(1)).getMinhasTurmas(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateTurma_Success() throws Exception {
        TurmaResponseDto updatedResponse = new TurmaResponseDto(
            1L,
            "Turma A",
            "Novo Semestre",
            (CursoParaTurmaResponseDto) null,
            (ProfessorParaTurmaResponseDto) null,
            (DisciplinaParaTurmaResponseDto) null
        );

        when(turmaService.updateTurma(eq(1L), any(TurmaUpdateDto.class)))
            .thenReturn(updatedResponse);

        mockMvc.perform(patch("/turmas/{id}", 1L)
                .with(csrf()) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTurma").value(1L))
                .andExpect(jsonPath("$.semestre").value("Novo Semestre"));

        verify(turmaService, times(1)).updateTurma(eq(1L), any(TurmaUpdateDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteTurma_Success() throws Exception {
        doNothing().when(turmaService).deleteTurma(1L);

        mockMvc.perform(delete("/turmas/{id}", 1L)
                .with(csrf())) 
                .andExpect(status().isNoContent());

        verify(turmaService, times(1)).deleteTurma(1L);
    }
}
