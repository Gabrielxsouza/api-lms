package br.ifsp.lms_api.controller.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.model.AtividadeArquivos;
import br.ifsp.lms_api.model.Curso;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.AtividadeArquivosRepository;
import br.ifsp.lms_api.repository.CursoRepository;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.ProfessorRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AtividadeArquivosControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired private AtividadeArquivosRepository atividadeArquivosRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private TopicosRepository topicosRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private DisciplinaRepository disciplinaRepository;

    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbcTemplate; 
    private Professor professorDono;
    private Topicos topico;
    private CustomUserDetails userDetailsDono;
    private AtividadeArquivos atividadeExistente;

    @BeforeEach
    void setUp() {
       
        try {
            jdbcTemplate.execute("DELETE FROM tentativa_texto");
            jdbcTemplate.execute("DELETE FROM tentativa_questionario");
            jdbcTemplate.execute("DELETE FROM tentativa_arquivo");
         
        } catch (Exception e) {
           
        }

        atividadeArquivosRepository.deleteAll();
        topicosRepository.deleteAll();
        turmaRepository.deleteAll();
        // -----------------------

        Curso curso = new Curso();
        curso.setNomeCurso("Curso Integration Test");
        curso.setCodigoCurso("INT-001-TEST");
        curso.setDescricaoCurso("Teste Int");
        curso = cursoRepository.save(curso);

        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina("Disc Integration Test");
        disciplina.setCodigoDisciplina("INT-DISC-TEST");
        disciplina.setDescricaoDisciplina("Desc");
        disciplina = disciplinaRepository.save(disciplina);

        professorDono = new Professor();
        professorDono.setNome("Prof. Integration Unique");
        professorDono.setEmail("prof.unique.integration" + System.currentTimeMillis() + "@teste.com");
        professorDono.setSenha("senhaForte123");
        professorDono.setCpf("999.888.777-66");
        professorDono.setDepartamento("Testes");
        professorDono = professorRepository.save(professorDono);

        entityManager.flush();
        entityManager.refresh(professorDono);

        Turma turma = new Turma();
        turma.setNomeTurma("Turma Integration");
        turma.setSemestre("2025/2");
        turma.setProfessor(professorDono);
        turma.setCurso(curso);
        turma.setDisciplina(disciplina);
        turma = turmaRepository.save(turma);

        topico = new Topicos();
        topico.setTituloTopico("Tópico Integration");
        topico.setTurma(turma);
        topico = topicosRepository.save(topico);

        userDetailsDono = new CustomUserDetails(professorDono);

        atividadeExistente = new AtividadeArquivos();
        atividadeExistente.setTituloAtividade("Atividade Base");
        atividadeExistente.setDescricaoAtividade("Desc");
        atividadeExistente.setDataInicioAtividade(LocalDate.now());
        atividadeExistente.setDataFechamentoAtividade(LocalDate.now().plusDays(7));
        atividadeExistente.setStatusAtividade(true);
        atividadeExistente.setArquivosPermitidos(List.of(".pdf"));
        atividadeExistente.setTopico(topico);
        atividadeExistente = atividadeArquivosRepository.save(atividadeExistente);
    }

    @Test
    void testCreateAtividade_Success() throws Exception {
        AtividadeArquivosRequestDto requestDto = new AtividadeArquivosRequestDto();
        requestDto.setTituloAtividade("Nova Atividade");
        requestDto.setDescricaoAtividade("Teste");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(5));
        requestDto.setStatusAtividade(true);
        requestDto.setArquivosPermitidos(List.of(".zip"));
        requestDto.setIdTopico(topico.getIdTopico());

        mockMvc.perform(post("/atividades-arquivo")
                .with(user(userDetailsDono))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAtividade").exists())
                .andExpect(jsonPath("$.tituloAtividade", is("Nova Atividade")));

        assertEquals(2, atividadeArquivosRepository.count());
    }

    @Test
    void testCreateAtividade_Forbidden_WrongProfessor() throws Exception {
        Professor outroProf = new Professor();
        outroProf.setNome("Invasor");
        outroProf.setEmail("invasor.unique" + System.currentTimeMillis() + "@test.com");
        outroProf.setSenha("senhaForte123");
        outroProf.setCpf("555.444.333-22");
        outroProf = professorRepository.save(outroProf);

        entityManager.flush();
        entityManager.refresh(outroProf);

        CustomUserDetails userDetailsOutro = new CustomUserDetails(outroProf);

        AtividadeArquivosRequestDto requestDto = new AtividadeArquivosRequestDto();
        requestDto.setTituloAtividade("Tentativa de Hack");
        requestDto.setDescricaoAtividade("Desc");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now());
        requestDto.setStatusAtividade(true);
        requestDto.setArquivosPermitidos(List.of(".pdf"));
        requestDto.setIdTopico(topico.getIdTopico());

        mockMvc.perform(post("/atividades-arquivo")
                .with(user(userDetailsOutro))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAll_Success() throws Exception {
        mockMvc.perform(get("/atividades-arquivo")
                .with(user(userDetailsDono))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].tituloAtividade", is("Atividade Base")));
    }

    @Test
    void testUpdateAtividade_Success() throws Exception {
   
        String jsonUpdate = "{\n" +
                "    \"tituloAtividade\": \"Atividade Atualizada\",\n" +
                "    \"arquivosPermitidos\": [\".docx\"]\n" +
                "}";

        mockMvc.perform(patch("/atividades-arquivo/{id}", atividadeExistente.getIdAtividade())
                .with(user(userDetailsDono))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloAtividade", is("Atividade Atualizada")));

        AtividadeArquivos atualizada = atividadeArquivosRepository.findById(atividadeExistente.getIdAtividade()).get();
        assertEquals("Atividade Atualizada", atualizada.getTituloAtividade());
    }

    @Test
    void testDeleteAtividade_Success() throws Exception {
        mockMvc.perform(delete("/atividades-arquivo/{id}", atividadeExistente.getIdAtividade())
                .with(user(userDetailsDono)))
                .andExpect(status().isNoContent());

        assertFalse(atividadeArquivosRepository.existsById(atividadeExistente.getIdAtividade()));
    }
}
