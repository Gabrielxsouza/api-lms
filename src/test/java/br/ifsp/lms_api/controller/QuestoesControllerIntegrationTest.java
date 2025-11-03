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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc 
@ActiveProfiles("test") 
class QuestoesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuestoesRepository questoesRepository; 

    @Autowired
    private AtividadeQuestionarioRepository atividadeQuestionarioRepository;

 @Test
    @Transactional 
    void testCreateQuestao_Success() throws Exception {
        AlternativasRequestDto alt1Dto = new AlternativasRequestDto();
        alt1Dto.setAlternativa("Alternativa de teste A");
        alt1Dto.setAlternativaCorreta(true);

        QuestoesRequestDto requestDto = new QuestoesRequestDto();
        requestDto.setEnunciado("Este é um enunciado de teste de integração?");
        
        requestDto.setAlternativas(List.of(alt1Dto)); 


        mockMvc.perform(post("/questoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.idQuestao").exists())
                .andExpect(jsonPath("$.enunciado").value("Este é um enunciado de teste de integração?"));

        List<Questoes> questoesNoBanco = questoesRepository.findAll();
        
        assertEquals(1, questoesNoBanco.size());
        assertEquals("Este é um enunciado de teste de integração?", questoesNoBanco.get(0).getEnunciado());
        
        assertEquals(1, questoesNoBanco.get(0).getAlternativas().size());
        assertEquals("Alternativa de teste A", questoesNoBanco.get(0).getAlternativas().get(0).getAlternativa());
    }
    @Test
    @Transactional
    void testDeleteQuestao_Success() throws Exception {
        Questoes questaoParaSalvar = new Questoes();
        questaoParaSalvar.setEnunciado("Item para deletar");
        
        Alternativas alt1 = new Alternativas();
        alt1.setAlternativa("Alternativa para deletar");
        alt1.setAlternativaCorreta(false);

        alt1.setQuestoes(questaoParaSalvar); 
        questaoParaSalvar.setAlternativas(List.of(alt1));
        
        Questoes questaoSalva = questoesRepository.save(questaoParaSalvar);
        Long idParaDeletar = questaoSalva.getIdQuestao();

        assertEquals(1, questoesRepository.count());

        mockMvc.perform(delete("/questoes/{id}", idParaDeletar))
                .andExpect(status().isNoContent());

        assertEquals(0, questoesRepository.count());
        assertFalse(questoesRepository.findById(idParaDeletar).isPresent());
    }
@Test
    @Transactional 
    void testGetAllQuestoes_Success() throws Exception {
        Questoes questao1 = new Questoes();
        questao1.setEnunciado("Primeira Questão de Teste");
        Alternativas alt1 = new Alternativas();
        alt1.setAlternativa("Alt 1");
        alt1.setAlternativaCorreta(true);
        alt1.setQuestoes(questao1); 
        questao1.setAlternativas(List.of(alt1)); 

        Questoes questao2 = new Questoes();
        questao2.setEnunciado("Segunda Questão de Teste");
        Alternativas alt2 = new Alternativas();
        alt2.setAlternativa("Alt 2");
        alt2.setAlternativaCorreta(false);
        alt2.setQuestoes(questao2); 
        questao2.setAlternativas(List.of(alt2)); 

        questoesRepository.saveAll(List.of(questao1, questao2));

        assertEquals(2, questoesRepository.count());

        mockMvc.perform(get("/questoes"))
        
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.page").value(0)) 
                .andExpect(jsonPath("$.size").value(20)) 
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].enunciado").value("Primeira Questão de Teste"))
                .andExpect(jsonPath("$.content[1].enunciado").value("Segunda Questão de Teste"));
    }

    @Test
    @Transactional
    void testUpdateQuestao_Success() throws Exception {
        Questoes questaoAntiga = new Questoes();
        questaoAntiga.setEnunciado("Enunciado Antigo");
        
        Alternativas alt = new Alternativas();
        alt.setAlternativa("Alternativa");
        alt.setAlternativaCorreta(true);
        alt.setQuestoes(questaoAntiga);

        List<Alternativas> listaMutavel = new ArrayList<>();
        listaMutavel.add(alt);
        questaoAntiga.setAlternativas(listaMutavel);

        Questoes questaoSalva = questoesRepository.save(questaoAntiga);
        Long idParaAtualizar = questaoSalva.getIdQuestao();

        QuestoesUpdateDto updateDto = new QuestoesUpdateDto();
        updateDto.setEnunciado(Optional.of("Enunciado Novo Atualizado"));

        mockMvc.perform(patch("/questoes/{id}", idParaAtualizar)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        
                .andDo(print()) 
                .andExpect(status().isOk()); 

        Optional<Questoes> questaoDoBanco = questoesRepository.findById(idParaAtualizar);
        assertTrue(questaoDoBanco.isPresent());
        assertEquals("Enunciado Novo Atualizado", questaoDoBanco.get().getEnunciado());
    }

    @Test
    @Transactional
    void testUpdateQuestao_NotFound() throws Exception {
        Long idInexistente = 999L;
        
        QuestoesUpdateDto updateDto = new QuestoesUpdateDto();
        updateDto.setEnunciado(Optional.of("Não importa, o ID não existe"));

        mockMvc.perform(patch("/questoes/{id}", idInexistente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound()); 
    }

    @Test
    @Transactional
    void testDeleteQuestao_NotFound() throws Exception {
        Long idInexistente = 999L;

        mockMvc.perform(delete("/questoes/{id}", idInexistente))
                .andExpect(status().isNotFound()); 
    }

    @Test
    @Transactional
    void testCreateQuestao_ValidationFails() throws Exception {
        AlternativasRequestDto altDto = new AlternativasRequestDto();
        altDto.setAlternativa("Alternativa válida");
        altDto.setAlternativaCorreta(true);

        QuestoesRequestDto requestDtoInvalido = new QuestoesRequestDto();
        requestDtoInvalido.setEnunciado(null); 
        requestDtoInvalido.setAlternativas(List.of(altDto)); 

        mockMvc.perform(post("/questoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDtoInvalido)))
                .andExpect(status().isBadRequest()); 
        
        assertEquals(0, questoesRepository.count());
    }

    
}
