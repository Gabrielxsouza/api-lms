package br.ifsp.lms_api.controller.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.dto.TurmaDto.TurmaRequestDto;
import br.ifsp.lms_api.model.Curso;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.CursoRepository;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.ProfessorRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import br.ifsp.lms_api.service.AutentificacaoService;
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
    private CursoRepository cursoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @MockBean
    private AutentificacaoService autentificacaoService;

    @Autowired
    private EntityManager entityManager;

    private Disciplina disciplinaExistente;
    private Curso cursoExistente;
    private Professor professorExistente;
    private Turma turmaExistente;

    @BeforeEach
    void setUp() {
        turmaRepository.deleteAll();
        disciplinaRepository.deleteAll();
        cursoRepository.deleteAll();
        professorRepository.deleteAll();

        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina("Disciplina Pai");
        disciplina.setCodigoDisciplina("PAI-101");
        disciplina.setDescricaoDisciplina("Base");
        disciplinaExistente = disciplinaRepository.save(disciplina);

        Curso curso = new Curso();
        curso.setNomeCurso("Curso Pai");
        curso.setCodigoCurso("CPAI-101");
        curso.setDescricaoCurso("Base Curso");
        cursoExistente = cursoRepository.save(curso);
        
        Professor professor = new Professor();
        professor.setNome("Prof. Base");
        professor.setEmail("prof.base@test.com");
        professor.setCpf("12345678900");
        professor.setSenha("senha");
        professor.setDepartamento("COMP");
        professorExistente = professorRepository.save(professor);

        Turma turma = new Turma();
        turma.setNomeTurma("Turma Base");
        turma.setSemestre("2025/1");
        turma.setDisciplina(disciplinaExistente);
        turma.setCurso(cursoExistente);
        turma.setProfessor(professorExistente);
        turmaExistente = turmaRepository.save(turma);
        
        entityManager.flush();
        entityManager.clear();

        // Mock global para o professor logado (usado no GET /minhas-turmas)
        when(autentificacaoService.getUsuarioLogado()).thenReturn(professorExistente);
    }

    @Test
    void testCreateTurma_Success() throws Exception {
        // CORRIGIDO: Adicionado idProfessor
        TurmaRequestDto requestDto = new TurmaRequestDto(
            "Turma Nova Avulsa",
            "2025/2",
            disciplinaExistente.getIdDisciplina(),
            cursoExistente.getIdCurso(),
            professorExistente.getIdUsuario() // <-- 5º ARGUMENTO
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
        // CORRIGIDO: Adicionado idProfessor
        TurmaRequestDto requestDto = new TurmaRequestDto(
            "Turma Fantasma",
            "2025/2",
            999L, // ID da Disciplina (não existe)
            cursoExistente.getIdCurso(),
            professorExistente.getIdUsuario() // <-- 5º ARGUMENTO
        );

        mockMvc.perform(post("/turmas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound()); 

        assertEquals(1, turmaRepository.count()); 
    }
    
    @Test
    void testGetAllTurmas_Success() throws Exception {
        // Este teste simula um ADMIN vendo todas as turmas
        mockMvc.perform(get("/turmas")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nomeTurma", is("Turma Base")));
    }

    // --- TESTE ADICIONADO ---
    @Test
    void testGetMinhasTurmas_Success() throws Exception {
        // Este teste simula o Professor logado vendo apenas suas turmas
        mockMvc.perform(get("/turmas/minhas-turmas")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1))) // Só deve achar 1 turma
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nomeTurma", is("Turma Base")));
    }
    // --- FIM DA ADIÇÃO ---

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