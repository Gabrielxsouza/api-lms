package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

import br.ifsp.lms_api.controller.CursoController;
import br.ifsp.lms_api.dto.CursoDto.CursoRequestDto;
import br.ifsp.lms_api.dto.CursoDto.CursoResponseDto;
import br.ifsp.lms_api.dto.CursoDto.CursoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.CursoService;

@WebMvcTest(CursoController.class)
@EnableMethodSecurity(prePostEnabled = true) 
public class CursoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CursoService cursoService;

    private CursoRequestDto requestDto;
    private CursoResponseDto responseDto;
    private CursoUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        requestDto = new CursoRequestDto("Engenharia", "Bacharelado", "ENG-01");
        
        responseDto = new CursoResponseDto(1L, "Engenharia", "Bacharelado", "ENG-01", Collections.emptyList());

        updateDto = new CursoUpdateDto(Optional.of("Novo Nome"), Optional.empty(), Optional.empty());
        
        objectMapper.findAndRegisterModules();
    }

    @Test
    void createCurso_Success() throws Exception {
        when(cursoService.createCurso(any(CursoRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/cursos")
                .with(user("admin").roles("ADMIN")) 
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCurso").value(1L))
                .andExpect(jsonPath("$.nomeCurso").value("Engenharia"));
    }

    @Test
    void createCurso_Forbidden() throws Exception {

        mockMvc.perform(post("/cursos")
                .with(user("user").roles("USER")) 
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllCursos_Success() throws Exception {
        PagedResponse<CursoResponseDto> pagedResponse = new PagedResponse<>(
            List.of(responseDto), 0, 10, 1L, 1, true
        );
        when(cursoService.getAllCursos(any(Pageable.class))).thenReturn(pagedResponse);

  
        mockMvc.perform(get("/cursos")
                .with(user("user"))) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nomeCurso").value("Engenharia"));
    }

    @Test
    void updateCurso_Success() throws Exception {
        when(cursoService.updateCurso(eq(1L), any(CursoUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/cursos/{id}", 1L)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCurso_Success() throws Exception {
        doNothing().when(cursoService).deleteCurso(1L);

        mockMvc.perform(delete("/cursos/{id}", 1L)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCurso_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Curso não encontrado")).when(cursoService).deleteCurso(99L);

        mockMvc.perform(delete("/cursos/{id}", 99L)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}