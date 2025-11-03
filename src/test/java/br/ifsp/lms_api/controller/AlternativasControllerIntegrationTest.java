package br.ifsp.lms_api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

// Importe suas Entidades e Repositórios REAIS
import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.Questoes; // Você precisará desta entidade
import br.ifsp.lms_api.repository.AlternativasRepository;
import br.ifsp.lms_api.repository.QuestoesRepository; // Você precisará deste repo
import jakarta.persistence.EntityManager;
// Importe seu DTO de request
import br.ifsp.lms_api.dto.alternativasDto.AlternativasRequestDto;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rollback no banco de dados após cada teste
public class AlternativasControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    // Injetamos os repositórios REAIS
    @Autowired
    private AlternativasRepository alternativasRepository;

    @Autowired
    private QuestoesRepository questoesRepository; // Essencial por causa da relação

    private Questoes questaoExistente;
    private Alternativas alternativaExistente;

    @BeforeEach
    void setUp() {
        alternativasRepository.deleteAll();
        questoesRepository.deleteAll();

        // 1. Cria Questao e Alternativa em memória
        Questoes questao = new Questoes();
        questao.setEnunciado("Quanto é 2 + 2?");

        Alternativas alternativa = new Alternativas();
        alternativa.setAlternativa("A resposta é 4.");
        alternativa.setAlternativaCorreta(true);

        // 2. Liga as duas (bi-direcional)
        alternativa.setQuestoes(questao);
        questao.getAlternativas().add(alternativa);

        // 3. Salva o PAI (o cascade salva o filho)
        questoesRepository.save(questao);

        // 4. FORÇA o Hibernate a escrever no banco E limpar a cache
        entityManager.flush();
        entityManager.clear();

        // 5. AGORA, busca os dados "limpos" do banco para usar nos testes
        alternativaExistente = alternativasRepository.findAll().get(0);
        questaoExistente = questoesRepository.findAll().get(0);
    }

    @Test
    void testCreate_Success() throws Exception {
        // ARRANGE (Preparar DTO)
        AlternativasRequestDto requestDto = new AlternativasRequestDto();
        requestDto.setAlternativa("A resposta é 5.");
        requestDto.setAlternativaCorreta(false);
        // Assumindo que o DTO agora tem o 'idQuestao' (conforme explicado acima)
        requestDto.setIdQuestao(questaoExistente.getIdQuestao());

        // ACT (Executar)
        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                // ASSERT (HTTP)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAlternativa").exists())
                .andExpect(jsonPath("$.alternativa", is("A resposta é 5.")))
                .andExpect(jsonPath("$.alternativaCorreta", is(false)));

        // ASSERT (BANCO DE DADOS)
        // Havia 1 (do setUp), agora deve haver 2
        assertTrue(alternativasRepository.count() == 2);
    }

    @Test
    void testCreate_InvalidInput_ReturnsBadRequest() throws Exception {
        // ARRANGE (DTO inválido, viola @Size(min=5))
        AlternativasRequestDto requestDto = new AlternativasRequestDto();
        requestDto.setAlternativa("Ops"); // Inválido
        requestDto.setAlternativaCorreta(false);
        requestDto.setIdQuestao(questaoExistente.getIdQuestao());

        // ACT
        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                // ASSERT (HTTP)
                .andExpect(status().isBadRequest());

        // ASSERT (BANCO DE DADOS)
        // Não deve ter criado, continua 1
        assertTrue(alternativasRepository.count() == 1);
    }

    @Test
    void testGetAll_Success() throws Exception {
        // ARRANGE (Feito no setUp)

        // ACT & ASSERT
        mockMvc.perform(get("/alternativas")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(
                        jsonPath("$.content[0].idAlternativa", is(alternativaExistente.getIdAlternativa().intValue())));
    }

    @Test
    void testGetById_Success() throws Exception {
        // ARRANGE
        Long id = alternativaExistente.getIdAlternativa();

        // ACT & ASSERT
        mockMvc.perform(get("/alternativas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlternativa", is(id.intValue())))
                .andExpect(jsonPath("$.alternativa", is("A resposta é 4.")));
    }

    @Test
    void testGetById_NotFound() throws Exception {
        // ARRANGE
        Long idQueNaoExiste = 999L;

        // ACT & ASSERT
        mockMvc.perform(get("/alternativas/{id}", idQueNaoExiste))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdate_Success() throws Exception {
        // ARRANGE
        Long id = alternativaExistente.getIdAlternativa();
        // JSON puro para o DTO de update
        String updateJson = """
                {
                    "alternativa": "Alternativa Atualizada",
                    "alternativaCorreta": false
                }
                """;

        // ACT
        mockMvc.perform(patch("/alternativas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                // ASSERT (HTTP)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlternativa", is(id.intValue())))
                .andExpect(jsonPath("$.alternativa", is("Alternativa Atualizada")))
                .andExpect(jsonPath("$.alternativaCorreta", is(false)));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        // ARRANGE
        Long idQueNaoExiste = 999L;
        String updateJson = """
                {
                    "alternativa": "Alternativa Fantasma"
                }
                """;

        // ACT
        mockMvc.perform(patch("/alternativas/{id}", idQueNaoExiste)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                // ASSERT (HTTP)
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete_Success() throws Exception {
        // ARRANGE
        assertNotNull(alternativaExistente, "Falha no setUp: alternativaExistente é nula");
        Long id = alternativaExistente.getIdAlternativa();
        assertNotNull(id, "Falha no setUp: ID da alternativa é nulo");

        // Confirma que o item REALMENTE está no banco antes de deletar
        assertTrue(alternativasRepository.findById(id).isPresent(),
                "Alternativa não foi encontrada no banco ANTES do delete");

        // ACT
        mockMvc.perform(delete("/alternativas/{id}", id))
                // ASSERT (HTTP)
                .andExpect(status().isNoContent());

        // ASSERT (BANCO DE DADOS)
        // Confirma que o item REALMENTE foi deletado
        assertFalse(alternativasRepository.findById(id).isPresent(),
                "Alternativa AINDA foi encontrada no banco APÓS o delete");
    }

    @Test
    void testDelete_NotFound() throws Exception {
        // ARRANGE
        Long idQueNaoExiste = 999L;

        // ACT
        mockMvc.perform(delete("/alternativas/{id}", idQueNaoExiste))
                // ASSERT (HTTP)
                .andExpect(status().isNotFound());
    }
}
