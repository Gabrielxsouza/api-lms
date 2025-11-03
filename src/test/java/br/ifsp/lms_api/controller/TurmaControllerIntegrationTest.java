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

        // Precisamos de uma disciplina PAI para vincular a turma
        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina("Disciplina Pai");
        disciplina.setCodigoDisciplina("PAI-101");
        disciplina.setDescricaoDisciplina("Base");
        disciplinaExistente = disciplinaRepository.save(disciplina);

        // Criamos uma turma base
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
        // Arrange
        // DTO para criar uma turma avulsa, vinculando ao ID da disciplina pai
        TurmaRequestDto requestDto = new TurmaRequestDto(
            "Turma Nova Avulsa",
            "2025/2",
            disciplinaExistente.getIdDisciplina()
        );

        // Act & Assert
        mockMvc.perform(post("/turmas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTurma").exists())
                .andExpect(jsonPath("$.nomeTurma", is("Turma Nova Avulsa")));

        // Assert (Banco de Dados)
        // Devemos ter 2 turmas (Base + Nova)
        assertEquals(2, turmaRepository.count());
    }

    @Test
    void testCreateTurma_DisciplinaNotFound() throws Exception {
        // Arrange
        TurmaRequestDto requestDto = new TurmaRequestDto(
            "Turma Fantasma",
            "2025/2",
            999L // ID de disciplina que não existe
        );

        // Act & Assert
        mockMvc.perform(post("/turmas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound()); // Service deve retornar 404

        // Assert (Banco de Dados)
        assertEquals(1, turmaRepository.count()); // Nenhuma turma nova foi criada
    }
    
    @Test
    void testGetAllTurmas_Success() throws Exception {
        // Act & Assert
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
        // Arrange
        Long id = turmaExistente.getIdTurma();
        String updateJson = """
        {
            "nomeTurma": "Turma Base ATUALIZADA"
        }
        """;

        // Act & Assert
        mockMvc.perform(patch("/turmas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTurma", is(id.intValue())))
                .andExpect(jsonPath("$.nomeTurma", is("Turma Base ATUALIZADA")));
    }

    @Test
    void testDeleteTurma_Success() throws Exception {
        // Arrange
        Long id = turmaExistente.getIdTurma();
        
        // Act
        mockMvc.perform(delete("/turmas/{id}", id))
                .andExpect(status().isNoContent());

        // Assert (Banco de Dados)
        assertFalse(turmaRepository.findById(id).isPresent());
        assertEquals(0, turmaRepository.count());
        assertEquals(1, disciplinaRepository.count()); // A disciplina PAI não deve ser deletada
    }
}