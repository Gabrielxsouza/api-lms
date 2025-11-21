package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.controller.QuestoesController;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.questoesDto.QuestoesRequestDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesUpdateDto;
import br.ifsp.lms_api.service.QuestoesService;

@WebMvcTest(QuestoesController.class)
class QuestoesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuestoesService questoesService;

    private QuestoesResponseDto responseDto;

    @BeforeEach
    void setUp() {
        responseDto = new QuestoesResponseDto();
        responseDto.setIdQuestao(1L);
        responseDto.setEnunciado("Qual a capital do Brasil?");

        objectMapper.findAndRegisterModules();
    }

    @Test
    @WithMockUser(roles = "PROFESSOR") // Controller exige ROLE_PROFESSOR
    void testCreateQuestao_Success() throws Exception {
        QuestoesRequestDto requestDto = new QuestoesRequestDto();
        requestDto.setEnunciado("Qual a capital do Brasil?");

        when(questoesService.createQuestao(any(QuestoesRequestDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(post("/questoes")
                .with(csrf()) // Necessário para POST
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idQuestao").value(1L))
                .andExpect(jsonPath("$.enunciado").value("Qual a capital do Brasil?"));

        verify(questoesService, times(1)).createQuestao(any(QuestoesRequestDto.class));
    }

    @Test
@WithMockUser(roles = "PROFESSOR")
void testGetAllQuestoes_Success() throws Exception {

    PagedResponse<QuestoesResponseDto> pagedResponse = new PagedResponse<QuestoesResponseDto>(null, 0, 0, 0, 0, false);

    pagedResponse.setContent(List.of(responseDto));
    pagedResponse.setPage(0);
    pagedResponse.setSize(10);
    pagedResponse.setTotalElements(1L);
    pagedResponse.setTotalPages(1);

    when(questoesService.getAllQuestoes(any(Pageable.class), any(), any())).thenReturn(pagedResponse);

    mockMvc.perform(get("/questoes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].idQuestao").value(1L))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.totalElements").value(1));

    verify(questoesService, times(1)).getAllQuestoes(any(Pageable.class), any(), any());
}

    @Test
    @WithMockUser(roles = "PROFESSOR")
    void testUpdateQuestao_Success() throws Exception {
        Long id = 1L;
        QuestoesUpdateDto updateDto = new QuestoesUpdateDto();
        updateDto.setEnunciado(Optional.of("Enunciado Atualizado"));

        QuestoesResponseDto updatedResponse = new QuestoesResponseDto();
        updatedResponse.setIdQuestao(id);
        updatedResponse.setEnunciado("Enunciado Atualizado");

        when(questoesService.updateQuestao(eq(id), any(QuestoesUpdateDto.class)))
            .thenReturn(updatedResponse);

        mockMvc.perform(patch("/questoes/{id}", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado").value("Enunciado Atualizado"));

        verify(questoesService, times(1)).updateQuestao(eq(id), any(QuestoesUpdateDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteQuestao_Success() throws Exception {
        Long id = 1L;
        doNothing().when(questoesService).deleteQuestao(id);

        mockMvc.perform(delete("/questoes/{id}", id)
                .with(csrf()))
                .andExpect(status().isNoContent());
        verify(questoesService, times(1)).deleteQuestao(id);
    }
}
