package br.ifsp.lms_api.controller.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
// Import necessário para o .with(user(...))
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import br.ifsp.lms_api.dto.TurmaDto.TurmaRequestDto;
import br.ifsp.lms_api.model.Administrador; // Importar Administrador
import br.ifsp.lms_api.model.Curso;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.AdministradorRepository; // Importar Repository
import br.ifsp.lms_api.repository.CursoRepository;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.ProfessorRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import br.ifsp.lms_api.service.AutentificacaoService;
import jakarta.persistence.EntityManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TurmaControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private ProfessorRepository professorRepository;

    // Repositório extra para criar o Admin
    @Autowired private AdministradorRepository administradorRepository;

    @MockBean
    private AutentificacaoService autentificacaoService;

    @Autowired
    private EntityManager entityManager;

    private Disciplina disciplinaExistente;
    private Curso cursoExistente;
    private Professor professorExistente;
    private Administrador adminExistente; // Admin para realizar as operações
    private Turma turmaExistente;

    private CustomUserDetails professorUserDetails;
    private CustomUserDetails adminUserDetails;

    @BeforeEach
    void setUp() {
        // --- 1. LIMPEZA DO BANCO (Ordem estrita para evitar Constraint Violation) ---
        entityManager.createNativeQuery("DELETE FROM atividade_arquivos_permitidos").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM atividade_tags").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM topico_tags").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM questao_tags").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM questionario_questoes").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tentativa_texto").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tentativa_questionario").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tentativa_arquivo").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM material_de_aula").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM alternativas").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM questoes").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM atividade").executeUpdate();

        entityManager.createNativeQuery("DELETE FROM topicos").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM matricula").executeUpdate();

        // Agora podemos deletar a Turma e os Usuários
        turmaRepository.deleteAll();
        cursoRepository.deleteAll();
        disciplinaRepository.deleteAll();
        professorRepository.deleteAll();
        administradorRepository.deleteAll();

        // --- 2. CRIAÇÃO DOS DADOS BASE ---

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

        // 2.1 CRIAR PROFESSOR (Para ser o professor da turma)
        Professor professor = new Professor();
        professor.setNome("Prof. Base");
        professor.setEmail("prof.base@test.com");
        professor.setCpf("12345678900");
        professor.setSenha("123456"); // Senha válida (>6 chars)
        professor.setTipoUsuario("PROFESSOR"); // Role correta
        professor.setDepartamento("COMP");
        professorExistente = professorRepository.save(professor);

        // UserDetails do Professor (para testar /minhas-turmas)
        professorUserDetails = new CustomUserDetails(professorExistente);

        // 2.2 CRIAR ADMIN (Para criar/editar/deletar a turma)
        Administrador admin = new Administrador();
        admin.setNome("Admin Teste");
        admin.setEmail("admin@test.com");
        admin.setCpf("00000000000");
        admin.setSenha("123456");
        admin.setTipoUsuario("ADMIN"); // Role correta
        adminExistente = administradorRepository.save(admin);

        // UserDetails do Admin (para operações de gestão)
        adminUserDetails = new CustomUserDetails(adminExistente);

        // 2.3 CRIAR TURMA
        Turma turma = new Turma();
        turma.setNomeTurma("Turma Base");
        turma.setSemestre("2025/1");
        turma.setDisciplina(disciplinaExistente);
        turma.setCurso(cursoExistente);
        turma.setProfessor(professorExistente);
        turmaExistente = turmaRepository.save(turma);

        entityManager.flush();
        entityManager.clear();

        // Recarregar referências
        professorExistente = professorRepository.findById(professorExistente.getIdUsuario()).get();
        adminExistente = administradorRepository.findById(adminExistente.getIdUsuario()).get();
        turmaExistente = turmaRepository.findById(turmaExistente.getIdTurma()).get();
        disciplinaExistente = disciplinaRepository.findById(disciplinaExistente.getIdDisciplina()).get();
        cursoExistente = cursoRepository.findById(cursoExistente.getIdCurso()).get();

        // Configurar o Mock para quando o Service pedir "quem é o usuário logado"
        // Isso é usado principalmente no método getMinhasTurmas
        when(autentificacaoService.getUsuarioLogado()).thenReturn(professorExistente);
    }

    @Test
    void testCreateTurma_Success() throws Exception {
        TurmaRequestDto requestDto = new TurmaRequestDto(
            "Turma Nova Avulsa",
            "2025/2",
            disciplinaExistente.getIdDisciplina(),
            cursoExistente.getIdCurso(),
            professorExistente.getIdUsuario()
        );

        mockMvc.perform(post("/turmas")
                .with(user(adminUserDetails)) // USA O ADMIN PARA CRIAR
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
            999L, // ID Inexistente
            cursoExistente.getIdCurso(),
            professorExistente.getIdUsuario()
        );

        mockMvc.perform(post("/turmas")
                .with(user(adminUserDetails)) // USA O ADMIN PARA TENTAR CRIAR
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        assertEquals(1, turmaRepository.count());
    }

    @Test
    void testGetAllTurmas_Success() throws Exception {
        mockMvc.perform(get("/turmas")
                .with(user(adminUserDetails)) // Admin pode ver tudo
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nomeTurma", is("Turma Base")));
    }

    @Test
    void testGetMinhasTurmas_Success() throws Exception {
        // ESTE TESTE USA O PROFESSOR, pois verifica as turmas DELE
        mockMvc.perform(get("/turmas/minhas-turmas")
                .with(user(professorUserDetails))
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
                .with(user(adminUserDetails)) // USA O ADMIN PARA ATUALIZAR
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTurma", is(id.intValue())))
                .andExpect(jsonPath("$.nomeTurma", is("Turma Base ATUALIZADA")));
    }

    @Test
    void testDeleteTurma_Success() throws Exception {
        Long id = turmaExistente.getIdTurma();

        mockMvc.perform(delete("/turmas/{id}", id)
                .with(user(adminUserDetails)))
                .andExpect(status().isNoContent());

        assertFalse(turmaRepository.findById(id).isPresent());
        assertEquals(0, turmaRepository.count());
        assertEquals(1, disciplinaRepository.count());
    }
}
