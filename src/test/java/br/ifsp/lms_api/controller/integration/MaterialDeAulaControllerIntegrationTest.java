package br.ifsp.lms_api.controller.integration;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.NestedServletException;


import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.exception.AccessDeniedException;
import br.ifsp.lms_api.model.Curso;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.MaterialDeAula;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.CursoRepository;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.MaterialDeAulaRepository;
import br.ifsp.lms_api.repository.ProfessorRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import br.ifsp.lms_api.service.StorageService;
import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MaterialDeAulaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private MaterialDeAulaRepository materialRepository;
    @Autowired private TopicosRepository topicosRepository;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private EntityManager entityManager;

    @Autowired private JdbcTemplate jdbcTemplate; 

    @MockBean
    private StorageService storageService;

    private Professor professorDono;
    private Professor professorIntruso;
    private Topicos topico;
    private MaterialDeAula materialExistente;
    private CustomUserDetails userDetailsDono;
    private CustomUserDetails userDetailsIntruso;

    @BeforeEach
    void setUp() {
    
        try {
            jdbcTemplate.execute("DELETE FROM tentativa_texto");
            jdbcTemplate.execute("DELETE FROM tentativa_questionario");
            jdbcTemplate.execute("DELETE FROM tentativa_arquivo");

         
            jdbcTemplate.execute("DELETE FROM atividade_questoes");

            jdbcTemplate.execute("DELETE FROM atividade");
        } catch (Exception e) {
          
        }

        materialRepository.deleteAll();
        topicosRepository.deleteAll();
        turmaRepository.deleteAll();
 

        Curso curso = new Curso();
        curso.setNomeCurso("Curso Mat");
        curso.setCodigoCurso("MAT10-UNIQUE");
        curso.setDescricaoCurso("Desc");
        curso = cursoRepository.save(curso);

        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina("Disc Mat");
        disciplina.setCodigoDisciplina("MAT-UNIQUE");
        disciplina.setDescricaoDisciplina("Desc");
        disciplina = disciplinaRepository.save(disciplina);

        professorDono = new Professor();
        professorDono.setNome("Prof. Dono Material");
        professorDono.setEmail("dono.material.unique" + System.currentTimeMillis() + "@test.com");
        professorDono.setSenha("123456");
        professorDono.setCpf("111.222.999-88");
        professorDono.setDepartamento("Exatas");
        professorDono = professorRepository.save(professorDono);

        entityManager.flush();
        entityManager.refresh(professorDono);

        userDetailsDono = new CustomUserDetails(professorDono);

        professorIntruso = new Professor();
        professorIntruso.setNome("Prof. Intruso");
        professorIntruso.setEmail("intruso.material.unique" + System.currentTimeMillis() + "@test.com");
        professorIntruso.setSenha("123456");
        professorIntruso.setCpf("999.888.111-00");
        professorIntruso = professorRepository.save(professorIntruso);

        entityManager.flush();
        entityManager.refresh(professorIntruso);

        userDetailsIntruso = new CustomUserDetails(professorIntruso);

        Turma turma = new Turma();
        turma.setNomeTurma("Turma Material");
        turma.setSemestre("2025/2");
        turma.setProfessor(professorDono);
        turma.setCurso(curso);
        turma.setDisciplina(disciplina);
        turma = turmaRepository.save(turma);

        topico = new Topicos();
        topico.setTituloTopico("Tópico Material");
        topico.setTurma(turma);
        topico = topicosRepository.save(topico);

        materialExistente = new MaterialDeAula();
        materialExistente.setNomeArquivo("existente.pdf");
        materialExistente.setTipoArquivo("application/pdf");
        materialExistente.setUrlArquivo("http://fake-url.com/existente.pdf");
        materialExistente.setTopico(topico);
        materialExistente = materialRepository.save(materialExistente);

        when(storageService.createArquivo(any())).thenReturn("http://fake-s3-url.com/novo.pdf");
    }

    @Test
    void testUploadMaterial_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "arquivo", "teste.pdf", "application/pdf", "conteudo".getBytes()
        );

        mockMvc.perform(multipart("/materiais/topico/{id}", topico.getIdTopico())
                .file(file)
                .with(user(userDetailsDono)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeArquivo", is("teste.pdf")))
                .andExpect(jsonPath("$.urlArquivo", is("http://fake-s3-url.com/novo.pdf")));

        assertEquals(2, materialRepository.count());
    }

    @Test
    void testUploadMaterial_Forbidden_WrongProfessor() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "arquivo", "hack.pdf", "application/pdf", "conteudo".getBytes()
        );

        try {
            mockMvc.perform(multipart("/materiais/topico/{id}", topico.getIdTopico())
                    .file(file)
                    .with(user(userDetailsIntruso)));
        } catch (NestedServletException e) {
            assertTrue(e.getCause() instanceof AccessDeniedException);
        }

        assertEquals(1, materialRepository.count());
    }

    @Test
    void testGetMaterialByTopico_Success() throws Exception {
        mockMvc.perform(get("/materiais/topico/{id}", topico.getIdTopico())
                .with(user(userDetailsDono))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nomeArquivo", is("existente.pdf")));
    }

    @Test
    void testDeleteMaterial_Success() throws Exception {
        mockMvc.perform(delete("/materiais/{id}", materialExistente.getIdMaterialDeAula())
                .with(user(userDetailsDono)))
                .andExpect(status().isOk());

        assertFalse(materialRepository.existsById(materialExistente.getIdMaterialDeAula()));
    }

    @Test
    void testDeleteMaterial_Forbidden() throws Exception {
        try {
            mockMvc.perform(delete("/materiais/{id}", materialExistente.getIdMaterialDeAula())
                    .with(user(userDetailsIntruso)));
        } catch (NestedServletException e) {
            assertTrue(e.getCause() instanceof AccessDeniedException);
        }

        assertEquals(1, materialRepository.count());
    }
}
