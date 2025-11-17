package br.ifsp.lms_api.controller.unit;

import br.ifsp.lms_api.controller.AlternativasController;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasRequestDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasResponseDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.AlternativasService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlternativasController.class)
@Import(AlternativasControllerTest.TestConfig.class)
class AlternativasControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlternativasService alternativasService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("POST - Deve criar alternativa com sucesso (201 Created)")
    @WithMockUser(roles = "PROFESSOR")
    void shouldCreateAlternativaSuccessfully() throws Exception {

        AlternativasRequestDto requestDto = new AlternativasRequestDto();
        requestDto.setIdQuestao(1L);
        requestDto.setAlternativa("Alternativa Correta");

        requestDto.setAlternativaCorreta(true);

        AlternativasResponseDto responseDto = new AlternativasResponseDto();
        responseDto.setIdAlternativa(10L);
        responseDto.setAlternativa("Alternativa Correta");

        when(alternativasService.createAlternativa(any(AlternativasRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/alternativas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAlternativa").value(10L));
    }

    @Test
    @DisplayName("GET - Deve retornar lista paginada de alternativas (200 OK)")
    @WithMockUser(roles = "PROFESSOR")
    void shouldGetAllAlternativas() throws Exception {

        PagedResponse<AlternativasResponseDto> pagedResponse = new PagedResponse<AlternativasResponseDto>(null, 0, 0, 0, 0, true);
        pagedResponse.setContent(Collections.emptyList());
        pagedResponse.setPage(0);
        pagedResponse.setSize(10);

        when(alternativasService.getAllAlternativas(any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/alternativas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    @DisplayName("GET - Deve retornar alternativa por ID (200 OK)")
    @WithMockUser(roles = "PROFESSOR")
    void shouldGetAlternativaById() throws Exception {

        Long id = 1L;
        AlternativasResponseDto responseDto = new AlternativasResponseDto();
        responseDto.setIdAlternativa(id);
        responseDto.setAlternativa("Texto");

        when(alternativasService.getAlternativaById(id)).thenReturn(responseDto);

        mockMvc.perform(get("/alternativas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlternativa").value(id));
    }

    @Test
    @DisplayName("PATCH - Deve atualizar alternativa com sucesso (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAlternativaSuccessfully() throws Exception {

        Long id = 1L;
        AlternativasUpdateDto updateDto = new AlternativasUpdateDto();
        updateDto.setAlternativa(Optional.of("Novo Texto"));

        AlternativasResponseDto responseDto = new AlternativasResponseDto();
        responseDto.setIdAlternativa(id);
        responseDto.setAlternativa("Novo Texto");

        when(alternativasService.updateAlternativa(eq(id), any(AlternativasUpdateDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/alternativas/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alternativa").value("Novo Texto"));
    }

    @Test
    @DisplayName("DELETE - Deve deletar alternativa com sucesso (204 No Content)")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteAlternativaSuccessfully() throws Exception {

        Long id = 1L;

        mockMvc.perform(delete("/alternativas/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }


    @Test
    @DisplayName("POST - Deve retornar 403 Forbidden se usuário não tiver role PROFESSOR")
    @WithMockUser(roles = "ALUNO")
    void shouldReturnForbiddenWhenUserHasWrongRoleForCreate() throws Exception {

        AlternativasRequestDto requestDto = new AlternativasRequestDto();
        requestDto.setIdQuestao(1L);
        requestDto.setAlternativa("Texto Qualquer");
        requestDto.setAlternativaCorreta(false);

        mockMvc.perform(post("/alternativas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST - Deve retornar 400 Bad Request se input for inválido")
    @WithMockUser(roles = "PROFESSOR")
    void shouldReturnBadRequestWhenInputIsInvalid() throws Exception {

        AlternativasRequestDto invalidDto = new AlternativasRequestDto();

        mockMvc.perform(post("/alternativas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET - Deve retornar 404 quando alternativa não encontrada")
    @WithMockUser(roles = "PROFESSOR")
    void shouldReturnNotFoundWhenAlternativaDoesNotExist() throws Exception {
        Long id = 99L;

        when(alternativasService.getAlternativaById(id))
            .thenThrow(new ResourceNotFoundException("Não encontrado"));

        mockMvc.perform(get("/alternativas/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH - Deve retornar 403 Forbidden se PROFESSOR tentar atualizar (Rota de ADMIN)")
    @WithMockUser(roles = "PROFESSOR")
    void shouldReturnForbiddenWhenProfessorTriesToUpdate() throws Exception {
        Long id = 1L;
        AlternativasUpdateDto updateDto = new AlternativasUpdateDto();

        mockMvc.perform(patch("/alternativas/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE - Deve retornar 404 ao tentar deletar ID inexistente")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnErrorWhenDeletingNonExistentAlternativa() throws Exception {
        Long id = 99L;

        doThrow(new ResourceNotFoundException("Não encontrado"))
                .when(alternativasService).deleteAlternativa(id);

        mockMvc.perform(delete("/alternativas/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNotFound()); 
    }
}
