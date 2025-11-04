package br.ifsp.lms_api.controller.integration;

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
@Transactional 
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
        turmaRepository.deleteAll();
        disciplinaRepository.deleteAll();

        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina("Disciplina Base");
        disciplina.setCodigoDisciplina("BASE-101");
        disciplina.setDescricaoDisciplina("Base");

        Turma turma = new Turma();
        turma.setNomeTurma("Turma Base");
        turma.setSemestre("2025/1");
        
        disciplina.setTurmas(List.of(turma));
        turma.setDisciplina(disciplina);

        disciplinaExistente = disciplinaRepository.save(disciplina);
        
        entityManager.flush();
        entityManager.clear();
        
        disciplinaExistente = disciplinaRepository.findAll().get(0);
        turmaExistente = turmaRepository.findAll().get(0);
    }

    @Test
    void testCreateDisciplina_Success_WithNestedTurmas() throws Exception {
        TurmaParaDisciplinaDTO novaTurmaDto = new TurmaParaDisciplinaDTO("Turma Aninhada", "2025/2");
        DisciplinaRequestDto requestDto = new DisciplinaRequestDto(
            "Engenharia de Software",
            "Testes",
            "ESL708",
            List.of(novaTurmaDto)
        );

        mockMvc.perform(post("/disciplinas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDisciplina").exists())
                .andExpect(jsonPath("$.nomeDisciplina", is("Engenharia de Software")))
                .andExpect(jsonPath("$.turmas", hasSize(1)))
                .andExpect(jsonPath("$.turmas[0].nomeTurma", is("Turma Aninhada")));

        assertEquals(2, disciplinaRepository.count());
        assertEquals(2, turmaRepository.count());
    }

    @Test
    void testGetAllDisciplinas_Success() throws Exception {
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
        Long id = disciplinaExistente.getIdDisciplina();
        String updateJson = """
        {
            "nomeDisciplina": "Disciplina Base ATUALIZADA"
        }
        """;

        mockMvc.perform(patch("/disciplinas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDisciplina", is(id.intValue())))
                .andExpect(jsonPath("$.nomeDisciplina", is("Disciplina Base ATUALIZADA")));
    }

    @Test
    void testDeleteDisciplina_Success_WithCascade() throws Exception {
        Long disciplinaId = disciplinaExistente.getIdDisciplina();
        Long turmaId = turmaExistente.getIdTurma();
        
        assertEquals(1, disciplinaRepository.count());
        assertEquals(1, turmaRepository.count());

        mockMvc.perform(delete("/disciplinas/{id}", disciplinaId))
                .andExpect(status().isNoContent());

        assertFalse(disciplinaRepository.findById(disciplinaId).isPresent());
        assertFalse(turmaRepository.findById(turmaId).isPresent(), "A turma não foi deletada em cascata!");
    }
}
