package br.ifsp.lms_api.controller.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import br.ifsp.lms_api.dto.alternativasDto.AlternativasRequestDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesRequestDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesUpdateDto;
import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.repository.QuestoesRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc 
@ActiveProfiles("test") 
@Transactional
class QuestoesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuestoesRepository questoesRepository; 

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("DELETE FROM questao_tags").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM questionario_questoes").executeUpdate();
        entityManager.createQuery("DELETE FROM Alternativas").executeUpdate();
        questoesRepository.deleteAll();
    }

    @Test
    void testCreateQuestao_Success() throws Exception {
        AlternativasRequestDto alt1Dto = new AlternativasRequestDto();
        alt1Dto.setAlternativa("Alternativa de teste A");
        alt1Dto.setAlternativaCorreta(true);

        QuestoesRequestDto requestDto = new QuestoesRequestDto();
        requestDto.setEnunciado("Este é um enunciado válido (mais de 5 chars)"); 
        requestDto.setAlternativas(List.of(alt1Dto)); 

        mockMvc.perform(post("/questoes")
                .with(user("professor").roles("PROFESSOR")) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.idQuestao").exists());
    }

    @Test
    void testDeleteQuestao_Success() throws Exception {
        Questoes questao = new Questoes();
        questao.setEnunciado("Enunciado válido para deletar"); 
        
        Alternativas alt1 = new Alternativas();
        alt1.setAlternativa("Alternativa para deletar");
        alt1.setAlternativaCorreta(false);
        alt1.setQuestoes(questao); 
        
        questao.setAlternativas(new ArrayList<>(List.of(alt1)));
        
        questao = questoesRepository.save(questao);
        Long idParaDeletar = questao.getIdQuestao();

      
        mockMvc.perform(delete("/questoes/{id}", idParaDeletar)
                .with(user("admin").roles("ADMIN"))) 
                .andExpect(status().isNoContent());

        assertFalse(questoesRepository.findById(idParaDeletar).isPresent());
    }
    
    @Test
    void testGetAllQuestoes_Success() throws Exception {
        // Questão 1
        Questoes q1 = new Questoes();
        q1.setEnunciado("Questão Um"); 
        Alternativas a1 = new Alternativas();
        a1.setAlternativa("Alt 1");
        a1.setAlternativaCorreta(true);
        a1.setQuestoes(q1);
        q1.setAlternativas(new ArrayList<>(List.of(a1)));
        questoesRepository.save(q1);

        // Questão 2
        Questoes q2 = new Questoes();
        q2.setEnunciado("Questão Dois"); 
        Alternativas a2 = new Alternativas();
        a2.setAlternativa("Alt 2");
        a2.setAlternativaCorreta(false);
        a2.setQuestoes(q2);
        q2.setAlternativas(new ArrayList<>(List.of(a2)));
        questoesRepository.save(q2);

        assertEquals(2, questoesRepository.count());

        mockMvc.perform(get("/questoes")
                .with(user("professor").roles("PROFESSOR"))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void testUpdateQuestao_Success() throws Exception {
        Questoes questao = new Questoes();
        questao.setEnunciado("Enunciado Antigo Válido");
        Alternativas alt = new Alternativas();
        alt.setAlternativa("Alt");
        alt.setAlternativaCorreta(true);
        alt.setQuestoes(questao);
        questao.setAlternativas(new ArrayList<>(List.of(alt)));
        
        questao = questoesRepository.save(questao);
        Long id = questao.getIdQuestao();

        QuestoesUpdateDto updateDto = new QuestoesUpdateDto();
        updateDto.setEnunciado(Optional.of("Enunciado Novo Atualizado"));

        mockMvc.perform(patch("/questoes/{id}", id)
                .with(user("professor").roles("PROFESSOR")) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk()); 

        entityManager.flush();
        entityManager.clear();

        Questoes qAtualizada = questoesRepository.findById(id).get();
        assertEquals("Enunciado Novo Atualizado", qAtualizada.getEnunciado());
    }

    @Test
    void testUpdateQuestao_NotFound() throws Exception {
        Long idInexistente = 999L;
        QuestoesUpdateDto updateDto = new QuestoesUpdateDto();
        updateDto.setEnunciado(Optional.of("Enunciado qualquer"));

        mockMvc.perform(patch("/questoes/{id}", idInexistente)
                .with(user("professor").roles("PROFESSOR")) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound()); 
    }

    @Test
    void testDeleteQuestao_NotFound() throws Exception {
        Long idInexistente = 999L;

        // Se der 403 aqui, mude para "ADMIN"
        mockMvc.perform(delete("/questoes/{id}", idInexistente)
                .with(user("admin").roles("ADMIN"))) 
                .andExpect(status().isNotFound()); 
    }

    @Test
    void testCreateQuestao_ValidationFails() throws Exception {
        AlternativasRequestDto altDto = new AlternativasRequestDto();
        altDto.setAlternativa("Válida");
        altDto.setAlternativaCorreta(true);

        QuestoesRequestDto requestDto = new QuestoesRequestDto();
        requestDto.setEnunciado(null); 
        requestDto.setAlternativas(List.of(altDto)); 

        mockMvc.perform(post("/questoes")
                .with(user("professor").roles("PROFESSOR")) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest()); 
    }
}