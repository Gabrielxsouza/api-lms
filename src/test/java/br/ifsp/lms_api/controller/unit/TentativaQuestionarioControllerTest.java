package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

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
import br.ifsp.lms_api.controller.TentativaQuestionarioController;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioRequestDto;
import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioResponseDto;
import br.ifsp.lms_api.exception.LimiteTentativasException;
import br.ifsp.lms_api.service.TentativaQuestionarioService;

@ExtendWith(MockitoExtension.class)
class TentativaQuestionarioControllerTest {

    @Mock
    private TentativaQuestionarioService tentativaQuestionarioService;

    @InjectMocks
    private TentativaQuestionarioController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CustomUserDetails userDetails;

    @RestControllerAdvice
    static class TestControllerAdvice {
        @ExceptionHandler(RuntimeException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public void handleNotFound() {}

        @ExceptionHandler(LimiteTentativasException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public void handleLimiteTentativas() {}
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
    void createTentativaQuestionario_Success() throws Exception {
        Long idAluno = 1L;
        TentativaQuestionarioRequestDto requestDto = new TentativaQuestionarioRequestDto();
        requestDto.setIdQuestionario(10L);

        TentativaQuestionarioResponseDto responseDto = new TentativaQuestionarioResponseDto();
        responseDto.setIdQuestionario(10L);
        responseDto.setIdAluno(idAluno);

        when(userDetails.getId()).thenReturn(idAluno);
        when(tentativaQuestionarioService.createTentativaQuestionario(any(TentativaQuestionarioRequestDto.class), eq(idAluno)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/tentativaQuestionario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idQuestionario").value(10L))
                .andExpect(jsonPath("$.idAluno").value(1L));
    }

    @Test
    void createTentativaQuestionario_LimiteAtingido_ShouldReturn400() throws Exception {
        Long idAluno = 1L;
        TentativaQuestionarioRequestDto requestDto = new TentativaQuestionarioRequestDto();
        requestDto.setIdQuestionario(10L); // Preenchido para passar na validação @Validated

        when(userDetails.getId()).thenReturn(idAluno);
        when(tentativaQuestionarioService.createTentativaQuestionario(any(TentativaQuestionarioRequestDto.class), eq(idAluno)))
                .thenThrow(new LimiteTentativasException("Limite atingido"));

        mockMvc.perform(post("/tentativaQuestionario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllTentativasQuestionario_Success() throws Exception {
        PagedResponse<TentativaQuestionarioResponseDto> pagedResponse = new PagedResponse<>(
            Collections.emptyList(), 0, 10, 0, 0, true
        );

        when(tentativaQuestionarioService.getAllTentativasQuestionario(any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/tentativaQuestionario")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTentativasQuestionarioByAlunoId_Success() throws Exception {
        Long idAluno = 1L;
        PagedResponse<TentativaQuestionarioResponseDto> pagedResponse = new PagedResponse<>(
            Collections.emptyList(), 0, 10, 0, 0, true
        );

        when(tentativaQuestionarioService.getTentativasQuestionarioByAlunoId(eq(idAluno), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/tentativaQuestionario/aluno/{alunoId}", idAluno)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getMinhasTentativasQuestionario_Success() throws Exception {
        Long idAluno = 1L;
        PagedResponse<TentativaQuestionarioResponseDto> pagedResponse = new PagedResponse<>(
            Collections.emptyList(), 0, 10, 0, 0, true
        );

        when(userDetails.getId()).thenReturn(idAluno);
        when(tentativaQuestionarioService.getTentativasQuestionarioByAlunoId(eq(idAluno), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/tentativaQuestionario/aluno/minhas")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deleteTentativaQuestionario_Success() throws Exception {
        Long idTentativa = 5L;
        TentativaQuestionarioResponseDto responseDto = new TentativaQuestionarioResponseDto();

        when(tentativaQuestionarioService.deleteTentativaQuestionario(idTentativa))
                .thenReturn(responseDto);

        mockMvc.perform(delete("/tentativaQuestionario/{idTentativa}", idTentativa))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTentativaQuestionario_NotFound_ShouldReturn404() throws Exception {
        Long idTentativa = 99L;

        doThrow(new RuntimeException("Não encontrado"))
                .when(tentativaQuestionarioService).deleteTentativaQuestionario(idTentativa);

        mockMvc.perform(delete("/tentativaQuestionario/{idTentativa}", idTentativa))
                .andExpect(status().isNotFound());
    }
}
