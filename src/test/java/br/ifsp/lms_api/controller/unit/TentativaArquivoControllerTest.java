package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import br.ifsp.lms_api.controller.TentativaArquivoController;
import br.ifsp.lms_api.dto.TentativaArquivoDto.TentativaArquivoResponseDto;
import br.ifsp.lms_api.dto.TentativaArquivoDto.TentativaArquivoUpdateDto;
import br.ifsp.lms_api.exception.AccessDeniedException;
import br.ifsp.lms_api.service.TentativaArquivoService;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class TentativaArquivoControllerTest {

    @Mock
    private TentativaArquivoService tentativaArquivoService;

    @InjectMocks
    private TentativaArquivoController controller;

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
                .setCustomArgumentResolvers(putPrincipal)
                .setControllerAdvice(new TestControllerAdvice())
                .build();
    }

    @Test
    void createTentativaArquivo_Success() throws Exception {
        Long idAtividade = 10L;
        Long idAluno = 1L;
        MockMultipartFile file = new MockMultipartFile("arquivo", "teste.pdf", "application/pdf", "content".getBytes());

        TentativaArquivoResponseDto responseDto = new TentativaArquivoResponseDto();
        responseDto.setIdTentativa(100L);

        when(userDetails.getId()).thenReturn(idAluno);
        when(tentativaArquivoService.createTentativaArquivo(any(), eq(idAluno), eq(idAtividade)))
                .thenReturn(responseDto);

        mockMvc.perform(multipart("/tentativaArquivo/{idAtividade}", idAtividade)
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTentativa").value(100L));
    }

    @Test
    void createTentativaArquivo_WhenEntityNotFound_ShouldReturn404() throws Exception {
        Long idAtividade = 10L;
        Long idAluno = 1L;
        MockMultipartFile file = new MockMultipartFile("arquivo", "teste.pdf", "application/pdf", "content".getBytes());

        when(userDetails.getId()).thenReturn(idAluno);
        when(tentativaArquivoService.createTentativaArquivo(any(), eq(idAluno), eq(idAtividade)))
                .thenThrow(new EntityNotFoundException("Not found"));

        mockMvc.perform(multipart("/tentativaArquivo/{idAtividade}", idAtividade)
                .file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTentativaArquivoProfessor_Success() throws Exception {
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
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value(10.0));
    }

    @Test
    void updateTentativaArquivoAluno_Success() throws Exception {
        Long idTentativa = 5L;
        Long idAluno = 1L;
        MockMultipartFile file = new MockMultipartFile("arquivo", "novo.pdf", "application/pdf", "novo content".getBytes());

        TentativaArquivoResponseDto responseDto = new TentativaArquivoResponseDto();
        responseDto.setIdTentativa(idTentativa);

        when(userDetails.getId()).thenReturn(idAluno);
        when(tentativaArquivoService.updateTentativaArquivoAluno(eq(idTentativa), eq(idAluno), any()))
                .thenReturn(responseDto);

        mockMvc.perform(multipart("/tentativaArquivo/aluno/{idTentativa}", idTentativa)
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTentativa").value(idTentativa));
    }

    @Test
    void updateTentativaArquivoAluno_WhenAccessDenied_ShouldReturn403() throws Exception {
        Long idTentativa = 5L;
        Long idAluno = 1L;
        MockMultipartFile file = new MockMultipartFile("arquivo", "novo.pdf", "application/pdf", "content".getBytes());

        when(userDetails.getId()).thenReturn(idAluno);
        when(tentativaArquivoService.updateTentativaArquivoAluno(eq(idTentativa), eq(idAluno), any()))
                .thenThrow(new AccessDeniedException("Denied"));

        mockMvc.perform(multipart("/tentativaArquivo/aluno/{idTentativa}", idTentativa)
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTentativaArquivo_Success() throws Exception {
        Long idTentativa = 5L;
        Long idAluno = 1L;

        TentativaArquivoResponseDto responseDto = new TentativaArquivoResponseDto();
        responseDto.setIdTentativa(idTentativa);

        when(userDetails.getId()).thenReturn(idAluno);
        when(tentativaArquivoService.deleteTentativaArquivo(idTentativa, idAluno))
                .thenReturn(responseDto);

        mockMvc.perform(delete("/tentativaArquivo/{idTentativa}", idTentativa))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTentativa").value(idTentativa));
    }

    @Test
    void deleteTentativaArquivo_WhenNotFound_ShouldReturn404() throws Exception {
        Long idTentativa = 5L;
        Long idAluno = 1L;

        when(userDetails.getId()).thenReturn(idAluno);
        doThrow(new EntityNotFoundException("Not found"))
                .when(tentativaArquivoService).deleteTentativaArquivo(idTentativa, idAluno);

        mockMvc.perform(delete("/tentativaArquivo/{idTentativa}", idTentativa))
                .andExpect(status().isNotFound());
    }
}
