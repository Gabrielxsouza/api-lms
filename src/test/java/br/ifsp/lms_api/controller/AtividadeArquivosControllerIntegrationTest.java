package br.ifsp.lms_api.controller; // Pode ser no mesmo pacote do controller ou num subpacote

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

// Importe suas Entidades e Repositórios REAIS
import br.ifsp.lms_api.model.AtividadeArquivos;
import br.ifsp.lms_api.repository.AtividadeArquivosRepository;

// Importe seus DTOs
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;

@SpringBootTest // 1. Carrega o contexto COMPLETO da aplicação
@AutoConfigureMockMvc // 2. Configura o MockMvc para fazer requisições HTTP
@Transactional // 3. Garante que cada teste rode em uma transação e dê rollback no final
public class AtividadeArquivosControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Para fazer as requisições HTTP

    @Autowired
    private ObjectMapper objectMapper; // Para converter objetos para JSON

    @Autowired
    private AtividadeArquivosRepository atividadeArquivosRepository; // O repositório REAL

    private LocalDate dataInicio;
    private LocalDate dataFechamento;
    private AtividadeArquivos atividadeExistente;

    @BeforeEach
    void setUp() {
        // Limpa o banco antes de cada teste (embora o @Transactional já faça isso)
        atividadeArquivosRepository.deleteAll();

        // Configura o ObjectMapper para datas
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules();

        dataInicio = LocalDate.of(2025, 11, 1);
        dataFechamento = LocalDate.of(2025, 11, 30);

        // ARRANGE: Insere dados REAIS no banco H2
        atividadeExistente = new AtividadeArquivos();
        atividadeExistente.setTituloAtividade("Atividade de Teste Existente");
        atividadeExistente.setDescricaoAtividade("Descrição");
        atividadeExistente.setDataInicioAtividade(dataInicio);
        atividadeExistente.setDataFechamentoAtividade(dataFechamento);
        atividadeExistente.setStatusAtividade(true);
        atividadeExistente.setArquivosPermitidos(new ArrayList<>(List.of(".pdf")));

        // Salva e atualiza o ID na variável
        atividadeExistente = atividadeArquivosRepository.save(atividadeExistente);
    }

    @Test
    void testCreate_Success() throws Exception {
        // ARRANGE (Preparar)
        AtividadeArquivosRequestDto requestDto = new AtividadeArquivosRequestDto();
        requestDto.setTituloAtividade("Nova Atividade de Arquivo");
        requestDto.setDescricaoAtividade("Nova Descrição");
        requestDto.setDataInicioAtividade(dataInicio);
        requestDto.setDataFechamentoAtividade(dataFechamento);
        requestDto.setStatusAtividade(true);
        requestDto.setArquivosPermitidos(List.of(".pdf", ".zip"));

        // ACT (Executar)
        mockMvc.perform(post("/atividades-arquivo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                // ASSERT (Verificar HTTP)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAtividade").exists()) // Verifica se um ID foi gerado
                .andExpect(jsonPath("$.tituloAtividade", is("Nova Atividade de Arquivo")))
                .andExpect(jsonPath("$.arquivosPermitidos[1]", is(".zip")));

        // ASSERT (Verificar no BANCO DE DADOS)
        // Havia 1 (do setUp), agora deve haver 2
        assertTrue(atividadeArquivosRepository.count() == 2);
    }

    @Test
    void testGetAll_Success() throws Exception {
        // ARRANGE
        // O item 'atividadeExistente' já foi inserido no setUp()

        // ACT & ASSERT
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
        // ARRANGE
        // O item 'atividadeExistente' já foi inserido no setUp()
        Long id = atividadeExistente.getIdAtividade();

        // JSON que o frontend enviaria (só o campo a ser alterado)
        String updateJson = """
                {
                    "tituloAtividade": "Título Atualizado"
                }
                """;

        // ACT
        mockMvc.perform(patch("/atividades-arquivo/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                // ASSERT (HTTP)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade", is(id.intValue())))
                .andExpect(jsonPath("$.tituloAtividade", is("Título Atualizado")))
                .andExpect(jsonPath("$.descricaoAtividade", is("Descrição"))); // Campo antigo (não mudou)
    }

    @Test
    void testDelete_Success() throws Exception {
        // ARRANGE
        // O item 'atividadeExistente' já foi inserido no setUp()
        Long id = atividadeExistente.getIdAtividade();
        assertTrue(atividadeArquivosRepository.findById(id).isPresent());

        // ACT
        mockMvc.perform(delete("/atividades-arquivo/{id}", id))
                // ASSERT (HTTP)
                .andExpect(status().isNoContent());

        // ASSERT (BANCO DE DADOS)
        // Verifica se o item foi REALMENTE deletado do banco
        assertFalse(atividadeArquivosRepository.findById(id).isPresent());
        assertTrue(atividadeArquivosRepository.count() == 0);
    }

    @Test
    void testCreate_InvalidInput_ReturnsBadRequest() throws Exception {
        // ARRANGE (Preparar)
        // DTO inválido (título é @NotBlank, mas está nulo)
        AtividadeArquivosRequestDto requestDto = new AtividadeArquivosRequestDto();
        requestDto.setTituloAtividade(null); // Campo inválido
        requestDto.setDescricaoAtividade("Descrição válida");
        requestDto.setDataInicioAtividade(dataInicio);
        requestDto.setDataFechamentoAtividade(dataFechamento);
        requestDto.setStatusAtividade(true);
        requestDto.setArquivosPermitidos(List.of(".pdf")); // Campo válido

        // ACT (Executar)
        mockMvc.perform(post("/atividades-arquivo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                // ASSERT (Verificar HTTP)
                .andExpect(status().isBadRequest()); // Esperamos 400

        // ASSERT (Verificar no BANCO DE DADOS)
        // O número de itens no banco não deve mudar (continuar 1 do setUp)
        assertTrue(atividadeArquivosRepository.count() == 1);
    }

    @Test
    void testUpdate_NotFound_ReturnsNotFound() throws Exception {
        // ARRANGE
        Long idQueNaoExiste = 999L; // Um ID que não está no banco H2
        String updateJson = """
                {
                    "tituloAtividade": "Título Fantasma"
                }
                """;

        // ACT
        mockMvc.perform(patch("/atividades-arquivo/{id}", idQueNaoExiste)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                // ASSERT (HTTP)
                .andExpect(status().isNotFound()); // Esperamos 404
    }

    @Test
    void testDelete_NotFound_ReturnsNotFound() throws Exception {
        // ARRANGE
        Long idQueNaoExiste = 999L; // Um ID que não está no banco H2

        // Verifica se ele realmente não existe
        assertFalse(atividadeArquivosRepository.findById(idQueNaoExiste).isPresent());

        // ACT
        mockMvc.perform(delete("/atividades-arquivo/{id}", idQueNaoExiste))
                // ASSERT (HTTP)
                .andExpect(status().isNotFound()); // Esperamos 404

        // ASSERT (BANCO DE DADOS)
        // O número de itens no banco não deve mudar (continuar 1 do setUp)
        assertTrue(atividadeArquivosRepository.count() == 1);
    }
}
