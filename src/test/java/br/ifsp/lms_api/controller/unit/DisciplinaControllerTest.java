package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
// --- IMPORTS DE SEGURANÇA ADICIONADOS ---
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
// ---------------------------------------
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

import br.ifsp.lms_api.controller.DisciplinaController;
import br.ifsp.lms_api.dto.CursoDto.CursoParaTurmaResponseDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaParaTurmaResponseDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaRequestDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaResponseDto;
import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaUpdateDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaParaDisciplinaDTO;
import br.ifsp.lms_api.dto.TurmaDto.TurmaResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.professorDto.ProfessorParaTurmaResponseDto;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.DisciplinaService;

@WebMvcTest(DisciplinaController.class)
public class DisciplinaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean 
    private DisciplinaService disciplinaService;

    private DisciplinaRequestDto requestDto;
    private DisciplinaResponseDto responseDto;
    private DisciplinaUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        TurmaParaDisciplinaDTO turmaDto = new TurmaParaDisciplinaDTO("Turma A", "2025/2");

        TurmaResponseDto turmaResponseDto = new TurmaResponseDto(
            1L, 
            "Turma A", 
            "2025/2", 
            (CursoParaTurmaResponseDto) null,
            (ProfessorParaTurmaResponseDto) null,
            (DisciplinaParaTurmaResponseDto) null
        );

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
        when(disciplinaService.createDisciplina(any(DisciplinaRequestDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(post("/disciplinas")
                // Simula usuário ADMIN (necessário para criar disciplina)
                .with(user("admin").roles("ADMIN")) 
                // Adiciona Token CSRF (necessário para POST/PUT/DELETE em testes)
                .with(csrf()) 
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
        DisciplinaRequestDto invalidDto = new DisciplinaRequestDto(
            null, 
            "Testes", "ESL708", List.of()
        );

        mockMvc.perform(post("/disciplinas")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(disciplinaService, times(0)).createDisciplina(any(DisciplinaRequestDto.class));
    }

    @Test
    void testGetAllDisciplinas_Success() throws Exception {
        PagedResponse<DisciplinaResponseDto> pagedResponse = new PagedResponse<>(
            List.of(responseDto), 0, 10, 1L, 1, true
        );
        when(disciplinaService.getAllDisciplinas(any(Pageable.class)))
            .thenReturn(pagedResponse);

        mockMvc.perform(get("/disciplinas")
                // Assume-se que ADMIN pode listar. Se for público, remova o .with(user)
                .with(user("admin").roles("ADMIN"))) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].nomeDisciplina").value("Engenharia de Software"));

        verify(disciplinaService, times(1)).getAllDisciplinas(any(Pageable.class));
    }

    @Test
    void testUpdateDisciplina_Success() throws Exception {
        DisciplinaResponseDto updatedResponse = new DisciplinaResponseDto(
            1L, "Novo Nome", "Testes", "ESL708", List.of()
        );
        
        when(disciplinaService.updateDisciplina(eq(1L), any(DisciplinaUpdateDto.class)))
            .thenReturn(updatedResponse);

        mockMvc.perform(patch("/disciplinas/{id}", 1L)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDisciplina").value(1L))
                .andExpect(jsonPath("$.nomeDisciplina").value("Novo Nome"));

        verify(disciplinaService, times(1)).updateDisciplina(eq(1L), any(DisciplinaUpdateDto.class));
    }

    @Test
    void testUpdateDisciplina_NotFound() throws Exception {
        when(disciplinaService.updateDisciplina(eq(99L), any(DisciplinaUpdateDto.class)))
            .thenThrow(new ResourceNotFoundException("Disciplina não encontrada"));

        mockMvc.perform(patch("/disciplinas/{id}", 99L)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteDisciplina_Success() throws Exception {
        doNothing().when(disciplinaService).deleteDisciplina(1L);

        // ATENÇÃO: Se o Controller retorna o objeto deletado (status 200), mude para isOk()
        // Se retorna void/noContent (status 204), mantenha isNoContent()
        mockMvc.perform(delete("/disciplinas/{id}", 1L)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(disciplinaService, times(1)).deleteDisciplina(1L);
    }
}