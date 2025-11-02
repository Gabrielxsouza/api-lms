// Salve este arquivo em:
// src/test/java/br/ifsp/lms_api/controller/QuestoesControllerIntegrationTest.java
// (Note o "IntegrationTest" no nome para diferenciar)

package br.ifsp.lms_api.controller;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.dto.alternativasDto.AlternativasRequestDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesRequestDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesUpdateDto;
import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.repository.AtividadeQuestionarioRepository;
import br.ifsp.lms_api.repository.QuestoesRepository;
// 1. @SpringBootTest: Carrega a aplicação INTEIRA (Controllers, Services, Repositories)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc // 2. Fornece o MockMvc para fazer requisições
@ActiveProfiles("test") // 3. Força o uso do 'application-test.properties' (Banco H2)
class QuestoesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuestoesRepository questoesRepository; // Injetamos o Repositório REAL

    @Autowired
    private AtividadeQuestionarioRepository atividadeQuestionarioRepository;

    // 4. @Transactional: Faz um "rollback" (limpa o banco) depois de cada teste
 @Test
    @Transactional 
    void testCreateQuestao_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        
        // **CORREÇÃO**: O DTO de Questão deve conter um DTO de Alternativa
        
        // 1. Crie o DTO da alternativa (o payload JSON)
        // (Estou assumindo que seu DTO tem os setters 'setAlternativa' e 'setAlternativaCorreta')
        AlternativasRequestDto alt1Dto = new AlternativasRequestDto();
        alt1Dto.setAlternativa("Alternativa de teste A");
        alt1Dto.setAlternativaCorreta(true);

        // 2. Crie o Request DTO da Questão e adicione o DTO da alternativa
        QuestoesRequestDto requestDto = new QuestoesRequestDto();
        requestDto.setEnunciado("Este é um enunciado de teste de integração?");
        
        // Agora isso é uma List<AlternativasRequestDto>, que é o tipo correto
        requestDto.setAlternativas(List.of(alt1Dto)); // <-- CORRIGIDO


        // --- 2. Act (Agir) ---
        mockMvc.perform(post("/questoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // Agora deve retornar 201
                .andExpect(jsonPath("$.idQuestao").exists())
                .andExpect(jsonPath("$.enunciado").value("Este é um enunciado de teste de integração?"));

        // --- 3. Assert (Verificar no Banco de Dados) ---
        List<Questoes> questoesNoBanco = questoesRepository.findAll();
        
        assertEquals(1, questoesNoBanco.size());
        assertEquals("Este é um enunciado de teste de integração?", questoesNoBanco.get(0).getEnunciado());
        
        // O ModelMapper (real) e o Service (real) devem ter salvo a alternativa
        assertEquals(1, questoesNoBanco.get(0).getAlternativas().size());
        assertEquals("Alternativa de teste A", questoesNoBanco.get(0).getAlternativas().get(0).getAlternativa());
    }
    @Test
    @Transactional
    void testDeleteQuestao_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        
        // **CORREÇÃO**: Precisamos salvar uma entidade válida
        
        // 1. Crie a Questão
        Questoes questaoParaSalvar = new Questoes();
        questaoParaSalvar.setEnunciado("Item para deletar");
        
        // 2. Crie a Alternativa
        Alternativas alt1 = new Alternativas();
        alt1.setAlternativa("Alternativa para deletar");
        alt1.setAlternativaCorreta(false);

        // 3. FAÇA A LIGAÇÃO (Bind Reverso e Forward)
        // Isso é crucial para o JPA salvar a relação
        alt1.setQuestoes(questaoParaSalvar); 
        questaoParaSalvar.setAlternativas(List.of(alt1));
        
        // 4. Salve (Agora é válido)
        Questoes questaoSalva = questoesRepository.save(questaoParaSalvar);
        Long idParaDeletar = questaoSalva.getIdQuestao();

        // Garante que o item existe antes de deletar
        assertEquals(1, questoesRepository.count());

        // --- 2. Act (Agir) ---
        mockMvc.perform(delete("/questoes/{id}", idParaDeletar))
                .andExpect(status().isNoContent());

        // --- 3. Assert (Verificar no Banco de Dados) ---
        assertEquals(0, questoesRepository.count());
        assertFalse(questoesRepository.findById(idParaDeletar).isPresent());
    }
