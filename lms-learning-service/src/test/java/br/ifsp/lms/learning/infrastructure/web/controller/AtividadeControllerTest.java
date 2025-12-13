package br.ifsp.lms.learning.infrastructure.web.controller;

import br.ifsp.lms.learning.application.usecase.*;
import br.ifsp.lms.learning.domain.model.AtividadeQuestionario;
import br.ifsp.lms.learning.infrastructure.web.dto.CreateQuestionarioRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AtividadeController.class)
class AtividadeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CriarAtividadeUseCase criarAtividadeUseCase;

    @MockBean
    private BuscarAtividadeUseCase buscarAtividadeUseCase;

    @MockBean
    private DeletarAtividadeUseCase deletarAtividadeUseCase;

    @MockBean
    private ListarAtividadesUseCase listarAtividadesUseCase;

    @MockBean
    private AtualizarAtividadeUseCase atualizarAtividadeUseCase;

    @Test
    void criarQuestionario_ShouldReturnCreated() throws Exception {
        CreateQuestionarioRequest request = new CreateQuestionarioRequest();
        request.setTituloAtividade("Test Quiz");
        request.setDescricaoAtividade("A simple test quiz");
        request.setDataInicioAtividade(LocalDate.now());
        request.setDataFechamentoAtividade(LocalDate.now().plusDays(1));
        request.setStatusAtividade(true);
        request.setDuracaoQuestionario(60L);
        request.setNumeroTentativas(3);
        request.setIdTopico(1L);
        request.setTags(Collections.singleton("test"));

        AtividadeQuestionario created = AtividadeQuestionario.builder()
                .id(1L)
                .titulo(request.getTituloAtividade())
                .descricao(request.getDescricaoAtividade())
                .dataInicio(request.getDataInicioAtividade())
                .dataFechamento(request.getDataFechamentoAtividade())
                .status(request.getStatusAtividade())
                .duracaoMinutes(request.getDuracaoQuestionario())
                .tentativasPermitidas(request.getNumeroTentativas())
                .topicoId(request.getIdTopico())
                .tags(request.getTags())
                .build();

        when(criarAtividadeUseCase.execute(any(AtividadeQuestionario.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/atividades/questionario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(1L))
                .andExpect(jsonPath("$.tituloAtividade").value("Test Quiz"))
                .andExpect(jsonPath("$.tipoAtividade").value("QUESTIONARIO"));
    }

    @Test
    void getAtividade_ShouldReturnAtividade() throws Exception {
        AtividadeQuestionario atividade = AtividadeQuestionario.builder()
                .id(1L)
                .titulo("Existing Quiz")
                .descricao("Description")
                .duracaoMinutes(30L)
                .tentativasPermitidas(1)
                .build();

        when(buscarAtividadeUseCase.execute(1L)).thenReturn(Optional.of(atividade));

        mockMvc.perform(get("/api/v1/atividades/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(1L))
                .andExpect(jsonPath("$.tituloAtividade").value("Existing Quiz"));
    }

    @Test
    void getAtividade_NotFound_ShouldReturn404() throws Exception {
        when(buscarAtividadeUseCase.execute(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/atividades/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAtividade_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/atividades/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllAtividades_ShouldReturnList() throws Exception {
        AtividadeQuestionario atividade = AtividadeQuestionario.builder()
                .id(1L)
                .titulo("Quiz 1")
                .build();

        when(listarAtividadesUseCase.execute()).thenReturn(List.of(atividade));

        mockMvc.perform(get("/api/v1/atividades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idAtividade").value(1L));
    }
}
