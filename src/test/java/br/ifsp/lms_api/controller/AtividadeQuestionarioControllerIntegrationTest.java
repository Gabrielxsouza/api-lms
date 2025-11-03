package br.ifsp.lms_api.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioRequestDto;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioUpdateDto;
import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.AtividadeQuestionario;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.repository.AtividadeQuestionarioRepository;
import br.ifsp.lms_api.repository.QuestoesRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test") 
class AtividadeQuestionarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AtividadeQuestionarioRepository atividadeQuestionarioRepository;

    @Autowired
    private QuestoesRepository questoesRepository;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
    }


    @Test
    @Transactional
    void testCreate_Success() throws Exception {
        AtividadeQuestionarioRequestDto requestDto = new AtividadeQuestionarioRequestDto();
        requestDto.setTituloAtividade("Questionário de Teste");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(5));
        requestDto.setStatusAtividade(true);
        requestDto.setNumeroTentativas(3);

        mockMvc.perform(post("/atividades-questionario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
        
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAtividade").exists())
                .andExpect(jsonPath("$.tituloAtividade").value("Questionário de Teste"));

        assertEquals(1, atividadeQuestionarioRepository.count());
    }

    @Test
    @Transactional
    void testGetById_Success() throws Exception {
        AtividadeQuestionario aq = new AtividadeQuestionario();
        aq.setTituloAtividade("Questionário Salvo");
        aq.setStatusAtividade(true);
        aq.setDataInicioAtividade(LocalDate.now());
        aq.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        
        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aq);
        Long idSalvo = aqSalvo.getIdAtividade();

        mockMvc.perform(get("/atividades-questionario/{id}", idSalvo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(idSalvo))
                .andExpect(jsonPath("$.tituloAtividade").value("Questionário Salvo"));
    }

    @Test
    @Transactional
    void testGetById_NotFound_404() throws Exception {
        mockMvc.perform(get("/atividades-questionario/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void testAdicionarQuestoesAoQuestionario_Success() throws Exception {
        AtividadeQuestionario aq = new AtividadeQuestionario();
        aq.setTituloAtividade("Questionário para Associar");
        aq.setDataInicioAtividade(LocalDate.now());
        aq.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        aq.setStatusAtividade(true);
        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aq);
        Long idQuestionario = aqSalvo.getIdAtividade();
        
        Questoes questaoSalva = createValidQuestao("Questão para ser associada");
        Long idQuestao = questaoSalva.getIdQuestao();

        List<Long> idsDasQuestoes = List.of(idQuestao);

        mockMvc.perform(post("/atividades-questionario/{idQuestionario}/questoes", idQuestionario)
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
    @Transactional
    void testRemoverQuestoesDoQuestionario_Success() throws Exception {
        AtividadeQuestionario aq = new AtividadeQuestionario();
        aq.setTituloAtividade("Questionário para Remover");
        aq.setDataInicioAtividade(LocalDate.now());
        aq.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        aq.setStatusAtividade(true);

        Questoes q1 = createValidQuestao("Questão 1");
        Questoes q2 = createValidQuestao("Questão 2 (fica)");

        aq.setQuestoes(new ArrayList<>(List.of(q1, q2)));
        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aq);
        Long idQuestionario = aqSalvo.getIdAtividade();
        
        List<Long> idsParaRemover = List.of(q1.getIdQuestao());

        mockMvc.perform(delete("/atividades-questionario/{idQuestionario}/questoes", idQuestionario)
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
    @Transactional
    void testRemoverTodasQuestoesDoQuestionario_Success() throws Exception {
        AtividadeQuestionario aq = new AtividadeQuestionario();
        aq.setTituloAtividade("Questionário para Limpar");
        aq.setDataInicioAtividade(LocalDate.now());
        aq.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        aq.setStatusAtividade(true);

        Questoes q1 = createValidQuestao("Questão A");
        Questoes q2 = createValidQuestao("Questão B");

        aq.setQuestoes(new ArrayList<>(List.of(q1, q2)));
        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aq);
        Long idQuestionario = aqSalvo.getIdAtividade();

        mockMvc.perform(delete("/atividades-questionario/{idQuestionario}/questoes/todas", idQuestionario))
        
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questoesQuestionario.length()").value(0)); 

        AtividadeQuestionario aqDoBanco = atividadeQuestionarioRepository.findById(idQuestionario).get();
        assertTrue(aqDoBanco.getQuestoes().isEmpty());
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

    @Test
    @Transactional 
    void testGetAll_Success() throws Exception {
        AtividadeQuestionario aq1 = new AtividadeQuestionario();
        aq1.setTituloAtividade("Questionário 1");
        aq1.setStatusAtividade(true);
        aq1.setDataInicioAtividade(LocalDate.now());
        aq1.setDataFechamentoAtividade(LocalDate.now().plusDays(1));

        AtividadeQuestionario aq2 = new AtividadeQuestionario();
        aq2.setTituloAtividade("Questionário 2");
        aq2.setStatusAtividade(true);
        aq2.setDataInicioAtividade(LocalDate.now());
        aq2.setDataFechamentoAtividade(LocalDate.now().plusDays(2));

        atividadeQuestionarioRepository.saveAll(new ArrayList<>(List.of(aq1, aq2)));

        assertEquals(2, atividadeQuestionarioRepository.count());

        mockMvc.perform(get("/atividades-questionario"))
        
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20)) 
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].tituloAtividade").value("Questionário 1"))
                .andExpect(jsonPath("$.content[1].tituloAtividade").value("Questionário 2"))
                .andExpect(jsonPath("$.content[0].questoesQuestionario.length()").value(0));
    }

    @Test
    @Transactional
    void testUpdate_Success() throws Exception {
        AtividadeQuestionario aqAntigo = new AtividadeQuestionario();
        aqAntigo.setTituloAtividade("Questionário para Atualizar");
        aqAntigo.setStatusAtividade(true);
        aqAntigo.setDataInicioAtividade(LocalDate.now());
        aqAntigo.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        aqAntigo.setNumeroTentativas(3); 
        
        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aqAntigo);
        Long idParaAtualizar = aqSalvo.getIdAtividade();

        AtividadeQuestionarioUpdateDto updateDto = new AtividadeQuestionarioUpdateDto();
        updateDto.setNumeroTentativas(Optional.of(5)); 

        mockMvc.perform(patch("/atividades-questionario/{id}", idParaAtualizar)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.idAtividade").value(idParaAtualizar))
                .andExpect(jsonPath("$.numeroTentativas").value(5)); 

        AtividadeQuestionario aqDoBanco = atividadeQuestionarioRepository.findById(idParaAtualizar).get();
        assertEquals(5, aqDoBanco.getNumeroTentativas()); 
    }
}
