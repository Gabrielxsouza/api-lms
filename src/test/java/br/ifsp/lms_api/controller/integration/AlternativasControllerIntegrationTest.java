package br.ifsp.lms_api.controller.integration;


import br.ifsp.lms_api.dto.alternativasDto.AlternativasRequestDto;
import br.ifsp.lms_api.dto.alternativasDto.AlternativasUpdateDto;
import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.repository.AlternativasRepository;
import br.ifsp.lms_api.repository.AtividadeRepository;
import br.ifsp.lms_api.repository.QuestoesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Garante rollback após cada teste
class AlternativasControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlternativasRepository alternativasRepository;

    @Autowired
    private QuestoesRepository questoesRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Questoes questaoSalva;

    @Autowired
    private AtividadeRepository atividadeRepository; // Add this (or QuestionarioRepository)

    @BeforeEach
    void setUp() {
        // CLEANUP ORDER IS CRITICAL TO AVOID CONSTRAINT VIOLATIONS

        // 1. Clear Children first
        alternativasRepository.deleteAll();

        // 2. Clear Relations (This is likely what was missing)
        // If you have a repo for 'Questionario', delete all from there too.
        // Assuming 'Atividade' is the parent of Questionnaires which hold Questions
        if(atividadeRepository != null) {
             atividadeRepository.deleteAll();
        }

        // 3. Clear Parents
        questoesRepository.deleteAll();

        // 1. Create the Question
        Questoes questao = new Questoes();
        questao.setEnunciado("Questão Teste Integração");

        // 2. Create a Dummy Alternative to satisfy the validation "min=1"
        Alternativas dummyAlt = new Alternativas();
        dummyAlt.setAlternativa("Dummy Init");
        dummyAlt.setAlternativaCorreta(false);

        // 3. Bi-directional Link (VERY IMPORTANT)
        dummyAlt.setQuestoes(questao); // Link Child -> Parent
        questao.setAlternativas(List.of(dummyAlt)); // Link Parent -> Child

        // 4. Save Parent (Cascade will save the Child automatically)
        this.questaoSalva = questoesRepository.save(questao);
    }

    // ==================================================================================
    // HAPPY PATHS (Caminhos Felizes)
    // ==================================================================================

    @Test
    @DisplayName("Deve criar uma alternativa vinculada a uma questão existente")
    @WithMockUser(roles = "PROFESSOR")
    void shouldCreateAlternativaSuccessfully() throws Exception {
        // ARRANGE
        AlternativasRequestDto requestDto = new AlternativasRequestDto();
        requestDto.setIdQuestao(questaoSalva.getIdQuestao());
        requestDto.setAlternativa("Alternativa A");
        requestDto.setAlternativaCorreta(true);

        // ACT
        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alternativa").value("Alternativa A"));

        // ASSERT (Banco de Dados)
        // Esperamos 2: A Dummy do setUp + A nova "Alternativa A"
        assertEquals(2, alternativasRepository.count());

        // CORREÇÃO: Busca especificamente a alternativa que criamos, ignorando a Dummy
        Alternativas salva = alternativasRepository.findAll().stream()
                .filter(a -> a.getAlternativa().equals("Alternativa A")) // Filtra pelo nome
                .findFirst()
                .orElseThrow(() -> new AssertionError("Alternativa A não foi salva no banco"));

        assertEquals("Alternativa A", salva.getAlternativa());
        assertEquals(questaoSalva.getIdQuestao(), salva.getQuestoes().getIdQuestao());
    }

    @Test
    @DisplayName("Deve listar alternativas paginadas")
    @WithMockUser(roles = "PROFESSOR")
    void shouldGetAllAlternativas() throws Exception {
        // ARRANGE
        createAndSaveAlternativa("Alt 1", false);
        createAndSaveAlternativa("Alt 2", true);

        // ACT & ASSERT
        mockMvc.perform(get("/alternativas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("Deve atualizar uma alternativa (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAlternativaSuccessfully() throws Exception {
        // ARRANGE
        Alternativas original = createAndSaveAlternativa("Texto Original", false);

        AlternativasUpdateDto updateDto = new AlternativasUpdateDto();
        updateDto.setAlternativa(Optional.of("Texto Atualizado"));
        updateDto.setAlternativaCorreta(Optional.of(true));

        // ACT
        mockMvc.perform(patch("/alternativas/{id}", original.getIdAlternativa())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alternativa").value("Texto Atualizado"));

        // ASSERT (Banco)
        Alternativas atualizada = alternativasRepository.findById(original.getIdAlternativa()).orElseThrow();
        assertEquals("Texto Atualizado", atualizada.getAlternativa());
        assertTrue(atualizada.getAlternativaCorreta());
    }

    @Test
    @DisplayName("Deve deletar uma alternativa (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteAlternativaSuccessfully() throws Exception {
        // ARRANGE
        Alternativas alt = createAndSaveAlternativa("Para Deletar", false);

        // ACT
        mockMvc.perform(delete("/alternativas/{id}", alt.getIdAlternativa()))
                .andExpect(status().isNoContent());

        // ASSERT
        assertTrue(alternativasRepository.findById(alt.getIdAlternativa()).isEmpty());
    }

    // ==================================================================================
    // SAD PATHS (Caminhos Tristes)
    // ==================================================================================

    @Test
    @DisplayName("POST - Deve retornar 404 ao tentar criar alternativa para questão inexistente")
    @WithMockUser(roles = "PROFESSOR")
    void shouldReturnNotFoundWhenCreatingForNonExistentQuestao() throws Exception {
        // ARRANGE
        Long idQuestaoInexistente = 9999L;
        AlternativasRequestDto requestDto = new AlternativasRequestDto();
        requestDto.setIdQuestao(idQuestaoInexistente);
        requestDto.setAlternativa("Teste");
        requestDto.setAlternativaCorreta(false);

        // ACT & ASSERT
        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound()); // Spring converte ResourceNotFoundException para 404

        assertEquals(1, alternativasRepository.count());
    }

    @Test
    @DisplayName("POST - Deve retornar 400 se faltar campos obrigatórios")
    @WithMockUser(roles = "PROFESSOR")
    void shouldReturnBadRequestForInvalidInput() throws Exception {
        // ARRANGE
        AlternativasRequestDto invalidDto = new AlternativasRequestDto(); // Vazio

        // ACT & ASSERT
        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH - Deve retornar 403 se PROFESSOR tentar atualizar (Rota Admin)")
    @WithMockUser(roles = "PROFESSOR")
    void shouldDenyUpdateAccessToProfessor() throws Exception {
        // ARRANGE
        Alternativas alt = createAndSaveAlternativa("Original", false);
        AlternativasUpdateDto updateDto = new AlternativasUpdateDto();
        updateDto.setAlternativa(Optional.of("Hacker"));

        // ACT & ASSERT
        mockMvc.perform(patch("/alternativas/{id}", alt.getIdAlternativa())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());

        // Verify database wasn't changed
        Alternativas check = alternativasRepository.findById(alt.getIdAlternativa()).orElseThrow();
        assertEquals("Original", check.getAlternativa());
    }

    @Test
    @DisplayName("DELETE - Deve retornar 404 ao tentar deletar ID inexistente")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenDeletingNonExistentId() throws Exception {
        mockMvc.perform(delete("/alternativas/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    // --- Método Auxiliar para facilitar a criação nos testes ---
    private Alternativas createAndSaveAlternativa(String texto, boolean correta) {
        Alternativas alt = new Alternativas();
        alt.setAlternativa(texto);
        alt.setAlternativaCorreta(correta);
        alt.setQuestoes(questaoSalva); // Vincula à questão criada no setUp
        return alternativasRepository.save(alt);
    }
}
