package br.ifsp.lms_api.controller.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioRequestDto;
import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioResponseDto;
import br.ifsp.lms_api.exception.LimiteTentativasException;
import br.ifsp.lms_api.model.Usuario;
import br.ifsp.lms_api.service.TentativaQuestionarioService;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TentativaQuestionarioControllerIntegrationTest.TestControllerAdvice.class)
class TentativaQuestionarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TentativaQuestionarioService tentativaQuestionarioService;

    private CustomUserDetails alunoUserDetails;
    private CustomUserDetails professorUserDetails;

    @TestConfiguration
    @RestControllerAdvice
    public static class TestControllerAdvice {

        @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public void handleSpringSecurityAccessDenied() {}

        @ExceptionHandler(RuntimeException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public void handleRuntimeException() {}

        @ExceptionHandler(LimiteTentativasException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public void handleLimiteTentativas() {}
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @BeforeEach
    void setUp() {
        Usuario alunoMock = mock(Usuario.class);
        Collection authoritiesAluno = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ALUNO"));

        lenient().when(alunoMock.getIdUsuario()).thenReturn(1L);
        lenient().when(alunoMock.getUsername()).thenReturn("aluno@email.com");
        lenient().when(alunoMock.getPassword()).thenReturn("senha");
        lenient().when(alunoMock.getAuthorities()).thenReturn(authoritiesAluno);
        lenient().when(alunoMock.isEnabled()).thenReturn(true);
        lenient().when(alunoMock.isAccountNonExpired()).thenReturn(true);
        lenient().when(alunoMock.isAccountNonLocked()).thenReturn(true);
        lenient().when(alunoMock.isCredentialsNonExpired()).thenReturn(true);

        alunoUserDetails = new CustomUserDetails(alunoMock);

        Usuario profMock = mock(Usuario.class);
        Collection authoritiesProf = Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROFESSOR"));

        lenient().when(profMock.getIdUsuario()).thenReturn(2L);
        lenient().when(profMock.getUsername()).thenReturn("prof@email.com");
        lenient().when(profMock.getPassword()).thenReturn("senha");
        lenient().when(profMock.getAuthorities()).thenReturn(authoritiesProf);
        lenient().when(profMock.isEnabled()).thenReturn(true);
        lenient().when(profMock.isAccountNonExpired()).thenReturn(true);
        lenient().when(profMock.isAccountNonLocked()).thenReturn(true);
        lenient().when(profMock.isCredentialsNonExpired()).thenReturn(true);

        professorUserDetails = new CustomUserDetails(profMock);
    }

    @Test
    void createTentativaQuestionario_HappyPath() throws Exception {
        Long idAluno = 1L;
        TentativaQuestionarioRequestDto requestDto = new TentativaQuestionarioRequestDto();
        requestDto.setIdQuestionario(10L);

        TentativaQuestionarioResponseDto responseDto = new TentativaQuestionarioResponseDto();
        responseDto.setIdQuestionario(10L);
        responseDto.setIdAluno(idAluno);

        when(tentativaQuestionarioService.createTentativaQuestionario(any(TentativaQuestionarioRequestDto.class), eq(idAluno)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/tentativaQuestionario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idQuestionario").value(10L))
                .andExpect(jsonPath("$.idAluno").value(1L));
    }

    @Test
    void createTentativaQuestionario_SadPath_WrongRole() throws Exception {
        TentativaQuestionarioRequestDto requestDto = new TentativaQuestionarioRequestDto();
        requestDto.setIdQuestionario(10L);

        mockMvc.perform(post("/tentativaQuestionario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(authentication(new UsernamePasswordAuthenticationToken(professorUserDetails, null, professorUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTentativaQuestionario_SadPath_LimiteAtingido() throws Exception {
        Long idAluno = 1L;
        TentativaQuestionarioRequestDto requestDto = new TentativaQuestionarioRequestDto();
        requestDto.setIdQuestionario(10L);

        when(tentativaQuestionarioService.createTentativaQuestionario(any(TentativaQuestionarioRequestDto.class), eq(idAluno)))
                .thenThrow(new LimiteTentativasException("Limite atingido"));

        mockMvc.perform(post("/tentativaQuestionario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTentativaQuestionario_SadPath_NotFound() throws Exception {
        Long idAluno = 1L;
        TentativaQuestionarioRequestDto requestDto = new TentativaQuestionarioRequestDto();
        requestDto.setIdQuestionario(99L);

        when(tentativaQuestionarioService.createTentativaQuestionario(any(TentativaQuestionarioRequestDto.class), eq(idAluno)))
                .thenThrow(new RuntimeException("Questionário não encontrado"));

        mockMvc.perform(post("/tentativaQuestionario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllTentativasQuestionario_HappyPath() throws Exception {
        PagedResponse<TentativaQuestionarioResponseDto> pagedResponse = new PagedResponse<>(
            Collections.emptyList(), 0, 10, 0, 0, true
        );

        when(tentativaQuestionarioService.getAllTentativasQuestionario(any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/tentativaQuestionario")
                .param("page", "0")
                .param("size", "10")
                .with(authentication(new UsernamePasswordAuthenticationToken(professorUserDetails, null, professorUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTentativasQuestionarioByAlunoId_HappyPath() throws Exception {
        Long idAluno = 1L;
        PagedResponse<TentativaQuestionarioResponseDto> pagedResponse = new PagedResponse<>(
            Collections.emptyList(), 0, 10, 0, 0, true
        );

        when(tentativaQuestionarioService.getTentativasQuestionarioByAlunoId(eq(idAluno), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/tentativaQuestionario/aluno/{alunoId}", idAluno)
                .with(authentication(new UsernamePasswordAuthenticationToken(professorUserDetails, null, professorUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getMinhasTentativasQuestionario_HappyPath() throws Exception {
        Long idAluno = 1L;
        PagedResponse<TentativaQuestionarioResponseDto> pagedResponse = new PagedResponse<>(
            Collections.emptyList(), 0, 10, 0, 0, true
        );

        when(tentativaQuestionarioService.getTentativasQuestionarioByAlunoId(eq(idAluno), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/tentativaQuestionario/aluno/minhas")
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deleteTentativaQuestionario_HappyPath() throws Exception {
        Long idTentativa = 5L;
        TentativaQuestionarioResponseDto responseDto = new TentativaQuestionarioResponseDto();
        responseDto.setIdQuestionario(10L);

        when(tentativaQuestionarioService.deleteTentativaQuestionario(idTentativa))
                .thenReturn(responseDto);

        mockMvc.perform(delete("/tentativaQuestionario/{idTentativa}", idTentativa)
                .with(authentication(new UsernamePasswordAuthenticationToken(professorUserDetails, null, professorUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTentativaQuestionario_SadPath_AccessDenied() throws Exception {
        Long idTentativa = 5L;

        mockMvc.perform(delete("/tentativaQuestionario/{idTentativa}", idTentativa)
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTentativaQuestionario_SadPath_NotFound() throws Exception {
        Long idTentativa = 99L;

        doThrow(new RuntimeException("Não encontrado"))
                .when(tentativaQuestionarioService).deleteTentativaQuestionario(idTentativa);

        mockMvc.perform(delete("/tentativaQuestionario/{idTentativa}", idTentativa)
                .with(authentication(new UsernamePasswordAuthenticationToken(professorUserDetails, null, professorUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
