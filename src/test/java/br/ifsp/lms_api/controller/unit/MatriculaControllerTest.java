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

import java.util.Collections;
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

import br.ifsp.lms_api.controller.MatriculaController;
import br.ifsp.lms_api.dto.matriculaDto.MatriculaRequestDto;
import br.ifsp.lms_api.dto.matriculaDto.MatriculaResponseDto;
import br.ifsp.lms_api.dto.matriculaDto.MatriculaUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.model.Status;
import br.ifsp.lms_api.service.MatriculaService;

@WebMvcTest(MatriculaController.class)
@EnableMethodSecurity(prePostEnabled = true)
public class MatriculaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private MatriculaService matriculaService;

    private MatriculaRequestDto requestDto;
    private MatriculaResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new MatriculaRequestDto(1L, 2L, Status.ATIVA);
        
        responseDto = new MatriculaResponseDto();
        responseDto.setIdMatricula(10L);
        responseDto.setStatusMatricula(Status.ATIVA);
        responseDto.setNomeAluno("João");
        responseDto.setNomeTurma("Turma A");

        objectMapper.findAndRegisterModules();
    }

    @Test
    void createMatricula_Admin_Success() throws Exception {
        when(matriculaService.createMatricula(any(MatriculaRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/matriculas")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMatricula").value(10L));
    }

    @Test
    void createMatricula_Professor_Forbidden() throws Exception {
        mockMvc.perform(post("/matriculas")
                .with(user("prof").roles("PROFESSOR")) 
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_Professor_Success() throws Exception {
        PagedResponse<MatriculaResponseDto> pagedResponse = new PagedResponse<>(
                List.of(responseDto), 0, 10, 1L, 1, true);
        
        when(matriculaService.getAllMatriculas(any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/matriculas")
                .with(user("prof").roles("PROFESSOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nomeAluno").value("João"));
    }

    @Test
    void deleteMatricula_Admin_Success() throws Exception {
        doNothing().when(matriculaService).deleteMatricula(10L);

        mockMvc.perform(delete("/matriculas/{id}", 10L)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateMatricula_Admin_Success() throws Exception {
        MatriculaUpdateDto updateDto = new MatriculaUpdateDto(Optional.of("PENDENTE"));
        responseDto.setStatusMatricula(Status.PENDENTE);

        when(matriculaService.updateMatricula(eq(10L), any(MatriculaUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/matriculas/{id}", 10L)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMatricula").value("PENDENTE"));
    }
}