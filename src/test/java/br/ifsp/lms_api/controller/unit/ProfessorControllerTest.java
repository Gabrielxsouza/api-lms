package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.controller.ProfessorController;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.professorDto.ProfessorRequestDto;
import br.ifsp.lms_api.dto.professorDto.ProfessorResponseDto;
import br.ifsp.lms_api.dto.professorDto.ProfessorUpdateDto;
import br.ifsp.lms_api.service.ProfessorService;

@WebMvcTest(ProfessorController.class)
@EnableMethodSecurity(prePostEnabled = true)
public class ProfessorControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ProfessorService professorService;

    private ProfessorRequestDto requestDto;
    private ProfessorResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new ProfessorRequestDto("Prof. Teste", "prof@test.com", "123456", "12345678900", "Dep 1");
        responseDto = new ProfessorResponseDto(1L, "Prof. Teste", "prof@test.com", "12345678900", "Dep 1");
        objectMapper.findAndRegisterModules();
    }

    @Test
    void createProfessor_Admin_Success() throws Exception {
        when(professorService.createProfessor(any(ProfessorRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/professores")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(1L));
    }

    @Test
    void createProfessor_User_Forbidden() throws Exception {
        mockMvc.perform(post("/professores")
                .with(user("user").roles("ALUNO"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_Admin_Success() throws Exception {
        PagedResponse<ProfessorResponseDto> pagedResponse = new PagedResponse<>(List.of(responseDto), 0, 10, 1L, 1, true);
        when(professorService.getAllProfessores(any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/professores")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void updateProfessor_Professor_Success() throws Exception {
        ProfessorUpdateDto updateDto = new ProfessorUpdateDto(Optional.of("Novo Nome"), Optional.empty(), Optional.empty(), Optional.empty());
        
        when(professorService.updateProfessor(eq(1L), any(ProfessorUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/professores/{id}", 1L)
                .with(user("prof").roles("PROFESSOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProfessor_Admin_Success() throws Exception {
        doNothing().when(professorService).deleteProfessor(1L);

        mockMvc.perform(delete("/professores/{id}", 1L)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
}