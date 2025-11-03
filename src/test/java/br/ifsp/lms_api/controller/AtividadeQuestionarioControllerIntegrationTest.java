// Caminho: src/test/java/br/ifsp/lms_api/controller/AtividadeQuestionarioControllerIntegrationTest.java

package br.ifsp.lms_api.controller;

// Imports do JUnit
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
@ActiveProfiles("test") // Usa o application-test.properties (H2)
class AtividadeQuestionarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Precisamos dos DOIS repositórios reais para este teste
    @Autowired
    private AtividadeQuestionarioRepository atividadeQuestionarioRepository;

    @Autowired
    private QuestoesRepository questoesRepository;

    @BeforeEach
    void setUp() {
        // Configura o ObjectMapper para lidar com datas (LocalDate)
        objectMapper.findAndRegisterModules();
    }


    // =================================================================
    // TESTES DE CRUD PADRÃO
    // =================================================================

    @Test
    @Transactional
    void testCreate_Success() throws Exception {
        // --- Arrange ---
        AtividadeQuestionarioRequestDto requestDto = new AtividadeQuestionarioRequestDto();
        requestDto.setTituloAtividade("Questionário de Teste");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(5));
        requestDto.setStatusAtividade(true);
        requestDto.setNumeroTentativas(3);

        // --- Act ---
        mockMvc.perform(post("/atividades-questionario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
        
        // --- Assert (HTTP) ---
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAtividade").exists())
                .andExpect(jsonPath("$.tituloAtividade").value("Questionário de Teste"));

        // --- Assert (Banco de Dados) ---
        assertEquals(1, atividadeQuestionarioRepository.count());
    }

    @Test
    @Transactional
    void testGetById_Success() throws Exception {
        // --- Arrange ---
        AtividadeQuestionario aq = new AtividadeQuestionario();
        aq.setTituloAtividade("Questionário Salvo");
        aq.setStatusAtividade(true);
        aq.setDataInicioAtividade(LocalDate.now());
        aq.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        
        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aq);
        Long idSalvo = aqSalvo.getIdAtividade();

        // --- Act & Assert ---
        mockMvc.perform(get("/atividades-questionario/{id}", idSalvo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(idSalvo))
                .andExpect(jsonPath("$.tituloAtividade").value("Questionário Salvo"));
    }

    @Test
    @Transactional
    void testGetById_NotFound_404() throws Exception {
        // --- Act & Assert ---
        mockMvc.perform(get("/atividades-questionario/{id}", 999L))
                .andExpect(status().isNotFound());
    }


    // =================================================================
    // TESTES DE RELACIONAMENTO (O MAIS IMPORTANTE)
    // =================================================================

    /**
     * Teste de Integração para POST /{idQuestionario}/questoes
     * Cenário: Cria um Questionário e uma Questão, e depois associa os dois.
     */
    @Test
    @Transactional
    void testAdicionarQuestoesAoQuestionario_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        
        // 1. Salva um Questionário (real) no banco
        AtividadeQuestionario aq = new AtividadeQuestionario();
        aq.setTituloAtividade("Questionário para Associar");
        aq.setDataInicioAtividade(LocalDate.now());
        aq.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        aq.setStatusAtividade(true);
        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aq);
        Long idQuestionario = aqSalvo.getIdAtividade();
        
        // 2. Salva uma Questão (real, válida) no banco
        Questoes questaoSalva = createValidQuestao("Questão para ser associada");
        Long idQuestao = questaoSalva.getIdQuestao();

        // 3. Prepara o "body" da requisição (a lista de IDs)
        List<Long> idsDasQuestoes = List.of(idQuestao);

        // --- 2. Act (Agir) ---
        mockMvc.perform(post("/atividades-questionario/{idQuestionario}/questoes", idQuestionario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(idsDasQuestoes)))
        
       // --- 3. Assert (HTTP) ---
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(idQuestionario))
                // Verifica se o JSON de resposta agora contém a questão
                .andExpect(jsonPath("$.questoesQuestionario.length()").value(1)) // <-- CORRIGIDO
                .andExpect(jsonPath("$.questoesQuestionario[0].idQuestao").value(idQuestao)); // <-- CORRIGIDO
        // --- 3b. Assert (Banco de Dados) ---
        // Busca o questionário do banco para ver se a relação foi salva
        AtividadeQuestionario aqDoBanco = atividadeQuestionarioRepository.findById(idQuestionario).get();
        assertEquals(1, aqDoBanco.getQuestoes().size());
        assertEquals(idQuestao, aqDoBanco.getQuestoes().get(0).getIdQuestao());
    }

    /**
     * Teste de Integração para DELETE /{idQuestionario}/questoes
     * Cenário: Associa 2 questões e remove 1.
     */
    @Test
    @Transactional
    void testRemoverQuestoesDoQuestionario_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        
        // 1. Salva um Questionário
        AtividadeQuestionario aq = new AtividadeQuestionario();
        aq.setTituloAtividade("Questionário para Remover");
        aq.setDataInicioAtividade(LocalDate.now());
        aq.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        aq.setStatusAtividade(true);

        // 2. Salva 2 Questões
        Questoes q1 = createValidQuestao("Questão 1");
        Questoes q2 = createValidQuestao("Questão 2 (fica)");

        // 3. Associa as duas ao Questionário (usando lista mutável)
        aq.setQuestoes(new ArrayList<>(List.of(q1, q2)));
        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aq);
        Long idQuestionario = aqSalvo.getIdAtividade();
        
        // 4. Prepara o "body" (ID da Questão 1 para remover)
        List<Long> idsParaRemover = List.of(q1.getIdQuestao());

        // --- 2. Act (Agir) ---
        mockMvc.perform(delete("/atividades-questionario/{idQuestionario}/questoes", idQuestionario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(idsParaRemover)))
        
 // --- 3. Assert (HTTP) ---
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questoesQuestionario.length()").value(1)) // <-- CORRIGIDO
                .andExpect(jsonPath("$.questoesQuestionario[0].idQuestao").value(q2.getIdQuestao())); // <-- CORRIGID

        // --- 3b. Assert (Banco de Dados) ---
        AtividadeQuestionario aqDoBanco = atividadeQuestionarioRepository.findById(idQuestionario).get();
        assertEquals(1, aqDoBanco.getQuestoes().size());
        assertEquals(q2.getIdQuestao(), aqDoBanco.getQuestoes().get(0).getIdQuestao());
    }

    /**
     * Teste de Integração para DELETE /{idQuestionario}/questoes/todas
     * Cenário: Associa 2 questões e remove todas.
     */
    @Test
    @Transactional
    void testRemoverTodasQuestoesDoQuestionario_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
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

        // --- 2. Act (Agir) ---
        mockMvc.perform(delete("/atividades-questionario/{idQuestionario}/questoes/todas", idQuestionario))
        
     // --- 3. Assert (HTTP) ---
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questoesQuestionario.length()").value(0)); // <-- CORRIGIDO

        // --- 3b. Assert (Banco de Dados) ---
        AtividadeQuestionario aqDoBanco = atividadeQuestionarioRepository.findById(idQuestionario).get();
        assertTrue(aqDoBanco.getQuestoes().isEmpty());
    }


    // =================================================================
    // MÉTODO DE AJUDA (HELPER)
    // =================================================================

    /**
     * Método auxiliar privado para criar uma Questão VÁLIDA (com alternativas)
     * e salvá-la no banco.
     */
    private Questoes createValidQuestao(String enunciado) {
        Questoes questao = new Questoes();
        questao.setEnunciado(enunciado);
        
        Alternativas alt = new Alternativas();
        alt.setAlternativa("Alternativa de teste");
        alt.setAlternativaCorreta(true);
        alt.setQuestoes(questao); // Ligação reversa

        // Usa ArrayList (lista MUTÁVEL) para evitar erro do Hibernate
        List<Alternativas> listaMutavel = new ArrayList<>();
        listaMutavel.add(alt);
        questao.setAlternativas(listaMutavel);

        return questoesRepository.save(questao);
    }

    @Test
    @Transactional 
    void testGetAll_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        // Salva 2 questionários direto no banco H2.
        // (Não precisamos de questões neles para este teste de listagem)

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

        // Salva ambos no repositório REAL
        // (Usando ArrayList para garantir que é mutável, como aprendemos)
        atividadeQuestionarioRepository.saveAll(new ArrayList<>(List.of(aq1, aq2)));

        // Garante que o setup funcionou
        assertEquals(2, atividadeQuestionarioRepository.count());

        // --- 2. Act (Agir) ---
        mockMvc.perform(get("/atividades-questionario"))
        
        // --- 3. Assert (Verificar) ---
                .andExpect(status().isOk()) // Espera um HTTP 200
                // Verifica os campos de paginação (usando 'page' e 'size')
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20)) // 20 é o tamanho padrão do Pageable
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                // Verifica se o array 'content' tem 2 itens
                .andExpect(jsonPath("$.content.length()").value(2))
                // Verifica os dados
                .andExpect(jsonPath("$.content[0].tituloAtividade").value("Questionário 1"))
                .andExpect(jsonPath("$.content[1].tituloAtividade").value("Questionário 2"))
                // Verifica se as listas de questões estão vindo (vazias, neste caso)
                .andExpect(jsonPath("$.content[0].questoesQuestionario.length()").value(0));
    }

    @Test
    @Transactional
    void testUpdate_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        // 1. Salva uma entidade no H2
        AtividadeQuestionario aqAntigo = new AtividadeQuestionario();
        aqAntigo.setTituloAtividade("Questionário para Atualizar");
        aqAntigo.setStatusAtividade(true);
        aqAntigo.setDataInicioAtividade(LocalDate.now());
        aqAntigo.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        aqAntigo.setNumeroTentativas(3); // <-- Valor antigo
        
        AtividadeQuestionario aqSalvo = atividadeQuestionarioRepository.save(aqAntigo);
        Long idParaAtualizar = aqSalvo.getIdAtividade();

        // 2. Cria o DTO de atualização (o "body" do PATCH)
        // (Assumindo que os setters do seu UpdateDto esperam Optionals,
        // como vimos no seu service: `dto.getNumeroTentativas().ifPresent(...)`)
        AtividadeQuestionarioUpdateDto updateDto = new AtividadeQuestionarioUpdateDto();
        updateDto.setNumeroTentativas(Optional.of(5)); // <-- Valor novo

        // --- 2. Act (Agir) ---
        mockMvc.perform(patch("/atividades-questionario/{id}", idParaAtualizar)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        
        // --- 3. Assert (HTTP) ---
                .andExpect(status().isOk()) // Espera 200
                .andExpect(jsonPath("$.idAtividade").value(idParaAtualizar))
                .andExpect(jsonPath("$.numeroTentativas").value(5)); // Verifica o JSON de resposta

        // --- 3b. Assert (Banco de Dados) ---
        // Busca a entidade direto do banco para ver se ela mudou
        AtividadeQuestionario aqDoBanco = atividadeQuestionarioRepository.findById(idParaAtualizar).get();
        assertEquals(5, aqDoBanco.getNumeroTentativas()); // Confirma a mudança no H2
    }
}