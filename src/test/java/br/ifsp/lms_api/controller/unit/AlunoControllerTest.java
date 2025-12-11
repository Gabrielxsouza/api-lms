package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.config.CustomAuthenticationEntryPoint;
import br.ifsp.lms_api.config.CustomLoginSuccessHandler;
import br.ifsp.lms_api.config.SecurityConfig;
import br.ifsp.lms_api.controller.AlunoController;
import br.ifsp.lms_api.dto.alunoDto.AlunoRequestDto;
import br.ifsp.lms_api.dto.alunoDto.AlunoResponseDto;
import br.ifsp.lms_api.dto.alunoDto.AlunoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.AlunoService;

@WebMvcTest(AlunoController.class)
@Import(SecurityConfig.class)
class AlunoControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AlunoService alunoService;

        @MockBean
        private CustomLoginSuccessHandler customLoginSuccessHandler;

        @MockBean
        private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

        private AlunoResponseDto responseDto;
        private AlunoRequestDto requestDto;
        private AlunoUpdateDto updateDto;

        @BeforeEach
        void setUp() {
                objectMapper.findAndRegisterModules();

                responseDto = new AlunoResponseDto(
                                1L, "Aluno Teste", "aluno@test.com", "123.456.789-00", "RA123456");

                requestDto = new AlunoRequestDto(
                                "Aluno Teste", "aluno@test.com", "senha123", "123.456.789-00", "RA123456");

                updateDto = new AlunoUpdateDto(
                                Optional.of("Aluno Atualizado"), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testCreate_Success() throws Exception {
                when(alunoService.createAluno(any(AlunoRequestDto.class))).thenReturn(responseDto);

                mockMvc.perform(post("/alunos")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.idUsuario").value(1L))
                                .andExpect(jsonPath("$.nome").value("Aluno Teste"));
        }

        @Test
        @WithMockUser(roles = "ALUNO")
        void testCreate_Forbidden() throws Exception {
                mockMvc.perform(post("/alunos")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testGetAll_Success() throws Exception {
                PagedResponse<AlunoResponseDto> pagedResponse = new PagedResponse<>(
                                List.of(responseDto), 0, 10, 1L, 1, true);

                when(alunoService.getAllAlunos(any(Pageable.class))).thenReturn(pagedResponse);

                mockMvc.perform(get("/alunos")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].nome").value("Aluno Teste"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testUpdate_Success() throws Exception {
                when(alunoService.updateAluno(eq(1L), any(AlunoUpdateDto.class))).thenReturn(responseDto);

                mockMvc.perform(patch("/alunos/{id}", 1L)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testDelete_Success() throws Exception {
                doNothing().when(alunoService).deleteAluno(1L);

                mockMvc.perform(delete("/alunos/{id}", 1L)
                                .with(csrf()))
                                .andExpect(status().isNoContent());
        }
}