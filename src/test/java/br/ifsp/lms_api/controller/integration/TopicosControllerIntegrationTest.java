package br.ifsp.lms_api.controller.integration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; 
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.TopicosDto.TopicosRequestDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosUpdateDto;
import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.AtividadeRepository;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.ProfessorRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.repository.TurmaRepository;
import br.ifsp.lms_api.service.AutentificacaoService; 
import jakarta.persistence.EntityManager;
import static org.mockito.Mockito.when; 

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TopicosControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TopicosRepository topicosRepository;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private AtividadeRepository atividadeRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private EntityManager entityManager;

   
    @MockBean
    private AutentificacaoService autentificacaoService;

    private Turma turmaPadrao;
    private Professor professorDono;
    private CustomUserDetails userDetails; 

    @BeforeEach
    void setUp() {
       
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

        turmaRepository.deleteAll();
        disciplinaRepository.deleteAll();
        professorRepository.deleteAll();
        
       
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        
        professorDono = new Professor();
        professorDono.setNome("Prof. Topico " + suffix);
        professorDono.setEmail("prof." + suffix + "@teste.com"); 
        professorDono.setSenha("123456");
        professorDono.setCpf(generateFakeCpf()); 
        professorDono.setDepartamento("Dep");
     
        professorDono.setTipoUsuario("PROFESSOR"); 
        
        professorDono = professorRepository.save(professorDono);
        entityManager.flush();

        
        userDetails = new CustomUserDetails(professorDono);

       
        when(autentificacaoService.getUsuarioLogado()).thenReturn(professorDono);

        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina("Engenharia " + suffix);
        disciplina.setCodigoDisciplina("ES-" + suffix);
        disciplina.setDescricaoDisciplina("Teste");
        Disciplina disciplinaSalva = disciplinaRepository.save(disciplina);

        Turma turma = new Turma();
        turma.setNomeTurma("Turma " + suffix);
        turma.setSemestre("2025.2");
        turma.setDisciplina(disciplinaSalva);
        turma.setProfessor(professorDono);
        turmaPadrao = turmaRepository.save(turma);
        
        entityManager.flush();
        entityManager.clear();
        
       
        turmaPadrao = turmaRepository.findById(turma.getIdTurma()).get();
        professorDono = professorRepository.findById(professorDono.getIdUsuario()).get();
        
        
        when(autentificacaoService.getUsuarioLogado()).thenReturn(professorDono);
    }
    
    private String generateFakeCpf() {
        long num = (long) (Math.random() * 10000000000L);
        return String.format("%011d", num);
    }

    @Test
    void testCreateTopico_Success_And_SanitizesHtml() throws Exception {
        String htmlSuja = "<b>Conteúdo</b><script>alert('XSS')</script>";
        String htmlLimpa = "<b>Conteúdo</b>";

        AtividadeTexto atividade = createAndSaveAtividadeTexto("Atividade 1");
        Long idAtividade = atividade.getIdAtividade();

        TopicosRequestDto requestDto = new TopicosRequestDto();
        requestDto.setTituloTopico("Novo Tópico");
        requestDto.setIdTurma(turmaPadrao.getIdTurma());
        requestDto.setConteudoHtml(htmlSuja);
        requestDto.setIdAtividade(List.of(idAtividade));

        mockMvc.perform(post("/topicos")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTopico").exists());

        List<Topicos> topicos = topicosRepository.findAll();
        assertEquals(1, topicos.size());
        assertEquals(htmlLimpa, topicos.get(0).getConteudoHtml());
    }

    @Test
    void testCreateTopico_TurmaNotFound_404() throws Exception {
        TopicosRequestDto requestDto = new TopicosRequestDto();
        requestDto.setTituloTopico("Tópico Falso");
        requestDto.setIdTurma(99999L);
        requestDto.setConteudoHtml("<p>Teste</p>");
        requestDto.setIdAtividade(new ArrayList<>());

        mockMvc.perform(post("/topicos")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTopicoById_Success() throws Exception {
        Topicos topico = createAndSaveTopico("Tópico Salvo", "...", turmaPadrao);

        mockMvc.perform(get("/topicos/{id}", topico.getIdTopico())
                .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTopicoById_NotFound_404() throws Exception {
        mockMvc.perform(get("/topicos/{id}", 99999L)
                .with(user(userDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTopicosByTurmaId_Success() throws Exception {
        createAndSaveTopico("Tópico 1", "...", turmaPadrao);
        createAndSaveTopico("Tópico 2", "...", turmaPadrao);

        mockMvc.perform(get("/topicos/turma/{idTurma}", turmaPadrao.getIdTurma())
                .with(user(userDetails))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void testDeleteTopico_Success() throws Exception {
        Topicos topico = createAndSaveTopico("Para Deletar", "...", turmaPadrao);
        Long id = topico.getIdTopico();
        
        mockMvc.perform(delete("/topicos/{id}", id)
                .with(user(userDetails)))
                .andExpect(status().isOk()); 
    }

    @Test
    void testUpdateTopico_Success_And_SanitizesHtml() throws Exception {
        Topicos topico = createAndSaveTopico("Antigo", "HTML Antigo", turmaPadrao);
        Long id = topico.getIdTopico();

        TopicosUpdateDto updateDto = new TopicosUpdateDto();
        updateDto.setTituloTopico(Optional.of("Novo"));
        
        mockMvc.perform(patch("/topicos/{id}", id)
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloTopico").value("Novo"));
    }

    private Topicos createAndSaveTopico(String titulo, String conteudo, Turma turma) {
        Topicos topico = new Topicos();
        topico.setTituloTopico(titulo);
        topico.setConteudoHtml(conteudo);
        topico.setTurma(turma);
        return topicosRepository.save(topico);
    }
    
    private AtividadeTexto createAndSaveAtividadeTexto(String titulo) {
        AtividadeTexto at = new AtividadeTexto();
        at.setTituloAtividade(titulo);
        at.setDescricaoAtividade("Desc");
        at.setDataInicioAtividade(LocalDate.now());
        at.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        at.setStatusAtividade(true);
        at.setNumeroMaximoCaracteres(1000L);
        return atividadeRepository.save(at);
    }
}