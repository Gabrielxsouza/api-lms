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

import br.ifsp.lms_api.dto.TurmaDto.TurmaRequestDto;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TurmaControllerIntegrationTest {

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
        disciplina.setNomeDisciplina("Disciplina Pai");
        disciplina.setCodigoDisciplina("PAI-101");
        disciplina.setDescricaoDisciplina("Base");
        disciplinaExistente = disciplinaRepository.save(disciplina);

        Turma turma = new Turma();
        turma.setNomeTurma("Turma Base");
        turma.setSemestre("2025/1");
        turma.setDisciplina(disciplinaExistente);
        turmaExistente = turmaRepository.save(turma);
        
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testCreateTurma_Success() throws Exception {
        TurmaRequestDto requestDto = new TurmaRequestDto(
            "Turma Nova Avulsa",
            "2025/2",
            disciplinaExistente.getIdDisciplina()
        );

        mockMvc.perform(post("/turmas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTurma").exists())
                .andExpect(jsonPath("$.nomeTurma", is("Turma Nova Avulsa")));

        assertEquals(2, turmaRepository.count());
    }

    @Test
    void testCreateTurma_DisciplinaNotFound() throws Exception {
        TurmaRequestDto requestDto = new TurmaRequestDto(
            "Turma Fantasma",
            "2025/2",
            999L 
        );

        mockMvc.perform(post("/turmas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound()); 

        assertEquals(1, turmaRepository.count()); 
    }
    
    @Test
    void testGetAllTurmas_Success() throws Exception {
        mockMvc.perform(get("/turmas")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nomeTurma", is("Turma Base")));
    }

    @Test
    void testUpdateTurma_Success() throws Exception {
        Long id = turmaExistente.getIdTurma();
        String updateJson = """
        {
            "nomeTurma": "Turma Base ATUALIZADA"
        }
        """;

        mockMvc.perform(patch("/turmas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTurma", is(id.intValue())))
                .andExpect(jsonPath("$.nomeTurma", is("Turma Base ATUALIZADA")));
    }

    @Test
    void testDeleteTurma_Success() throws Exception {
        Long id = turmaExistente.getIdTurma();
        
        mockMvc.perform(delete("/turmas/{id}", id))
                .andExpect(status().isNoContent());

        assertFalse(turmaRepository.findById(id).isPresent());
        assertEquals(0, turmaRepository.count());
        assertEquals(1, disciplinaRepository.count()); 
    }
}
