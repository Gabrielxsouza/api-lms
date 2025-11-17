package br.ifsp.lms_api.controller.integration;

import br.ifsp.lms_api.dto.adminDto.AdminRequestDto;
import br.ifsp.lms_api.dto.adminDto.AdminUpdateDto;
import br.ifsp.lms_api.model.Administrador;
import br.ifsp.lms_api.repository.AdministradorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdministradorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        administradorRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar um administrador e persistir no banco H2")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAdminAndPersistInDatabase() throws Exception {

        AdminRequestDto requestDto = new AdminRequestDto(
            "Carlos Integração",
            "carlos.int@teste.com",
            "senha123",
            "11122233344"
        );

        mockMvc.perform(post("/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Carlos Integração"))
                .andExpect(jsonPath("$.idUsuario").exists());

        assertEquals(1, administradorRepository.count());
        Administrador salvo = administradorRepository.findAll().get(0);
        assertEquals("carlos.int@teste.com", salvo.getEmail());
    }

    @Test
    @DisplayName("Deve impedir cadastro de e-mail duplicado (Restrição do Banco)")
    @WithMockUser(roles = "ADMIN")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldFailWhenCreatingDuplicateEmail() throws Exception {

        administradorRepository.deleteAll();

        Administrador admin1 = new Administrador();
        admin1.setNome("Admin Original");
        admin1.setEmail("duplicado@teste.com");
        admin1.setSenha("123456");
        admin1.setCpf("11111111111");

        administradorRepository.save(admin1);

        AdminRequestDto requestDto = new AdminRequestDto(
            "Admin Intruso",
            "duplicado@teste.com",
            "senha123",
            "22222222222"
        );

        mockMvc.perform(post("/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError());

        assertEquals(1, administradorRepository.count());

        administradorRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve atualizar o nome no banco de dados (PATCH)")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAdminNameInDatabase() throws Exception {

        Administrador admin = new Administrador();
        admin.setNome("Nome Antigo");
        admin.setEmail("update@teste.com");
        admin.setSenha("123456");
        admin.setCpf("33333333333");
        Administrador salvo = administradorRepository.save(admin);

        AdminUpdateDto updateDto = new AdminUpdateDto();
        updateDto.setNome(Optional.of("Nome Novo Integração"));

        mockMvc.perform(patch("/admin/{id}", salvo.getIdUsuario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo Integração"));

        Administrador atualizado = administradorRepository.findById(salvo.getIdUsuario()).orElseThrow();
        assertEquals("Nome Novo Integração", atualizado.getNome());
        assertEquals("update@teste.com", atualizado.getEmail());
    }

    @Test
    @DisplayName("Deve deletar do banco de dados")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteFromDatabase() throws Exception {

        Administrador admin = new Administrador();
        admin.setNome("Para Deletar");
        admin.setEmail("delete@teste.com");
        admin.setSenha("123456");
        admin.setCpf("44444444444");
        Administrador salvo = administradorRepository.save(admin);

        mockMvc.perform(delete("/admin/{id}", salvo.getIdUsuario()))
                .andExpect(status().isNoContent());

        assertTrue(administradorRepository.findById(salvo.getIdUsuario()).isEmpty());
        assertEquals(0, administradorRepository.count());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar criar com dados inválidos (Validação real)")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenCreatingInvalidAdmin() throws Exception {

        AdminRequestDto invalidDto = new AdminRequestDto();

        mockMvc.perform(post("/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
        assertEquals(0, administradorRepository.count());
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar atualizar ID inexistente (Banco real vazio)")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnErrorWhenUpdatingNonExistentAdmin() throws Exception {

        Long nonExistentId = 999L;
        AdminUpdateDto updateDto = new AdminUpdateDto();
        updateDto.setNome(Optional.of("Novo Nome"));

        mockMvc.perform(patch("/admin/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isInternalServerError());

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar deletar ID inexistente")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnErrorWhenDeletingNonExistentAdmin() throws Exception {

        Long nonExistentId = 999L;

        mockMvc.perform(delete("/admin/{id}", nonExistentId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Deve negar acesso (403) se usuário tiver role errada")
    @WithMockUser(roles = "ALUNO")
    void shouldDenyAccessWhenUserHasWrongRole() throws Exception {

        AdminRequestDto requestDto = new AdminRequestDto(
            "Hacker Aluno", "hacker@teste.com", "123456", "11122233344"
        );

        mockMvc.perform(post("/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        assertEquals(0, administradorRepository.count());
    }

    @Test
    @DisplayName("Deve negar acesso (401) se usuário não estiver logado")

    void shouldDenyAccessWhenUserIsNotAuthenticated() throws Exception {

        mockMvc.perform(get("/admin"))
                .andExpect(status().isUnauthorized());
    }
}
