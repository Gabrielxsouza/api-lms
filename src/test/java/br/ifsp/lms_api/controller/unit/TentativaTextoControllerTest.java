package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.controller.TentativaTextoController;
import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoRequestDto;
import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoResponseDto;
import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.AccessDeniedException;
import br.ifsp.lms_api.service.TentativaTextoService;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class TentativaTextoControllerTest {

    @Mock
    private TentativaTextoService tentativaTextoService;

    @InjectMocks
    private TentativaTextoController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CustomUserDetails userDetails;

    @RestControllerAdvice
    static class TestControllerAdvice {
        @ExceptionHandler(EntityNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public void handleNotFound() {}

        @ExceptionHandler(AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public void handleAccessDenied() {}
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());

        userDetails = mock(CustomUserDetails.class);

        HandlerMethodArgumentResolver putPrincipal = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return userDetails;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(putPrincipal, new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new TestControllerAdvice())
                .build();
    }

    @Test
    void createTentativaTexto_Success() throws Exception {
        Long idAtividade = 10L;
        Long idAluno = 1L;
        TentativaTextoRequestDto requestDto = new TentativaTextoRequestDto();
        requestDto.setTextoResposta("Minha resposta");

        TentativaTextoResponseDto responseDto = new TentativaTextoResponseDto();
        responseDto.setIdTentativa(100L);
        responseDto.setTextoResposta("Minha resposta");

        when(userDetails.getId()).thenReturn(idAluno);
        when(tentativaTextoService.createTentativaTexto(any(TentativaTextoRequestDto.class), eq(idAluno), eq(idAtividade)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/tentativaTexto/{idAtividade}", idAtividade)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTentativa").value(100L))
                .andExpect(jsonPath("$.textoResposta").value("Minha resposta"));
    }

    @Test
    void createTentativaTexto_WhenEntityNotFound_ShouldReturn404() throws Exception {
        Long idAtividade = 99L;
        Long idAluno = 1L;
        TentativaTextoRequestDto requestDto = new TentativaTextoRequestDto();
        requestDto.setTextoResposta("Resposta");

        when(userDetails.getId()).thenReturn(idAluno);
        when(tentativaTextoService.createTentativaTexto(any(TentativaTextoRequestDto.class), eq(idAluno), eq(idAtividade)))
                .thenThrow(new EntityNotFoundException("Atividade não encontrada"));

        mockMvc.perform(post("/tentativaTexto/{idAtividade}", idAtividade)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTentativaTextoProfessor_Success() throws Exception {
        Long idTentativa = 5L;
        TentativaTextoUpdateDto updateDto = new TentativaTextoUpdateDto();
        updateDto.setNota(Optional.of(9.5));
        updateDto.setFeedback(Optional.of("Bom"));

        TentativaTextoResponseDto responseDto = new TentativaTextoResponseDto();
        responseDto.setIdTentativa(idTentativa);
        responseDto.setNota(9.5);

        when(tentativaTextoService.updateTentativaTextoProfessor(any(TentativaTextoUpdateDto.class), eq(idTentativa)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/tentativaTexto/professor/{idTentativa}", idTentativa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value(9.5));
    }

    @Test
    void updateTentativaTextoProfessor_WhenNotFound_ShouldReturn404() throws Exception {
        Long idTentativa = 99L;
        TentativaTextoUpdateDto updateDto = new TentativaTextoUpdateDto();

        when(tentativaTextoService.updateTentativaTextoProfessor(any(TentativaTextoUpdateDto.class), eq(idTentativa)))
                .thenThrow(new EntityNotFoundException("Tentativa não encontrada"));

        mockMvc.perform(patch("/tentativaTexto/professor/{idTentativa}", idTentativa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMinhaTentativa_Success() throws Exception {
        Long idTentativa = 5L;
        Long idAluno = 1L;
        TentativaTextoUpdateDto updateDto = new TentativaTextoUpdateDto();
        updateDto.setTextoResposta(Optional.of("Nova resposta"));

        TentativaTextoResponseDto responseDto = new TentativaTextoResponseDto();
        responseDto.setIdTentativa(idTentativa);
        responseDto.setTextoResposta("Nova resposta");

        when(userDetails.getId()).thenReturn(idAluno);
        when(tentativaTextoService.updateTentativaTextoAluno(any(TentativaTextoUpdateDto.class), eq(idTentativa), eq(idAluno)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/tentativaTexto/aluno/{idTentativa}", idTentativa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.textoResposta").value("Nova resposta"));
    }

    @Test
    void updateMinhaTentativa_WhenAccessDenied_ShouldReturn403() throws Exception {
        Long idTentativa = 5L;
        Long idAluno = 1L;
        TentativaTextoUpdateDto updateDto = new TentativaTextoUpdateDto();

        when(userDetails.getId()).thenReturn(idAluno);
        when(tentativaTextoService.updateTentativaTextoAluno(any(TentativaTextoUpdateDto.class), eq(idTentativa), eq(idAluno)))
                .thenThrow(new AccessDeniedException("Não permitido"));

        mockMvc.perform(patch("/tentativaTexto/aluno/{idTentativa}", idTentativa)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllTentativasTexto_Success() throws Exception {
        // Usando o construtor com argumentos: content, page, size, totalElements, totalPages, last
        PagedResponse<TentativaTextoResponseDto> pagedResponse = new PagedResponse<>(
            Collections.emptyList(), 0, 10, 0, 0, true
        );

        when(tentativaTextoService.getAllTentativasTexto(any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/tentativaTexto")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deleteTentativaTexto_Success() throws Exception {
        Long idTentativa = 5L;
        Long idAluno = 1L;

        TentativaTextoResponseDto responseDto = new TentativaTextoResponseDto();
        responseDto.setIdTentativa(idTentativa);

        when(userDetails.getId()).thenReturn(idAluno);
        when(tentativaTextoService.deleteTentativaTexto(idTentativa, idAluno))
                .thenReturn(responseDto);

        mockMvc.perform(delete("/tentativaTexto/{idTentativa}", idTentativa))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTentativa").value(idTentativa));
    }

    @Test
    void deleteTentativaTexto_WhenAccessDenied_ShouldReturn403() throws Exception {
        Long idTentativa = 5L;
        Long idAluno = 1L;

        when(userDetails.getId()).thenReturn(idAluno);
        doThrow(new AccessDeniedException("Já corrigida"))
                .when(tentativaTextoService).deleteTentativaTexto(idTentativa, idAluno);

        mockMvc.perform(delete("/tentativaTexto/{idTentativa}", idTentativa))
                .andExpect(status().isForbidden());
    }
}
