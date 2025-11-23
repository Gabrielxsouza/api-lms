package br.ifsp.lms_api.controller.integration;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.dto.DisciplinaDto.DisciplinaRequestDto;
import br.ifsp.lms_api.dto.TurmaDto.TurmaParaDisciplinaDTO;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import br.ifsp.lms_api.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;

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
    private UsuarioRepository usuarioRepository; 
    
    @Autowired
    private EntityManager entityManager;

    private Disciplina disciplinaExistente;
    private Turma turmaExistente;
    
  

    @BeforeEach
    void setUp() {
      
        entityManager.createNativeQuery("DELETE FROM atividade_arquivos_permitidos").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM atividade_tags").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM topico_tags").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM questao_tags").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM questionario_questoes").executeUpdate();

        entityManager.createQuery("DELETE FROM TentativaTexto").executeUpdate();
        entityManager.createQuery("DELETE FROM TentativaQuestionario").executeUpdate();
        entityManager.createQuery("DELETE FROM TentativaArquivo").executeUpdate(); 
        
        entityManager.createQuery("DELETE FROM MaterialDeAula").executeUpdate();
        entityManager.createQuery("DELETE FROM Alternativas").executeUpdate();
        entityManager.createQuery("DELETE FROM Questoes").executeUpdate();
        entityManager.createQuery("DELETE FROM Atividade").executeUpdate();

        entityManager.createQuery("DELETE FROM Topicos").executeUpdate();
        entityManager.createQuery("DELETE FROM Matricula").executeUpdate();

        turmaRepository.deleteAll();
        disciplinaRepository.deleteAll();

        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina("Disciplina Base");
        disciplina.setCodigoDisciplina("BASE-101");
        disciplina.setDescricaoDisciplina("Base");
        disciplina = disciplinaRepository.save(disciplina);

        Turma turma = new Turma();
        turma.setNomeTurma("Turma Base");
        turma.setSemestre("2025/1");
        turma.setDisciplina(disciplina);
        turma = turmaRepository.save(turma);

     
        disciplina.setTurmas(new ArrayList<>(List.of(turma))); 
        disciplinaExistente = disciplinaRepository.save(disciplina);
        
        entityManager.flush();
        entityManager.clear();
        
        disciplinaExistente = disciplinaRepository.findById(disciplina.getIdDisciplina()).get();
        turmaExistente = turmaRepository.findById(turma.getIdTurma()).get();
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
              
                .with(user("admin").password("pass").roles("ADMIN")) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDisciplina").exists())
                .andExpect(jsonPath("$.nomeDisciplina", is("Engenharia de Software")));
    }

    @Test
    void testGetAllDisciplinas_Success() throws Exception {
        mockMvc.perform(get("/disciplinas")
              
                .with(user("admin").roles("ADMIN"))
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
           
                .with(user("admin").roles("ADMIN"))
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
        
        mockMvc.perform(delete("/disciplinas/{id}", disciplinaId)
       
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        assertFalse(disciplinaRepository.findById(disciplinaId).isPresent());
        assertFalse(turmaRepository.findById(turmaId).isPresent(), "A turma não foi deletada em cascata!");
    }
}