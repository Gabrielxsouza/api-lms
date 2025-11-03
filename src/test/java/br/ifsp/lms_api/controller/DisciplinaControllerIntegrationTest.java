package br.ifsp.lms_api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaRequestDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaParaDisciplinaDTO;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import jakarta.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rola para trás as transações do banco após cada teste
public class DisciplinaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private TurmaRepository turmaRepository;
    
    @Autowired
    private EntityManager entityManager;

    private Disciplina disciplinaExistente;
    private Turma turmaExistente;

    @BeforeEach
    void setUp() {
        // Limpa o banco antes de cada teste
        turmaRepository.deleteAll();
        disciplinaRepository.deleteAll();

        // Cria uma disciplina e turma "base"
        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina("Disciplina Base");
        disciplina.setCodigoDisciplina("BASE-101");
        disciplina.setDescricaoDisciplina("Base");

        Turma turma = new Turma();
        turma.setNomeTurma("Turma Base");
        turma.setSemestre("2025/1");
        
        // Seta o relacionamento bidirecional
        disciplina.setTurmas(List.of(turma));
        turma.setDisciplina(disciplina);

        // Salva (o cascade deve salvar a turma junto)
        disciplinaExistente = disciplinaRepository.save(disciplina);
        
        entityManager.flush();
        entityManager.clear();
        
        // Recarrega do banco para garantir que os IDs estão corretos
        disciplinaExistente = disciplinaRepository.findAll().get(0);
        turmaExistente = turmaRepository.findAll().get(0);
    }

    @Test
    void testCreateDisciplina_Success_WithNestedTurmas() throws Exception {
        // Arrange
        TurmaParaDisciplinaDTO novaTurmaDto = new TurmaParaDisciplinaDTO("Turma Aninhada", "2025/2");
        DisciplinaRequestDto requestDto = new DisciplinaRequestDto(
            "Engenharia de Software",
            "Testes",
            "ESL708",
            List.of(novaTurmaDto)
        );

        // Act & Assert
        mockMvc.perform(post("/disciplinas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDisciplina").exists())
                .andExpect(jsonPath("$.nomeDisciplina", is("Engenharia de Software")))
                .andExpect(jsonPath("$.turmas", hasSize(1)))
                .andExpect(jsonPath("$.turmas[0].nomeTurma", is("Turma Aninhada")));

        // Assert (Banco de Dados)
        // Devemos ter 2 disciplinas (Base + Nova) e 2 turmas (Base + Nova)
        assertEquals(2, disciplinaRepository.count());
        assertEquals(2, turmaRepository.count());
    }

    @Test
    void testGetAllDisciplinas_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/disciplinas")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nomeDisciplina", is("Disciplina Base")));
    }

    @Test
    void testUpdateDisciplina_Success() throws Exception {
        // Arrange
        Long id = disciplinaExistente.getIdDisciplina();
        String updateJson = """
        {
            "nomeDisciplina": "Disciplina Base ATUALIZADA"
        }
        """;

        // Act & Assert
        mockMvc.perform(patch("/disciplinas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDisciplina", is(id.intValue())))
                .andExpect(jsonPath("$.nomeDisciplina", is("Disciplina Base ATUALIZADA")));
    }

    @Test
    void testDeleteDisciplina_Success_WithCascade() throws Exception {
        // Arrange
        Long disciplinaId = disciplinaExistente.getIdDisciplina();
        Long turmaId = turmaExistente.getIdTurma();
        
        // Garante que ambos existem antes do teste
        assertEquals(1, disciplinaRepository.count());
        assertEquals(1, turmaRepository.count());

        // Act
        mockMvc.perform(delete("/disciplinas/{id}", disciplinaId))
                .andExpect(status().isNoContent());

        // Assert (Banco de Dados)
        // O `orphanRemoval` deve ter deletado a turma junto
        assertFalse(disciplinaRepository.findById(disciplinaId).isPresent());
        assertFalse(turmaRepository.findById(turmaId).isPresent(), "A turma não foi deletada em cascata!");
    }
}