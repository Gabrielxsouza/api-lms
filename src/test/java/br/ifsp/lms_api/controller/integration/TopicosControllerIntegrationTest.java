package br.ifsp.lms_api.controller.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.ifsp.lms_api.dto.TopicosDto.TopicosRequestDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosUpdateDto;
import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.repository.AtividadeRepository;
import br.ifsp.lms_api.repository.DisciplinaRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.repository.TurmaRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TopicosControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TopicosRepository topicosRepository;
    
    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private AtividadeRepository atividadeRepository;

    private Turma turmaPadrao;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
        
        atividadeRepository.deleteAll();
        topicosRepository.deleteAll();
        turmaRepository.deleteAll();
        disciplinaRepository.deleteAll();

        Disciplina disciplina = new Disciplina();
        disciplina.setNomeDisciplina("Engenharia de Software");
        disciplina.setCodigoDisciplina("ES-123");
        disciplina.setDescricaoDisciplina("Teste");
        Disciplina disciplinaSalva = disciplinaRepository.save(disciplina);

        Turma turma = new Turma();
        turma.setNomeTurma("Turma A");
        turma.setSemestre("2025.2");
        turma.setDisciplina(disciplinaSalva);
        turmaPadrao = turmaRepository.save(turma);
    }

    @Test
    @Transactional
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTopico").exists())
                .andExpect(jsonPath("$.tituloTopico").value("Novo Tópico"))
                .andExpect(jsonPath("$.atividades[0].idAtividade").value(idAtividade));

        List<Topicos> topicosNoBanco = topicosRepository.findAll();
        assertEquals(1, topicosNoBanco.size());
        assertEquals(htmlLimpa, topicosNoBanco.get(0).getConteudoHtml());
        assertEquals(turmaPadrao.getIdTurma(), topicosNoBanco.get(0).getTurma().getIdTurma());
        assertEquals(1, topicosNoBanco.get(0).getAtividades().size());
        assertEquals(idAtividade, topicosNoBanco.get(0).getAtividades().get(0).getIdAtividade());
    }

    @Test
    @Transactional
    void testCreateTopico_TurmaNotFound_404() throws Exception {
        TopicosRequestDto requestDto = new TopicosRequestDto();
        requestDto.setTituloTopico("Tópico com Turma Falsa");
        requestDto.setIdTurma(999L);
        requestDto.setConteudoHtml("<p>Teste</p>");
        requestDto.setIdAtividade(new ArrayList<>());

        mockMvc.perform(post("/topicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        assertEquals(0, topicosRepository.count());
    }

    @Test
    @Transactional
    void testGetTopicoById_Success() throws Exception {
        Topicos topico = createAndSaveTopico("Tópico Salvo", "...", turmaPadrao);

        mockMvc.perform(get("/topicos/{id}", topico.getIdTopico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTopico").value(topico.getIdTopico()))
                .andExpect(jsonPath("$.tituloTopico").value("Tópico Salvo"))
                .andExpect(jsonPath("$.atividades").exists());
    }

    @Test
    @Transactional
    void testGetTopicoById_NotFound_404() throws Exception {
        mockMvc.perform(get("/topicos/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void testGetTopicosByTurmaId_Success() throws Exception {
        Turma turma2 = new Turma();
        turma2.setNomeTurma("Outra Turma");
        turma2.setSemestre("2025.1");
        turma2.setDisciplina(turmaPadrao.getDisciplina());
        turmaRepository.save(turma2);

        createAndSaveTopico("Tópico 1", "...", turmaPadrao);
        createAndSaveTopico("Tópico 2", "...", turmaPadrao);
        createAndSaveTopico("Tópico 3", "...", turma2);

        mockMvc.perform(get("/topicos/turma/{idTurma}", turmaPadrao.getIdTurma()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].tituloTopico").value("Tópico 1"))
                .andExpect(jsonPath("$.content[0].atividades").exists());
    }

    @Test
    @Transactional
    void testDeleteTopico_Success() throws Exception {
        Topicos topicoSalvo = createAndSaveTopico("Tópico para Deletar", "...", turmaPadrao);
        Long id = topicoSalvo.getIdTopico();
        
        assertEquals(1, topicosRepository.count());

        mockMvc.perform(delete("/topicos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTopico").value(id));

        assertEquals(0, topicosRepository.count());
    }

    @Test
    @Transactional
    void testUpdateTopico_Success_And_SanitizesHtml() throws Exception {
        Topicos topicoAntigo = createAndSaveTopico("Tópico Antigo", "HTML Antigo", turmaPadrao);
        Long id = topicoAntigo.getIdTopico();

        String htmlSuja = "<p>HTML Novo</p><script>alert(1)</script>";
        String htmlLimpa = "<p>HTML Novo</p>";

        TopicosUpdateDto updateDto = new TopicosUpdateDto();
        updateDto.setTituloTopico(Optional.of("Tópico Novo"));
        updateDto.setConteudoHtml(Optional.of(htmlSuja));

        mockMvc.perform(patch("/topicos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloTopico").value("Tópico Novo"));
        
        Topicos topicoDoBanco = topicosRepository.findById(id).get();
        assertEquals("Tópico Novo", topicoDoBanco.getTituloTopico());
        assertEquals(htmlLimpa, topicoDoBanco.getConteudoHtml());
    }

    private Topicos createAndSaveTopico(String titulo, String conteudo, Turma turma) {
        Topicos topico = new Topicos();
        topico.setTituloTopico(titulo);
        topico.setConteudoHtml(conteudo);
        topico.setTurma(turma);
        topico.setAtividades(new ArrayList<>());
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