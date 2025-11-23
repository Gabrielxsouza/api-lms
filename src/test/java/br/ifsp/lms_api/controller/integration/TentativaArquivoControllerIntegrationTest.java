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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.TentativaArquivoDto.TentativaArquivoResponseDto;
import br.ifsp.lms_api.dto.TentativaArquivoDto.TentativaArquivoUpdateDto;
import br.ifsp.lms_api.exception.AccessDeniedException;
import br.ifsp.lms_api.model.Usuario;
import br.ifsp.lms_api.service.TentativaArquivoService;
import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc

@Import(TentativaArquivoControllerIntegrationTest.TestControllerAdvice.class)
class TentativaArquivoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TentativaArquivoService tentativaArquivoService;

    private CustomUserDetails alunoUserDetails;
    private CustomUserDetails professorUserDetails;

    @TestConfiguration
    @RestControllerAdvice
    static class TestControllerAdvice {

        @ExceptionHandler(EntityNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public void handleEntityNotFound() {}

        @ExceptionHandler(AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public void handleCustomAccessDenied() {}
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
    void createTentativaArquivo_HappyPath() throws Exception {
        Long idAtividade = 10L;
        MockMultipartFile file = new MockMultipartFile("arquivo", "teste.pdf", "application/pdf", "content".getBytes());

        TentativaArquivoResponseDto responseDto = new TentativaArquivoResponseDto();
        responseDto.setIdTentativa(100L);

        when(tentativaArquivoService.createTentativaArquivo(any(), eq(1L), eq(idAtividade)))
                .thenReturn(responseDto);

        mockMvc.perform(multipart("/tentativaArquivo/{idAtividade}", idAtividade)
                .file(file)
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTentativa").value(100L));
    }

    @Test
    void createTentativaArquivo_SadPath_WrongRole() throws Exception {
        Long idAtividade = 10L;
        MockMultipartFile file = new MockMultipartFile("arquivo", "teste.pdf", "application/pdf", "content".getBytes());

        mockMvc.perform(multipart("/tentativaArquivo/{idAtividade}", idAtividade)
                .file(file)
                .with(authentication(new UsernamePasswordAuthenticationToken(professorUserDetails, null, professorUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTentativaArquivo_SadPath_EntityNotFound() throws Exception {
        Long idAtividade = 99L;
        MockMultipartFile file = new MockMultipartFile("arquivo", "teste.pdf", "application/pdf", "content".getBytes());

        when(tentativaArquivoService.createTentativaArquivo(any(), eq(1L), eq(idAtividade)))
                .thenThrow(new EntityNotFoundException("Atividade não encontrada"));

        mockMvc.perform(multipart("/tentativaArquivo/{idAtividade}", idAtividade)
                .file(file)
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTentativaArquivoProfessor_HappyPath() throws Exception {
        Long idTentativa = 5L;
        TentativaArquivoUpdateDto dto = new TentativaArquivoUpdateDto();
        dto.setNota(Optional.of(10.0));
        dto.setFeedback(Optional.of("Excelente"));

        TentativaArquivoResponseDto responseDto = new TentativaArquivoResponseDto();
        responseDto.setIdTentativa(idTentativa);
        responseDto.setNota(10.0);

        when(tentativaArquivoService.updateTentativaArquivoProfessor(any(TentativaArquivoUpdateDto.class), eq(idTentativa)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/tentativaArquivo/professor/{idTentativa}", idTentativa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(authentication(new UsernamePasswordAuthenticationToken(professorUserDetails, null, professorUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value(10.0));
    }

    @Test
    void updateTentativaArquivoAluno_HappyPath() throws Exception {
        Long idTentativa = 5L;
        MockMultipartFile file = new MockMultipartFile("arquivo", "novo.pdf", "application/pdf", "novo content".getBytes());

        TentativaArquivoResponseDto responseDto = new TentativaArquivoResponseDto();
        responseDto.setIdTentativa(idTentativa);

        when(tentativaArquivoService.updateTentativaArquivoAluno(eq(idTentativa), eq(1L), any()))
                .thenReturn(responseDto);

        mockMvc.perform(multipart("/tentativaArquivo/aluno/{idTentativa}", idTentativa)
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTentativa").value(idTentativa));
    }

    @Test
    void deleteTentativaArquivo_SadPath_AccessDenied() throws Exception {
        Long idTentativa = 5L;

        doThrow(new AccessDeniedException("Não permitido"))
                .when(tentativaArquivoService).deleteTentativaArquivo(idTentativa, 1L);

        mockMvc.perform(delete("/tentativaArquivo/{idTentativa}", idTentativa)
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
