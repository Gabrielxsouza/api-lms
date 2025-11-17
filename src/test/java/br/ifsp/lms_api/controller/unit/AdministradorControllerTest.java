package br.ifsp.lms_api.controller.unit;

import br.ifsp.lms_api.controller.AdministradorController;

import br.ifsp.lms_api.dto.adminDto.AdminRequestDto;
import br.ifsp.lms_api.dto.adminDto.AdminResponseDto;
import br.ifsp.lms_api.dto.adminDto.AdminUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.AdministradorService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Optional;

@WebMvcTest(AdministradorController.class)
class AdministradorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdministradorService administradorService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestConfig {

    }

    @Test
    @DisplayName("Deve criar administrador com sucesso (201 Created)")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAdminSuccessfully() throws Exception {

        AdminRequestDto requestDto = new AdminRequestDto("Admin Teste", "admin@teste.com", "123456", "12345678900");

        AdminResponseDto responseDto = new AdminResponseDto();
        responseDto.setIdUsuario(1L);
        responseDto.setNome("Admin Teste");

        when(administradorService.createAdmin(any(AdminRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(1L))
                .andExpect(jsonPath("$.nome").value("Admin Teste"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar criar admin inválido")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenCreatingInvalidAdmin() throws Exception {
        AdminRequestDto invalidDto = new AdminRequestDto();

        mockMvc.perform(post("/admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar lista paginada de administradores (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllAdmins() throws Exception {
        AdminResponseDto responseDto = new AdminResponseDto();
        responseDto.setNome("Admin 1");

        PagedResponse<AdminResponseDto> pagedResponse = new PagedResponse<>(
                Collections.singletonList(responseDto), 0, 1, 1, 10, true
        );

        when(administradorService.getAllAdmin(any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Admin 1"));
    }

    @Test
    @DisplayName("Deve negar acesso a usuário sem permissão (403 Forbidden)")
    @WithMockUser(roles = "ALUNO")
    void shouldDenyAccessToNonAdminUser() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve atualizar administrador com sucesso (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAdminSuccessfully() throws Exception {
        Long id = 1L;

        AdminUpdateDto updateDto = new AdminUpdateDto();
        updateDto.setNome(Optional.of("Novo Nome"));

        AdminResponseDto responseDto = new AdminResponseDto();
        responseDto.setIdUsuario(id);
        responseDto.setNome("Novo Nome");

        when(administradorService.updateAdmin(eq(id), any(AdminUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/admin/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"));
    }

    @Test
    @DisplayName("Deve deletar administrador com sucesso (204 No Content)")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteAdminSuccessfully() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/admin/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }



    @Test
    @DisplayName("PATCH - Deve retornar 400 Bad Request ao tentar atualizar com dados inválidos")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenUpdatingInvalidAdmin() throws Exception {
        Long id = 1L;

        String jsonInvalido = "{ \"email\": \"nao-eh-um-email\" }";

        mockMvc.perform(patch("/admin/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH - Deve retornar Erro (500) ao tentar atualizar ID inexistente")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnErrorWhenUpdatingNonExistentAdmin() throws Exception {
        Long id = 99L;
        AdminUpdateDto updateDto = new AdminUpdateDto();
        updateDto.setNome(Optional.of("Novo Nome"));

        doThrow(new RuntimeException("Administrador com id " + id + " nao encontrado"))
                .when(administradorService).updateAdmin(eq(id), any(AdminUpdateDto.class));

        mockMvc.perform(patch("/admin/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("DELETE - Deve retornar Erro (500) ao tentar deletar ID inexistente")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnErrorWhenDeletingNonExistentAdmin() throws Exception {

        Long id = 99L;

        doThrow(new RuntimeException("Administrador com id " + id + " nao encontrado"))
                .when(administradorService).deleteAdmin(id);

        mockMvc.perform(delete("/admin/{id}", id)
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST - Deve retornar Erro (500) ao tentar criar admin com email duplicado")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnErrorWhenCreatingDuplicateAdmin() throws Exception {

        AdminRequestDto requestDto = new AdminRequestDto(
            "Admin Duplicado",
            "existente@teste.com",
            "senha123",
            "12345678900"
        );

        doThrow(new RuntimeException("Erro ao salvar: Email já cadastrado"))
                .when(administradorService).createAdmin(any(AdminRequestDto.class));

        mockMvc.perform(post("/admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError());
    }
}
