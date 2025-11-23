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
import org.springframework.jdbc.core.JdbcTemplate;
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
@Transactional
class AlternativasControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlternativasRepository alternativasRepository;

    @Autowired
    private QuestoesRepository questoesRepository;

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Questoes questaoSalva;

    @BeforeEach
    void setUp() {

        try {
            jdbcTemplate.execute("DELETE FROM tentativa_texto");
            jdbcTemplate.execute("DELETE FROM tentativa_questionario");
        } catch (Exception e) {

        }

        alternativasRepository.deleteAll();
        questoesRepository.deleteAll();

        if(atividadeRepository != null) {
             atividadeRepository.deleteAll();
        }

        Questoes questao = new Questoes();
        questao.setEnunciado("Questão Teste Integração");

        Alternativas dummyAlt = new Alternativas();
        dummyAlt.setAlternativa("Dummy Init");
        dummyAlt.setAlternativaCorreta(false);

        dummyAlt.setQuestoes(questao);
        questao.setAlternativas(List.of(dummyAlt));

        this.questaoSalva = questoesRepository.save(questao);
    }

    @Test
    @DisplayName("Deve criar uma alternativa vinculada a uma questão existente")
    @WithMockUser(roles = "PROFESSOR")
    void shouldCreateAlternativaSuccessfully() throws Exception {

        AlternativasRequestDto requestDto = new AlternativasRequestDto();
        requestDto.setIdQuestao(questaoSalva.getIdQuestao());
        requestDto.setAlternativa("Alternativa A");
        requestDto.setAlternativaCorreta(true);

        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alternativa").value("Alternativa A"));

        assertEquals(2, alternativasRepository.count());

        Alternativas salva = alternativasRepository.findAll().stream()
                .filter(a -> a.getAlternativa().equals("Alternativa A"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Alternativa A não foi salva no banco"));

        assertEquals("Alternativa A", salva.getAlternativa());
        assertEquals(questaoSalva.getIdQuestao(), salva.getQuestoes().getIdQuestao());
    }

    @Test
    @DisplayName("Deve listar alternativas paginadas")
    @WithMockUser(roles = "PROFESSOR")
    void shouldGetAllAlternativas() throws Exception {

        createAndSaveAlternativa("Alt 1", false);
        createAndSaveAlternativa("Alt 2", true);

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

        Alternativas original = createAndSaveAlternativa("Texto Original", false);

        AlternativasUpdateDto updateDto = new AlternativasUpdateDto();
        updateDto.setAlternativa(Optional.of("Texto Atualizado"));
        updateDto.setAlternativaCorreta(Optional.of(true));

        mockMvc.perform(patch("/alternativas/{id}", original.getIdAlternativa())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alternativa").value("Texto Atualizado"));

        Alternativas atualizada = alternativasRepository.findById(original.getIdAlternativa()).orElseThrow();
        assertEquals("Texto Atualizado", atualizada.getAlternativa());
        assertTrue(atualizada.getAlternativaCorreta());
    }

    @Test
    @DisplayName("Deve deletar uma alternativa (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteAlternativaSuccessfully() throws Exception {

        Alternativas alt = createAndSaveAlternativa("Para Deletar", false);

        mockMvc.perform(delete("/alternativas/{id}", alt.getIdAlternativa()))
                .andExpect(status().isNoContent());

        assertTrue(alternativasRepository.findById(alt.getIdAlternativa()).isEmpty());
    }


    @Test
    @DisplayName("POST - Deve retornar 404 ao tentar criar alternativa para questão inexistente")
    @WithMockUser(roles = "PROFESSOR")
    void shouldReturnNotFoundWhenCreatingForNonExistentQuestao() throws Exception {

        Long idQuestaoInexistente = 9999L;
        AlternativasRequestDto requestDto = new AlternativasRequestDto();
        requestDto.setIdQuestao(idQuestaoInexistente);
        requestDto.setAlternativa("Teste");
        requestDto.setAlternativaCorreta(false);

        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        assertEquals(1, alternativasRepository.count());
    }

    @Test
    @DisplayName("POST - Deve retornar 400 se faltar campos obrigatórios")
    @WithMockUser(roles = "PROFESSOR")
    void shouldReturnBadRequestForInvalidInput() throws Exception {

        AlternativasRequestDto invalidDto = new AlternativasRequestDto();

        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH - Deve retornar 403 se PROFESSOR tentar atualizar (Rota Admin)")
    @WithMockUser(roles = "PROFESSOR")
    void shouldDenyUpdateAccessToProfessor() throws Exception {

        Alternativas alt = createAndSaveAlternativa("Original", false);
        AlternativasUpdateDto updateDto = new AlternativasUpdateDto();
        updateDto.setAlternativa(Optional.of("Hacker"));


        mockMvc.perform(patch("/alternativas/{id}", alt.getIdAlternativa())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());

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

    private Alternativas createAndSaveAlternativa(String texto, boolean correta) {
        Alternativas alt = new Alternativas();
        alt.setAlternativa(texto);
        alt.setAlternativaCorreta(correta);
        alt.setQuestoes(questaoSalva);
        return alternativasRepository.save(alt);
    }
}