@Test
    @Transactional 
    void testGetAllQuestoes_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        // Temos que salvar 2 questões VÁLIDAS (com alternativas)
        // no banco H2 antes de chamar a API.

        // Questão 1
        Questoes questao1 = new Questoes();
        questao1.setEnunciado("Primeira Questão de Teste");
        Alternativas alt1 = new Alternativas();
        alt1.setAlternativa("Alt 1");
        alt1.setAlternativaCorreta(true);
        alt1.setQuestoes(questao1); // Ligação
        questao1.setAlternativas(List.of(alt1)); // Ligação

        // Questão 2
        Questoes questao2 = new Questoes();
        questao2.setEnunciado("Segunda Questão de Teste");
        Alternativas alt2 = new Alternativas();
        alt2.setAlternativa("Alt 2");
        alt2.setAlternativaCorreta(false);
        alt2.setQuestoes(questao2); // Ligação
        questao2.setAlternativas(List.of(alt2)); // Ligação

        // Salva as duas no repositório REAL
        questoesRepository.saveAll(List.of(questao1, questao2));

        // Verifica o setup (garante que temos 2 itens no banco)
        assertEquals(2, questoesRepository.count());

        // --- 2. Act (Agir) ---
        mockMvc.perform(get("/questoes"))
        
        // --- 3. Assert (Verificar) ---
                .andExpect(status().isOk()) // Espera um HTTP 200
                // Verifica os campos de paginação (usando 'page' e 'size')
                .andExpect(jsonPath("$.page").value(0)) 
                .andExpect(jsonPath("$.size").value(20)) // 20 é o tamanho padrão do Pageable
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                // Verifica se o array 'content' tem 2 itens
                .andExpect(jsonPath("$.content.length()").value(2))
                // Opcional: verifica os dados
                .andExpect(jsonPath("$.content[0].enunciado").value("Primeira Questão de Teste"))
                .andExpect(jsonPath("$.content[1].enunciado").value("Segunda Questão de Teste"));
    }

    @Test
    @Transactional
    void testUpdateQuestao_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        // 1. Salva uma entidade VÁLIDA no banco H2
        Questoes questaoAntiga = new Questoes();
        questaoAntiga.setEnunciado("Enunciado Antigo");
        
        Alternativas alt = new Alternativas();
        alt.setAlternativa("Alternativa");
        alt.setAlternativaCorreta(true);
        alt.setQuestoes(questaoAntiga);

        // --- A CORREÇÃO ESTÁ AQUI ---
        // Não use List.of() para coleções de Entidades
        List<Alternativas> listaMutavel = new ArrayList<>();
        listaMutavel.add(alt);
        questaoAntiga.setAlternativas(listaMutavel);
        // --- FIM DA CORREÇÃO ---

        Questoes questaoSalva = questoesRepository.save(questaoAntiga);
        Long idParaAtualizar = questaoSalva.getIdQuestao();

        // 2. Cria o DTO de atualização (o "body" do PATCH)
        QuestoesUpdateDto updateDto = new QuestoesUpdateDto();
        updateDto.setEnunciado(Optional.of("Enunciado Novo Atualizado"));

        // --- 2. Act (Agir) ---
        mockMvc.perform(patch("/questoes/{id}", idParaAtualizar)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        
        // --- 3. Assert (Verificar) ---
                .andDo(print()) // Pode deixar para debugar
                .andExpect(status().isOk()); // Agora deve ser 200

        // 3b. Assert (Verificar no Banco de Dados)
        Optional<Questoes> questaoDoBanco = questoesRepository.findById(idParaAtualizar);
        assertTrue(questaoDoBanco.isPresent());
        assertEquals("Enunciado Novo Atualizado", questaoDoBanco.get().getEnunciado());
    }

    /**
     * Teste de Integração "Sad Path" para PATCH /questoes/{id} (Não Encontrado)
     * Cenário: Tenta atualizar uma questão com um ID que não existe.
     */
    @Test
    @Transactional
    void testUpdateQuestao_NotFound() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long idInexistente = 999L;
        
        QuestoesUpdateDto updateDto = new QuestoesUpdateDto();
        updateDto.setEnunciado(Optional.of("Não importa, o ID não existe"));

        // --- 2. Act (Agir) & 3. Assert (Verificar) ---
        mockMvc.perform(patch("/questoes/{id}", idInexistente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound()); // Espera um HTTP 404
    }

    /**
     * Teste de Integração "Sad Path" para DELETE /questoes/{id} (Não Encontrado)
     * Cenário: Tenta deletar uma questão com um ID que não existe.
     */
    @Test
    @Transactional
    void testDeleteQuestao_NotFound() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        Long idInexistente = 999L;

        // --- 2. Act (Agir) & 3. Assert (Verificar) ---
        mockMvc.perform(delete("/questoes/{id}", idInexistente))
                .andExpect(status().isNotFound()); // Espera um HTTP 404
    }

    /**
     * Teste de Integração "Sad Path" para POST /questoes (Validação)
     * Cenário: Tenta criar uma questão com dados inválidos (ex: enunciado nulo)
     * (Este teste assume que você tem @NotBlank ou @NotNull no 'enunciado' 
     * do seu QuestoesRequestDto)
     */
    @Test
    @Transactional
    void testCreateQuestao_ValidationFails() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        
        // 1. Crie um DTO de alternativa (pois as alternativas são obrigatórias)
        AlternativasRequestDto altDto = new AlternativasRequestDto();
        altDto.setAlternativa("Alternativa válida");
        altDto.setAlternativaCorreta(true);

        // 2. Crie um DTO de Questão INVÁLIDO (enunciado nulo)
        QuestoesRequestDto requestDtoInvalido = new QuestoesRequestDto();
        requestDtoInvalido.setEnunciado(null); // <-- O PROBLEMA
        requestDtoInvalido.setAlternativas(List.of(altDto)); // Lista de alternativas está OK

        // --- 2. Act (Agir) & 3. Assert (Verificar) ---
        mockMvc.perform(post("/questoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDtoInvalido)))
                .andExpect(status().isBadRequest()); // Espera um HTTP 400
        
        // 3b. Assert (Verificar no Banco de Dados)
        // Garante que NADA foi salvo no banco
        assertEquals(0, questoesRepository.count());
    }

    
}
