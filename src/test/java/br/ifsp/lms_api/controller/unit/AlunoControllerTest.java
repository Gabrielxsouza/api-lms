package br.ifsp.lms_api.controller.unit;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver; // IMPORT IMPORTANTE
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.controller.AlunoController;
import br.ifsp.lms_api.dto.alunoDto.AlunoRequestDto;
import br.ifsp.lms_api.dto.alunoDto.AlunoResponseDto;
import br.ifsp.lms_api.dto.alunoDto.AlunoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.service.AlunoService;

@ExtendWith(MockitoExtension.class)
public class AlunoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AlunoService alunoService;

    @InjectMocks
    private AlunoController alunoController;

    private ObjectMapper objectMapper;

    private AlunoResponseDto responseDto;
    private AlunoRequestDto requestDto;
    private AlunoUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        // --- CORREÇÃO AQUI ---
        // Adicionamos o PageableHandlerMethodArgumentResolver para o MockMvc entender paginação
        mockMvc = MockMvcBuilders.standaloneSetup(alunoController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()) 
                .build();
        // ---------------------

        responseDto = new AlunoResponseDto(
            1L, "Aluno Teste", "aluno@test.com", "123.456.789-00", "RA123456"
        );

        requestDto = new AlunoRequestDto(
            "Aluno Teste", "aluno@test.com", "senha123", "123.456.789-00", "RA123456"
        );

        updateDto = new AlunoUpdateDto(
            Optional.of("Aluno Atualizado"), Optional.empty(), Optional.empty(), Optional.empty()
        );
    }

    @Test
    void testCreate_Success() throws Exception {
        when(alunoService.createAluno(any(AlunoRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/alunos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(1L))
                .andExpect(jsonPath("$.nome").value("Aluno Teste"));

        verify(alunoService).createAluno(any(AlunoRequestDto.class));
    }

    @Test
    void testGetAll_Success() throws Exception {
        PagedResponse<AlunoResponseDto> pagedResponse = new PagedResponse<>(
            List.of(responseDto), 0, 10, 1L, 1, true
        );
        
        // O ArgumentMatcher any(Pageable.class) agora vai funcionar
        when(alunoService.getAllAlunos(any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/alunos")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Aluno Teste"));
    }

    @Test
    void testUpdate_Success() throws Exception {
        when(alunoService.updateAluno(eq(1L), any(AlunoUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/alunos/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
        
        verify(alunoService).updateAluno(eq(1L), any(AlunoUpdateDto.class));
    }

    @Test
    void testDelete_Success() throws Exception {
        doNothing().when(alunoService).deleteAluno(1L);

        mockMvc.perform(delete("/alunos/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(alunoService).deleteAluno(1L);
    }
}