package br.ifsp.lms_api.controller.integration;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.matriculaDto.MatriculaRequestDto;
import br.ifsp.lms_api.dto.matriculaDto.MatriculaUpdateDto;
import br.ifsp.lms_api.model.Administrador;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.Curso;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Matricula;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Status;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.AdministradorRepository;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.CursoRepository;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.MatriculaRepository;
import br.ifsp.lms_api.repository.ProfessorRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import br.ifsp.lms_api.service.AutentificacaoService;
import jakarta.persistence.EntityManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class MatriculaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MatriculaRepository matriculaRepository;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private TurmaRepository turmaRepository;
    @Autowired
    private DisciplinaRepository disciplinaRepository;
    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private EntityManager entityManager;
    @MockBean
    private AutentificacaoService autentificacaoService;

    private Aluno aluno;
    private Turma turma;
    private Matricula matriculaExistente;
    private CustomUserDetails adminUserDetails;
    private CustomUserDetails professorUserDetails;

    @BeforeEach
    void setUp() {
        matriculaRepository.deleteAll();
        turmaRepository.deleteAll();
        alunoRepository.deleteAll();
        professorRepository.deleteAll();
        administradorRepository.deleteAll();
        cursoRepository.deleteAll();
        disciplinaRepository.deleteAll();

        Administrador admin = new Administrador();
        admin.setNome("Admin");
        admin.setEmail("admin@test.com");
        admin.setCpf("11111111111");
        admin.setSenha("123456");
        admin.setTipoUsuario("ADMIN");
        administradorRepository.save(admin);
        adminUserDetails = new CustomUserDetails(admin);

        Professor prof = new Professor();
        prof.setNome("Prof");
        prof.setEmail("prof@test.com");
        prof.setCpf("22222222222");
        prof.setSenha("123456");
        prof.setTipoUsuario("PROFESSOR");
        professorRepository.save(prof);
        professorUserDetails = new CustomUserDetails(prof);

        aluno = new Aluno();
        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno@test.com");
        aluno.setCpf("33333333333");
        aluno.setRa("RA123");
        aluno.setSenha("123456");
        aluno.setTipoUsuario("ALUNO");
        aluno = alunoRepository.save(aluno);

        Curso curso = new Curso(null, "Curso TI", "TI", "Curso Legal", null);
        curso = cursoRepository.save(curso);

        Disciplina disc = new Disciplina(null, "Java", "Prog", "JAV-01", null);
        disc = disciplinaRepository.save(disc);

        turma = new Turma();
        turma.setNomeTurma("Turma A");
        turma.setSemestre("2025/1");
        turma.setProfessor(prof);
        turma.setCurso(curso);
        turma.setDisciplina(disc);
        turma = turmaRepository.save(turma);

        matriculaExistente = new Matricula();
        matriculaExistente.setAluno(aluno);
        matriculaExistente.setTurma(turma);
        matriculaExistente.setStatusMatricula(Status.ATIVA);
        matriculaExistente = matriculaRepository.save(matriculaExistente);

        entityManager.flush();
        entityManager.clear();

        aluno = alunoRepository.findById(aluno.getIdUsuario()).get();
        turma = turmaRepository.findById(turma.getIdTurma()).get();
        matriculaExistente = matriculaRepository.findById(matriculaExistente.getIdMatricula()).get();

        when(autentificacaoService.getUsuarioLogado()).thenReturn(admin);
    }

    @Test
    void createMatricula_Success() throws Exception {
        Aluno novoAluno = new Aluno();
        novoAluno.setNome("Novo Aluno");
        novoAluno.setEmail("novo@test.com");
        novoAluno.setCpf("44444444444");
        novoAluno.setRa("RA999");
        novoAluno.setSenha("123456");
        novoAluno.setTipoUsuario("ALUNO");
        novoAluno = alunoRepository.save(novoAluno);

        MatriculaRequestDto request = new MatriculaRequestDto(
                novoAluno.getIdUsuario(),
                turma.getIdTurma(),
                Status.PENDENTE);

        mockMvc.perform(post("/matriculas")
                .with(user(adminUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeAluno", is("Novo Aluno")))
                .andExpect(jsonPath("$.statusMatricula", is("PENDENTE")));

        assertEquals(2, matriculaRepository.count());
    }

    @Test
    void getAllMatriculas_Professor_Success() throws Exception {
        mockMvc.perform(get("/matriculas")
                .with(user(professorUserDetails))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void updateMatricula_Success() throws Exception {
        Long id = matriculaExistente.getIdMatricula();
        MatriculaUpdateDto updateDto = new MatriculaUpdateDto(Optional.of("REPROVADA"));

        mockMvc.perform(patch("/matriculas/{id}", id)
                .with(user(adminUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMatricula", is("REPROVADA")));

        Status statusAtual = matriculaRepository.findById(id).get().getStatusMatricula();
        assertEquals(Status.REPROVADA, statusAtual);
    }

    @Test
    void deleteMatricula_Success() throws Exception {
        Long id = matriculaExistente.getIdMatricula();

        mockMvc.perform(delete("/matriculas/{id}", id)
                .with(user(adminUserDetails)))
                .andExpect(status().isNoContent());

        assertFalse(matriculaRepository.findById(id).isPresent());
    }
}