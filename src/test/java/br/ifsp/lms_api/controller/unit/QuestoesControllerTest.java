package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.controller.QuestoesController;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.questoesDto.QuestoesRequestDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesUpdateDto;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
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
    void testCreateQuestao_Success() throws Exception {
        QuestoesRequestDto requestDto = new QuestoesRequestDto();
        requestDto.setEnunciado("Qual a capital do Brasil?");

        when(questoesService.createQuestao(any(QuestoesRequestDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(post("/questoes") 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))) 
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.idQuestao").value(1L))
                .andExpect(jsonPath("$.enunciado").value("Qual a capital do Brasil?"));
        
        verify(questoesService, times(1)).createQuestao(any(QuestoesRequestDto.class));
    }

   @Test
    void testGetAllQuestoes_Success() throws Exception {
        PagedResponse<QuestoesResponseDto> pagedResponse = mock(PagedResponse.class);

        List<QuestoesResponseDto> content = List.of(responseDto);

        when(pagedResponse.getContent()).thenReturn(content);
        when(pagedResponse.getPage()).thenReturn(0);      
        when(pagedResponse.getSize()).thenReturn(10);
        when(pagedResponse.getTotalElements()).thenReturn(1L);
        when(pagedResponse.getTotalPages()).thenReturn(1);
        
        when(questoesService.getAllQuestoes(any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/questoes")) 
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.content[0].idQuestao").value(1L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
        
        verify(questoesService, times(1)).getAllQuestoes(any(Pageable.class));
    }

     }
