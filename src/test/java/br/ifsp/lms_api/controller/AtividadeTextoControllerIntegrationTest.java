// Caminho: src/test/java/br/ifsp/lms_api/controller/AtividadeTextoControllerIntegrationTest.java

package br.ifsp.lms_api.controller;

// Imports do JUnit
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
@ActiveProfiles("test") // Usa o application-test.properties (H2)
class AtividadeTextoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AtividadeTextoRepository atividadeTextoRepository; // Repositório REAL

    @BeforeEach
    void setUp() {
        // Configura o ObjectMapper para lidar com datas (LocalDate)
        objectMapper.findAndRegisterModules();
    }

    /**
     * Teste de Integração para POST /atividades-texto (Caminho Feliz)
     */
    @Test
    @Transactional // Limpa o banco H2 depois do teste
    void testCreate_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        AtividadeTextoRequestDto requestDto = new AtividadeTextoRequestDto();
        requestDto.setTituloAtividade("Nova Atividade de Texto");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(5));
        requestDto.setStatusAtividade(true);
        requestDto.setNumeroMaximoCaracteres(500L);

        // --- 2. Act (Agir) ---
        mockMvc.perform(post("/atividades-texto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
        
        // --- 3. Assert (HTTP) ---
                .andExpect(status().isCreated()) // Espera 201
                .andExpect(jsonPath("$.idAtividade").exists())
                .andExpect(jsonPath("$.tituloAtividade").value("Nova Atividade de Texto"));

        // --- 3b. Assert (Banco de Dados) ---
        // Verifica se foi REALMENTE salvo no H2
        assertEquals(1, atividadeTextoRepository.count());
    }

    /**
     * Teste de Integração para POST /atividades-texto (Falha de Validação 400)
     * (Assume que 'tituloAtividade' tem @NotBlank ou @NotNull no DTO)
     */
    @Test
    @Transactional
    void testCreate_ValidationFails_400() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        AtividadeTextoRequestDto requestDtoInvalido = new AtividadeTextoRequestDto();
        requestDtoInvalido.setTituloAtividade(null); // Título nulo (inválido)
        requestDtoInvalido.setNumeroMaximoCaracteres(500L);

        // --- 2. Act (Agir) & 3. Assert (HTTP) ---
        mockMvc.perform(post("/atividades-texto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDtoInvalido)))
                .andExpect(status().isBadRequest()); // Espera 400

        // --- 3b. Assert (Banco de Dados) ---
        // Garante que NADA foi salvo
        assertEquals(0, atividadeTextoRepository.count());
    }

    /**
     * Teste de Integração para GET /atividades-texto
     */
    @Test
    @Transactional
    void testGetAll_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        // Salva 2 atividades direto no banco
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

        // Como AtividadeTexto não tem coleções, podemos usar List.of()
        // (Mas new ArrayList<>() é mais seguro)
        atividadeTextoRepository.saveAll(List.of(at1, at2));

        // --- 2. Act (Agir) ---
        mockMvc.perform(get("/atividades-texto"))
        
        // --- 3. Assert (HTTP) ---
                .andExpect(status().isOk()) // Espera 200
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].tituloAtividade").value("Atividade 1"));
    }

    /**
     * Teste de Integração para PATCH /atividades-texto/{id} (Caminho Feliz)
     */
    @Test
    @Transactional
    void testUpdate_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        // 1. Salva uma entidade no H2
        AtividadeTexto atAntiga = new AtividadeTexto();
        atAntiga.setTituloAtividade("Título Antigo");
        atAntiga.setStatusAtividade(true);
        atAntiga.setDataInicioAtividade(LocalDate.now());
        atAntiga.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        
        AtividadeTexto atSalva = atividadeTextoRepository.save(atAntiga);
        Long idParaAtualizar = atSalva.getIdAtividade();

        // 2. Cria o DTO de atualização (usando Optionals)
        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Título Novo"));
        updateDto.setNumeroMaximoCaracteres(Optional.of(999L));

        // --- 2. Act (Agir) ---
        mockMvc.perform(patch("/atividades-texto/{id}", idParaAtualizar)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        
        // --- 3. Assert (HTTP) ---
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(idParaAtualizar))
                .andExpect(jsonPath("$.tituloAtividade").value("Título Novo"));

        // --- 3b. Assert (Banco de Dados) ---
        Optional<AtividadeTexto> atDoBanco = atividadeTextoRepository.findById(idParaAtualizar);
        assertTrue(atDoBanco.isPresent());
        assertEquals("Título Novo", atDoBanco.get().getTituloAtividade());
        assertEquals(999L, atDoBanco.get().getNumeroMaximoCaracteres());
    }

    /**
     * Teste de Integração "Sad Path" para PATCH /atividades-texto/{id} (Não Encontrado)
     */
    @Test
    @Transactional
    void testUpdate_NotFound_404() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Título Novo"));

        // --- 2. Act (Agir) & 3. Assert (HTTP) ---
        mockMvc.perform(patch("/atividades-texto/{id}", 999L) // ID Inexistente
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound()); // Espera 404
    }

    /**
     * Teste de Integração para DELETE /atividades-texto/{id} (Caminho Feliz)
     */
    @Test
    @Transactional
    void testDelete_Success() throws Exception {
        // --- 1. Arrange (Arrumar) ---
        // Salva uma entidade para poder deletar
        AtividadeTexto at = new AtividadeTexto();
        at.setTituloAtividade("Para Deletar");
        at.setStatusAtividade(true);
        at.setDataInicioAtividade(LocalDate.now());
        at.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        
        AtividadeTexto atSalva = atividadeTextoRepository.save(at);
        Long idParaDeletar = atSalva.getIdAtividade();

        assertEquals(1, atividadeTextoRepository.count()); // Garante que foi salvo

        // --- 2. Act (Agir) ---
        mockMvc.perform(delete("/atividades-texto/{id}", idParaDeletar))
        
        // --- 3. Assert (HTTP) ---
                .andExpect(status().isNoContent()); // Espera 204

        // --- 3b. Assert (Banco de Dados) ---
        // Garante que foi REALMENTE deletado
        assertEquals(0, atividadeTextoRepository.count());
        assertFalse(atividadeTextoRepository.findById(idParaDeletar).isPresent());
    }
}