package br.ifsp.lms_api.controller; 

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.ifsp.lms_api.model.AtividadeArquivos;
import br.ifsp.lms_api.repository.AtividadeArquivosRepository;

import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;

@SpringBootTest 
@AutoConfigureMockMvc 
@Transactional 
public class AtividadeArquivosControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; 

    @Autowired
    private ObjectMapper objectMapper; 

    @Autowired
    private AtividadeArquivosRepository atividadeArquivosRepository; 

    private LocalDate dataInicio;
    private LocalDate dataFechamento;
    private AtividadeArquivos atividadeExistente;

    @BeforeEach
    void setUp() {
        atividadeArquivosRepository.deleteAll();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules();

        dataInicio = LocalDate.of(2025, 11, 1);
        dataFechamento = LocalDate.of(2025, 11, 30);

        atividadeExistente = new AtividadeArquivos();
        atividadeExistente.setTituloAtividade("Atividade de Teste Existente");
        atividadeExistente.setDescricaoAtividade("Descrição");
        atividadeExistente.setDataInicioAtividade(dataInicio);
        atividadeExistente.setDataFechamentoAtividade(dataFechamento);
        atividadeExistente.setStatusAtividade(true);
        atividadeExistente.setArquivosPermitidos(new ArrayList<>(List.of(".pdf")));

        atividadeExistente = atividadeArquivosRepository.save(atividadeExistente);
    }

    @Test
    void testCreate_Success() throws Exception {
        AtividadeArquivosRequestDto requestDto = new AtividadeArquivosRequestDto();
        requestDto.setTituloAtividade("Nova Atividade de Arquivo");
        requestDto.setDescricaoAtividade("Nova Descrição");
        requestDto.setDataInicioAtividade(dataInicio);
        requestDto.setDataFechamentoAtividade(dataFechamento);
        requestDto.setStatusAtividade(true);
        requestDto.setArquivosPermitidos(List.of(".pdf", ".zip"));

        mockMvc.perform(post("/atividades-arquivo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAtividade").exists()) 
                .andExpect(jsonPath("$.tituloAtividade", is("Nova Atividade de Arquivo")))
                .andExpect(jsonPath("$.arquivosPermitidos[1]", is(".zip")));

        assertTrue(atividadeArquivosRepository.count() == 2);
    }

    @Test
    void testGetAll_Success() throws Exception {
        mockMvc.perform(get("/atividades-arquivo")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].idAtividade", is(atividadeExistente.getIdAtividade().intValue())))
                .andExpect(jsonPath("$.content[0].tituloAtividade", is("Atividade de Teste Existente")));
    }

    @Test
    void testUpdate_Success() throws Exception {
        Long id = atividadeExistente.getIdAtividade();

        String updateJson = """
                {
                    "tituloAtividade": "Título Atualizado"
                }
                """;

        mockMvc.perform(patch("/atividades-arquivo/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade", is(id.intValue())))
                .andExpect(jsonPath("$.tituloAtividade", is("Título Atualizado")))
                .andExpect(jsonPath("$.descricaoAtividade", is("Descrição"))); 
    }

    @Test
    void testDelete_Success() throws Exception {
        Long id = atividadeExistente.getIdAtividade();
        assertTrue(atividadeArquivosRepository.findById(id).isPresent());

        mockMvc.perform(delete("/atividades-arquivo/{id}", id))
                .andExpect(status().isNoContent());

        assertFalse(atividadeArquivosRepository.findById(id).isPresent());
        assertTrue(atividadeArquivosRepository.count() == 0);
    }

    @Test
    void testCreate_InvalidInput_ReturnsBadRequest() throws Exception {
        AtividadeArquivosRequestDto requestDto = new AtividadeArquivosRequestDto();
        requestDto.setTituloAtividade(null); 
        requestDto.setDescricaoAtividade("Descrição válida");
        requestDto.setDataInicioAtividade(dataInicio);
        requestDto.setDataFechamentoAtividade(dataFechamento);
        requestDto.setStatusAtividade(true);
        requestDto.setArquivosPermitidos(List.of(".pdf")); 

        mockMvc.perform(post("/atividades-arquivo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest()); 

        assertTrue(atividadeArquivosRepository.count() == 1);
    }

    @Test
    void testUpdate_NotFound_ReturnsNotFound() throws Exception {
        Long idQueNaoExiste = 999L; 
        String updateJson = """
                {
                    "tituloAtividade": "Título Fantasma"
                }
                """;

        mockMvc.perform(patch("/atividades-arquivo/{id}", idQueNaoExiste)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound()); 
    }

    @Test
    void testDelete_NotFound_ReturnsNotFound() throws Exception {
        Long idQueNaoExiste = 999L; 

        assertFalse(atividadeArquivosRepository.findById(idQueNaoExiste).isPresent());

        mockMvc.perform(delete("/atividades-arquivo/{id}", idQueNaoExiste))
                .andExpect(status().isNotFound()); 

        assertTrue(atividadeArquivosRepository.count() == 1);
    }
}
