package br.ifsp.lms_api.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioRequestDto;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioUpdateDto;
import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.AtividadeQuestionario;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.AtividadeQuestionarioRepository;
import br.ifsp.lms_api.repository.ProfessorRepository;
import br.ifsp.lms_api.repository.QuestoesRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.repository.TurmaRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AtividadeQuestionarioControllerIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AtividadeQuestionarioRepository atividadeQuestionarioRepository;

    @Autowired
    private QuestoesRepository questoesRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private TopicosRepository topicoRepository;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testCreate_Success() throws Exception {
        AtividadeQuestionarioRequestDto requestDto = new AtividadeQuestionarioRequestDto();
        requestDto.setTituloAtividade("Questionário de Teste");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(5));
        requestDto.setStatusAtividade(true);
        requestDto.setNumeroTentativas(3);

        mockMvc.perform(post("/atividades-questionario")
                .with(csrf())
                .with(user("professor").roles("PROFESSOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAtividade").exists())
                .andExpect(jsonPath("$.tituloAtividade").value("Questionário de Teste"));

        assertTrue(atividadeQuestionarioRepository.count() > 0);
    }

    @Test
    void testGetById_Success() throws Exception {
        AtividadeQuestionario aq = new AtividadeQuestionario();
        aq.setTituloAtividade("Questionário Salvo");
        aq.setStatusAtividade(true);
        aq.setDataInicioAtividade(LocalDate.now());
        aq.setDataFechamentoAtividade(LocalDate.now().plusDays(1));

        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aq);
        Long idSalvo = aqSalvo.getIdAtividade();

        mockMvc.perform(get("/atividades-questionario/{id}", idSalvo)
                .with(user("aluno")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(idSalvo))
                .andExpect(jsonPath("$.tituloAtividade").value("Questionário Salvo"));
    }

    @Test
    void testGetById_NotFound_404() throws Exception {
        mockMvc.perform(get("/atividades-questionario/{id}", 999L)
                .with(user("aluno")))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAdicionarQuestoesAoQuestionario_Success() throws Exception {
        Professor professor = createProfessor();
        Topicos topico = createHierarchy(professor);

        AtividadeQuestionario aq = new AtividadeQuestionario();
        aq.setTituloAtividade("Questionário para Associar");
        aq.setDataInicioAtividade(LocalDate.now());
        aq.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        aq.setStatusAtividade(true);
        aq.setTopico(topico);
        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aq);
        Long idQuestionario = aqSalvo.getIdAtividade();

        Questoes questaoSalva = createValidQuestao("Questão para ser associada");
        Long idQuestao = questaoSalva.getIdQuestao();

        List<Long> idsDasQuestoes = List.of(idQuestao);

        CustomUserDetails userDetails = mockCustomUser(professor.getIdUsuario());

        mockMvc.perform(post("/atividades-questionario/{idQuestionario}/questoes", idQuestionario)
                .with(csrf())
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(idsDasQuestoes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(idQuestionario))
                .andExpect(jsonPath("$.questoesQuestionario.length()").value(1))
                .andExpect(jsonPath("$.questoesQuestionario[0].idQuestao").value(idQuestao));

        AtividadeQuestionario aqDoBanco = atividadeQuestionarioRepository.findById(idQuestionario).get();
        assertEquals(1, aqDoBanco.getQuestoes().size());
        assertEquals(idQuestao, aqDoBanco.getQuestoes().get(0).getIdQuestao());
    }

    @Test
    void testRemoverQuestoesDoQuestionario_Success() throws Exception {
        Professor professor = createProfessor();
        Topicos topico = createHierarchy(professor);

        AtividadeQuestionario aq = new AtividadeQuestionario();
        aq.setTituloAtividade("Questionário para Remover");
        aq.setDataInicioAtividade(LocalDate.now());
        aq.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        aq.setStatusAtividade(true);
        aq.setTopico(topico);

        Questoes q1 = createValidQuestao("Questão 1");
        Questoes q2 = createValidQuestao("Questão 2 (fica)");

        aq.setQuestoes(new ArrayList<>(List.of(q1, q2)));
        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aq);
        Long idQuestionario = aqSalvo.getIdAtividade();

        List<Long> idsParaRemover = List.of(q1.getIdQuestao());

        CustomUserDetails userDetails = mockCustomUser(professor.getIdUsuario());

        mockMvc.perform(patch("/atividades-questionario/{idQuestionario}/questoes", idQuestionario)
                .with(csrf())
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(idsParaRemover)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questoesQuestionario.length()").value(1))
                .andExpect(jsonPath("$.questoesQuestionario[0].idQuestao").value(q2.getIdQuestao()));

        AtividadeQuestionario aqDoBanco = atividadeQuestionarioRepository.findById(idQuestionario).get();
        assertEquals(1, aqDoBanco.getQuestoes().size());
        assertEquals(q2.getIdQuestao(), aqDoBanco.getQuestoes().get(0).getIdQuestao());
    }

    @Test
    void testRemoverTodasQuestoesDoQuestionario_Success() throws Exception {
        Professor professor = createProfessor();
        Topicos topico = createHierarchy(professor);

        AtividadeQuestionario aq = new AtividadeQuestionario();
        aq.setTituloAtividade("Questionário para Limpar");
        aq.setDataInicioAtividade(LocalDate.now());
        aq.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        aq.setStatusAtividade(true);
        aq.setTopico(topico);

        Questoes q1 = createValidQuestao("Questão A");
        Questoes q2 = createValidQuestao("Questão B");

        aq.setQuestoes(new ArrayList<>(List.of(q1, q2)));
        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aq);
        Long idQuestionario = aqSalvo.getIdAtividade();

        CustomUserDetails userDetails = mockCustomUser(professor.getIdUsuario());

        mockMvc.perform(delete("/atividades-questionario/{idQuestionario}/questoes/todas", idQuestionario)
                .with(csrf())
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questoesQuestionario.length()").value(0));

        AtividadeQuestionario aqDoBanco = atividadeQuestionarioRepository.findById(idQuestionario).get();
        assertTrue(aqDoBanco.getQuestoes().isEmpty());
    }

    @Test
    void testGetAll_Success() throws Exception {

        try {
            jdbcTemplate.execute("DELETE FROM tentativa_questionario");

        } catch (Exception e) {

        }
        atividadeQuestionarioRepository.deleteAll();

        Professor professor = createProfessor();
        Topicos topico = createHierarchy(professor);

        AtividadeQuestionario aq1 = new AtividadeQuestionario();
        aq1.setTituloAtividade("Questionário 1");
        aq1.setStatusAtividade(true);
        aq1.setDataInicioAtividade(LocalDate.now());
        aq1.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        aq1.setTopico(topico);
        aq1.setQuestoes(new ArrayList<>());

        AtividadeQuestionario aq2 = new AtividadeQuestionario();
        aq2.setTituloAtividade("Questionário 2");
        aq2.setStatusAtividade(true);
        aq2.setDataInicioAtividade(LocalDate.now());
        aq2.setDataFechamentoAtividade(LocalDate.now().plusDays(2));
        aq2.setTopico(topico);
        aq2.setQuestoes(new ArrayList<>());

        atividadeQuestionarioRepository.saveAll(List.of(aq1, aq2));

        mockMvc.perform(get("/atividades-questionario")
                .with(user("professor").roles("PROFESSOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].tituloAtividade").value("Questionário 1"))
                .andExpect(jsonPath("$.content[1].tituloAtividade").value("Questionário 2"));
    }

    @Test
    void testUpdate_Success() throws Exception {
        Professor professor = createProfessor();
        Topicos topico = createHierarchy(professor);

        AtividadeQuestionario aqAntigo = new AtividadeQuestionario();
        aqAntigo.setTituloAtividade("Questionário para Atualizar");
        aqAntigo.setStatusAtividade(true);
        aqAntigo.setDataInicioAtividade(LocalDate.now());
        aqAntigo.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        aqAntigo.setNumeroTentativas(3);
        aqAntigo.setTopico(topico);

        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aqAntigo);
        Long idParaAtualizar = aqSalvo.getIdAtividade();

        AtividadeQuestionarioUpdateDto updateDto = new AtividadeQuestionarioUpdateDto();
        updateDto.setNumeroTentativas(Optional.of(5));

        CustomUserDetails userDetails = mockCustomUser(professor.getIdUsuario());

        mockMvc.perform(patch("/atividades-questionario/{id}", idParaAtualizar)
                .with(csrf())
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(idParaAtualizar))
                .andExpect(jsonPath("$.numeroTentativas").value(5));

        AtividadeQuestionario aqDoBanco = atividadeQuestionarioRepository.findById(idParaAtualizar).get();
        assertEquals(5, aqDoBanco.getNumeroTentativas());
    }

    private Questoes createValidQuestao(String enunciado) {
        Questoes questao = new Questoes();
        questao.setEnunciado(enunciado);

        Alternativas alt = new Alternativas();
        alt.setAlternativa("Alternativa de teste");
        alt.setAlternativaCorreta(true);
        alt.setQuestoes(questao);

        List<Alternativas> listaMutavel = new ArrayList<>();
        listaMutavel.add(alt);
        questao.setAlternativas(listaMutavel);

        return questoesRepository.save(questao);
    }

    private Professor createProfessor() {
        Professor professor = new Professor();
        professor.setNome("Professor Teste");
        professor.setEmail("prof" + System.currentTimeMillis() + "@teste.com");
        professor.setSenha("123456");
        professor.setCpf("111.222.333-44");
        return professorRepository.save(professor);
    }

    private Topicos createHierarchy(Professor professor) {
        Turma turma = new Turma();
        turma.setProfessor(professor);
        turma.setNomeTurma("Turma de Teste Integration");
        turma.setSemestre("2025/1");
        turma = turmaRepository.save(turma);

        Topicos topico = new Topicos();
        topico.setTurma(turma);

        topico.setTituloTopico("Tópico Geral de Teste");

        return topicoRepository.save(topico);
    }

    private CustomUserDetails mockCustomUser(Long id) {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(id);
        when(userDetails.getUsername()).thenReturn("user");

        doReturn(List.of(new SimpleGrantedAuthority("ROLE_PROFESSOR")))
            .when(userDetails).getAuthorities();

        return userDetails;
    }
}
