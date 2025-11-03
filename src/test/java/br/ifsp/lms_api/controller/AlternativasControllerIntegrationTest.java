package br.ifsp.lms_api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.Questoes; 
import br.ifsp.lms_api.repository.AlternativasRepository;
import br.ifsp.lms_api.repository.QuestoesRepository; 
import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
public class AlternativasControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    
    @Autowired
    private AlternativasRepository alternativasRepository;

    @Autowired
    private QuestoesRepository questoesRepository; 

    private Questoes questaoExistente;
    private Alternativas alternativaExistente;

    @BeforeEach
    void setUp() {
        alternativasRepository.deleteAll();
        questoesRepository.deleteAll();

        
        Questoes questao = new Questoes();
        questao.setEnunciado("Quanto é 2 + 2?");

        Alternativas alternativa = new Alternativas();
        alternativa.setAlternativa("A resposta é 4.");
        alternativa.setAlternativaCorreta(true);

        
        alternativa.setQuestoes(questao);
        questao.getAlternativas().add(alternativa);

        
        questoesRepository.save(questao);

        
        entityManager.flush();
        entityManager.clear();

        
        alternativaExistente = alternativasRepository.findAll().get(0);
        questaoExistente = questoesRepository.findAll().get(0);
    }

    @Test
    void testCreate_Success() throws Exception {
        
        AlternativasRequestDto requestDto = new AlternativasRequestDto();
        requestDto.setAlternativa("A resposta é 5.");
        requestDto.setAlternativaCorreta(false);
        
        requestDto.setIdQuestao(questaoExistente.getIdQuestao());

        
        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAlternativa").exists())
                .andExpect(jsonPath("$.alternativa", is("A resposta é 5.")))
                .andExpect(jsonPath("$.alternativaCorreta", is(false)));

     
        assertTrue(alternativasRepository.count() == 2);
    }

    @Test
    void testCreate_InvalidInput_ReturnsBadRequest() throws Exception {
    
        AlternativasRequestDto requestDto = new AlternativasRequestDto();
        requestDto.setAlternativa("Ops"); 
        requestDto.setAlternativaCorreta(false);
        requestDto.setIdQuestao(questaoExistente.getIdQuestao());

       
        mockMvc.perform(post("/alternativas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());


        assertTrue(alternativasRepository.count() == 1);
    }

    @Test
    void testGetAll_Success() throws Exception {

        mockMvc.perform(get("/alternativas")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(
                        jsonPath("$.content[0].idAlternativa", is(alternativaExistente.getIdAlternativa().intValue())));
    }

    @Test
    void testGetById_Success() throws Exception {
      
        Long id = alternativaExistente.getIdAlternativa();

     
        mockMvc.perform(get("/alternativas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlternativa", is(id.intValue())))
                .andExpect(jsonPath("$.alternativa", is("A resposta é 4.")));
    }

    @Test
    void testGetById_NotFound() throws Exception {

        Long idQueNaoExiste = 999L;

 
        mockMvc.perform(get("/alternativas/{id}", idQueNaoExiste))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdate_Success() throws Exception {

        Long id = alternativaExistente.getIdAlternativa();
        String updateJson = """
                {
                    "alternativa": "Alternativa Atualizada",
                    "alternativaCorreta": false
                }
                """;

        mockMvc.perform(patch("/alternativas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
               
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlternativa", is(id.intValue())))
                .andExpect(jsonPath("$.alternativa", is("Alternativa Atualizada")))
                .andExpect(jsonPath("$.alternativaCorreta", is(false)));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
       
        Long idQueNaoExiste = 999L;
        String updateJson = """
                {
                    "alternativa": "Alternativa Fantasma"
                }
                """;

      
        mockMvc.perform(patch("/alternativas/{id}", idQueNaoExiste)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
              
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete_Success() throws Exception {
 
        assertNotNull(alternativaExistente, "Falha no setUp: alternativaExistente é nula");
        Long id = alternativaExistente.getIdAlternativa();
        assertNotNull(id, "Falha no setUp: ID da alternativa é nulo");

        
        assertTrue(alternativasRepository.findById(id).isPresent(),
                "Alternativa não foi encontrada no banco ANTES do delete");

        mockMvc.perform(delete("/alternativas/{id}", id))
             
                .andExpect(status().isNoContent());

      
        assertFalse(alternativasRepository.findById(id).isPresent(),
                "Alternativa AINDA foi encontrada no banco APÓS o delete");
    }

    @Test
    void testDelete_NotFound() throws Exception {
      
        Long idQueNaoExiste = 999L;


        mockMvc.perform(delete("/alternativas/{id}", idQueNaoExiste))
              
                .andExpect(status().isNotFound());
    }
}
