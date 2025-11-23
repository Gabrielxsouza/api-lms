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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

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
import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoRequestDto;
import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoResponseDto;
import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.AccessDeniedException;
import br.ifsp.lms_api.model.Usuario;
import br.ifsp.lms_api.service.TentativaTextoService;
import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TentativaTextoControllerIntegrationTest.TestControllerAdvice.class)
class TentativaTextoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TentativaTextoService tentativaTextoService;

    private CustomUserDetails alunoUserDetails;
    private CustomUserDetails professorUserDetails;

    @TestConfiguration
    @RestControllerAdvice
    public static class TestControllerAdvice {

        @ExceptionHandler(EntityNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public void handleEntityNotFound() {}

        @ExceptionHandler(AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public void handleAccessDenied() {}
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
    void createTentativaTexto_HappyPath() throws Exception {
        Long idAtividade = 10L;
        TentativaTextoRequestDto requestDto = new TentativaTextoRequestDto();
        requestDto.setTextoResposta("Resposta do Aluno");

        TentativaTextoResponseDto responseDto = new TentativaTextoResponseDto();
        responseDto.setIdTentativa(100L);
        responseDto.setTextoResposta("Resposta do Aluno");

        when(tentativaTextoService.createTentativaTexto(any(TentativaTextoRequestDto.class), eq(1L), eq(idAtividade)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/tentativaTexto/{idAtividade}", idAtividade)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTentativa").value(100L))
                .andExpect(jsonPath("$.textoResposta").value("Resposta do Aluno"));
    }

    @Test
    void createTentativaTexto_SadPath_WrongRole() throws Exception {
        Long idAtividade = 10L;
        TentativaTextoRequestDto requestDto = new TentativaTextoRequestDto();
        requestDto.setTextoResposta("Resposta");

        mockMvc.perform(post("/tentativaTexto/{idAtividade}", idAtividade)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(authentication(new UsernamePasswordAuthenticationToken(professorUserDetails, null, professorUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTentativaTextoProfessor_HappyPath() throws Exception {
        Long idTentativa = 5L;
        TentativaTextoUpdateDto updateDto = new TentativaTextoUpdateDto();
        updateDto.setNota(Optional.of(10.0));
        updateDto.setFeedback(Optional.of("Perfeito"));

        TentativaTextoResponseDto responseDto = new TentativaTextoResponseDto();
        responseDto.setIdTentativa(idTentativa);
        responseDto.setNota(10.0);

        when(tentativaTextoService.updateTentativaTextoProfessor(any(TentativaTextoUpdateDto.class), eq(idTentativa)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/tentativaTexto/professor/{idTentativa}", idTentativa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(authentication(new UsernamePasswordAuthenticationToken(professorUserDetails, null, professorUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value(10.0));
    }

    @Test
    void updateMinhaTentativa_HappyPath() throws Exception {
        Long idTentativa = 5L;
        TentativaTextoUpdateDto updateDto = new TentativaTextoUpdateDto();
        updateDto.setTextoResposta(Optional.of("Nova resposta"));

        TentativaTextoResponseDto responseDto = new TentativaTextoResponseDto();
        responseDto.setIdTentativa(idTentativa);
        responseDto.setTextoResposta("Nova resposta");

        when(tentativaTextoService.updateTentativaTextoAluno(any(TentativaTextoUpdateDto.class), eq(idTentativa), eq(1L)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/tentativaTexto/aluno/{idTentativa}", idTentativa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.textoResposta").value("Nova resposta"));
    }

    @Test
    void getAllTentativasTexto_HappyPath() throws Exception {
        PagedResponse<TentativaTextoResponseDto> pagedResponse = new PagedResponse<>(
            Collections.emptyList(), 0, 10, 0, 0, true
        );

        when(tentativaTextoService.getAllTentativasTexto(any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/tentativaTexto")
                .param("page", "0")
                .param("size", "10")
                .with(authentication(new UsernamePasswordAuthenticationToken(professorUserDetails, null, professorUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deleteTentativaTexto_SadPath_EntityNotFound() throws Exception {
        Long idTentativa = 99L;

        doThrow(new EntityNotFoundException("Não encontrado"))
                .when(tentativaTextoService).deleteTentativaTexto(idTentativa, 1L);

        mockMvc.perform(delete("/tentativaTexto/{idTentativa}", idTentativa)
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTentativaTexto_SadPath_AccessDenied() throws Exception {
        Long idTentativa = 5L;

        doThrow(new AccessDeniedException("Não permitido"))
                .when(tentativaTextoService).deleteTentativaTexto(idTentativa, 1L);

        mockMvc.perform(delete("/tentativaTexto/{idTentativa}", idTentativa)
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
