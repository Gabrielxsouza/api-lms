package br.ifsp.lms_api.controller.integration;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoUpdateDto;
import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.repository.AtividadeTextoRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test") 
class AtividadeTextoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AtividadeTextoRepository atividadeTextoRepository; 

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    @Transactional 
    void testCreate_Success() throws Exception {
        AtividadeTextoRequestDto requestDto = new AtividadeTextoRequestDto();
        requestDto.setTituloAtividade("Nova Atividade de Texto");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(5));
        requestDto.setStatusAtividade(true);
        requestDto.setNumeroMaximoCaracteres(500L);

        mockMvc.perform(post("/atividades-texto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
        
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.idAtividade").exists())
                .andExpect(jsonPath("$.tituloAtividade").value("Nova Atividade de Texto"));

        assertEquals(1, atividadeTextoRepository.count());
    }

    @Test
    @Transactional
    void testCreate_ValidationFails_400() throws Exception {
        AtividadeTextoRequestDto requestDtoInvalido = new AtividadeTextoRequestDto();
        requestDtoInvalido.setTituloAtividade(null); 
        requestDtoInvalido.setNumeroMaximoCaracteres(500L);

        mockMvc.perform(post("/atividades-texto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDtoInvalido)))
                .andExpect(status().isBadRequest()); 

        assertEquals(0, atividadeTextoRepository.count());
    }

    @Test
    @Transactional
    void testGetAll_Success() throws Exception {
        AtividadeTexto at1 = new AtividadeTexto();
        at1.setTituloAtividade("Atividade 1");
        at1.setStatusAtividade(true);
        at1.setDataInicioAtividade(LocalDate.now());
        at1.setDataFechamentoAtividade(LocalDate.now().plusDays(1));

        AtividadeTexto at2 = new AtividadeTexto();
        at2.setTituloAtividade("Atividade 2");
        at2.setStatusAtividade(true);
        at2.setDataInicioAtividade(LocalDate.now());
        at2.setDataFechamentoAtividade(LocalDate.now().plusDays(2));

        atividadeTextoRepository.saveAll(List.of(at1, at2));

        mockMvc.perform(get("/atividades-texto"))
        
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].tituloAtividade").value("Atividade 1"));
    }

    @Test
    @Transactional
    void testUpdate_Success() throws Exception {
        AtividadeTexto atAntiga = new AtividadeTexto();
        atAntiga.setTituloAtividade("Título Antigo");
        atAntiga.setStatusAtividade(true);
        atAntiga.setDataInicioAtividade(LocalDate.now());
        atAntiga.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        
        AtividadeTexto atSalva = atividadeTextoRepository.save(atAntiga);
        Long idParaAtualizar = atSalva.getIdAtividade();

        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Título Novo"));
        updateDto.setNumeroMaximoCaracteres(Optional.of(999L));

        mockMvc.perform(patch("/atividades-texto/{id}", idParaAtualizar)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(idParaAtualizar))
                .andExpect(jsonPath("$.tituloAtividade").value("Título Novo"));

        Optional<AtividadeTexto> atDoBanco = atividadeTextoRepository.findById(idParaAtualizar);
        assertTrue(atDoBanco.isPresent());
        assertEquals("Título Novo", atDoBanco.get().getTituloAtividade());
        assertEquals(999L, atDoBanco.get().getNumeroMaximoCaracteres());
    }

    @Test
    @Transactional
    void testUpdate_NotFound_404() throws Exception {
        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Título Novo"));

        mockMvc.perform(patch("/atividades-texto/{id}", 999L) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound()); 
    }

    @Test
    @Transactional
    void testDelete_Success() throws Exception {
        AtividadeTexto at = new AtividadeTexto();
        at.setTituloAtividade("Para Deletar");
        at.setStatusAtividade(true);
        at.setDataInicioAtividade(LocalDate.now());
        at.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        
        AtividadeTexto atSalva = atividadeTextoRepository.save(at);
        Long idParaDeletar = atSalva.getIdAtividade();

        assertEquals(1, atividadeTextoRepository.count()); 

        mockMvc.perform(delete("/atividades-texto/{id}", idParaDeletar))
        
                .andExpect(status().isNoContent()); 

        assertEquals(0, atividadeTextoRepository.count());
        assertFalse(atividadeTextoRepository.findById(idParaDeletar).isPresent());
    }
}
