package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.controller.AtividadeArquivosController;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.AtividadeArquivosService;

@ExtendWith(MockitoExtension.class)
public class AtividadeArquivosControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AtividadeArquivosService atividadeArquivosService;

    @InjectMocks
    private AtividadeArquivosController atividadeArquivosController;

    private ObjectMapper objectMapper;

    private AtividadeArquivosResponseDto responseDto;
    private AtividadeArquivosRequestDto requestDto;
    private LocalDate dataInicio;
    private LocalDate dataFechamento;
    
    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // Garante que o Jackson processe a herança corretamente
        objectMapper.findAndRegisterModules(); 

        dataInicio = LocalDate.of(2025, 11, 1);
        dataFechamento = LocalDate.of(2025, 11, 30);

        mockUserDetails = mock(CustomUserDetails.class);
        // CORREÇÃO: lenient() permite que este stub não seja usado em alguns testes (como getAll) sem dar erro
        lenient().when(mockUserDetails.getId()).thenReturn(1L); 

        responseDto = new AtividadeArquivosResponseDto();
        responseDto.setIdAtividade(1L);
        responseDto.setTituloAtividade("Trabalho de Java");
        responseDto.setDescricaoAtividade("Descricao teste");
        responseDto.setDataInicioAtividade(dataInicio);
        responseDto.setDataFechamentoAtividade(dataFechamento);
        responseDto.setStatusAtividade(true);
        responseDto.setArquivosPermitidos(List.of(".pdf", ".zip"));

        requestDto = new AtividadeArquivosRequestDto();
        requestDto.setTituloAtividade("Trabalho de Java");
        requestDto.setDescricaoAtividade("Descricao teste");
        requestDto.setDataInicioAtividade(dataInicio);
        requestDto.setDataFechamentoAtividade(dataFechamento);
        requestDto.setStatusAtividade(true);
        requestDto.setArquivosPermitidos(List.of(".pdf", ".zip"));
        requestDto.setIdTopico(10L);

        mockMvc = MockMvcBuilders.standaloneSetup(atividadeArquivosController)
                .setCustomArgumentResolvers(
                    new HandlerMethodArgumentResolver() {
                        @Override
                        public boolean supportsParameter(MethodParameter parameter) {
                            return parameter.getParameterType().isAssignableFrom(CustomUserDetails.class);
                        }
                        @Override
                        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                            return mockUserDetails;
                        }
                    },
                    new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    void testCreate_Success() throws Exception {
        Long idUsuario = 1L;

        when(atividadeArquivosService.createAtividadeArquivos(any(AtividadeArquivosRequestDto.class), eq(idUsuario)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/atividades-arquivo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAtividade").value(1L));

        verify(atividadeArquivosService, times(1)).createAtividadeArquivos(any(AtividadeArquivosRequestDto.class), eq(idUsuario));
    }

    @Test
    void testCreate_InvalidInput() throws Exception {
        // Cria um DTO vazio propositalmente
        AtividadeArquivosRequestDto invalidDto = new AtividadeArquivosRequestDto();

        mockMvc.perform(post("/atividades-arquivo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest()); 

        verify(atividadeArquivosService, never()).createAtividadeArquivos(any(), anyLong());
    }

    @Test
    void testGetAll_Success() throws Exception {
        List<AtividadeArquivosResponseDto> dtoList = List.of(responseDto);
        
        PagedResponse<AtividadeArquivosResponseDto> pagedResponse = mock(PagedResponse.class);
        when(pagedResponse.getContent()).thenReturn(dtoList);
        when(pagedResponse.getTotalElements()).thenReturn(1L);

        when(atividadeArquivosService.getAllAtividadesArquivos(any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/atividades-arquivo")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(atividadeArquivosService, times(1)).getAllAtividadesArquivos(any(Pageable.class));
    }

    @Test
    void testUpdate_Success() throws Exception {
        Long id = 1L;
        Long idUsuario = 1L;

        AtividadeArquivosUpdateDto updateDto = new AtividadeArquivosUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Trabalho de Java V2"));
        
        when(atividadeArquivosService.updateAtividadeArquivos(eq(id), any(AtividadeArquivosUpdateDto.class), eq(idUsuario)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/atividades-arquivo/{id}", id) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        verify(atividadeArquivosService, times(1)).updateAtividadeArquivos(eq(id), any(AtividadeArquivosUpdateDto.class), eq(idUsuario));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        Long id = 1L;
        Long idUsuario = 1L;
        
        AtividadeArquivosUpdateDto updateDto = new AtividadeArquivosUpdateDto();

        when(atividadeArquivosService.updateAtividadeArquivos(eq(id), any(AtividadeArquivosUpdateDto.class), eq(idUsuario)))
                .thenThrow(new ResourceNotFoundException("Atividade não encontrada"));

        // Nota: Como estamos em standalone sem ControllerAdvice global, a exceção "vaza"
        // Podemos verificar que a requisição falha com o erro esperado
        try {
            mockMvc.perform(patch("/atividades-arquivo/{id}", id) 
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)));
        } catch (Exception e) {
             // Verifica se a causa raiz foi o ResourceNotFoundException
             if (!(e.getCause() instanceof ResourceNotFoundException)) {
                 throw e; // Se não for o erro esperado, relança
             }
        }
        
        verify(atividadeArquivosService, times(1)).updateAtividadeArquivos(eq(id), any(AtividadeArquivosUpdateDto.class), eq(idUsuario));
    }

    @Test
    void testDelete_Success() throws Exception {
        Long id = 1L;
        Long idUsuario = 1L;
        
        doNothing().when(atividadeArquivosService).deleteAtividadeArquivos(id, idUsuario);

        mockMvc.perform(delete("/atividades-arquivo/{id}", id))
                .andExpect(status().isNoContent()); 

        verify(atividadeArquivosService, times(1)).deleteAtividadeArquivos(id, idUsuario);
    }

    @Test
    void testDelete_NotFound() throws Exception {
        Long id = 1L;
        Long idUsuario = 1L;

        doThrow(new ResourceNotFoundException("Atividade não encontrada"))
            .when(atividadeArquivosService).deleteAtividadeArquivos(id, idUsuario);

        try {
            mockMvc.perform(delete("/atividades-arquivo/{id}", id));
        } catch (Exception e) {
            if (!(e.getCause() instanceof ResourceNotFoundException)) {
                throw e;
            }
        }

        verify(atividadeArquivosService, times(1)).deleteAtividadeArquivos(id, idUsuario);
    }
}