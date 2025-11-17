package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.controller.AtividadeTextoController;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.AtividadeTextoService;

@WebMvcTest(AtividadeTextoController.class)
class AtividadeTextoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AtividadeTextoService atividadeTextoService;

    private AtividadeTextoResponseDto responseDto;

    @BeforeEach
    void setUp() {
        responseDto = new AtividadeTextoResponseDto();
        responseDto.setIdAtividade(1L);
        responseDto.setTituloAtividade("Teste de Atividade de Texto");
        responseDto.setNumeroMaximoCaracteres(500L);

        objectMapper.findAndRegisterModules();
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    void testCreate_Success() throws Exception {
        AtividadeTextoRequestDto requestDto = new AtividadeTextoRequestDto();
        requestDto.setTituloAtividade("Nova Atividade");
        requestDto.setDataInicioAtividade(LocalDate.now());
        requestDto.setDataFechamentoAtividade(LocalDate.now().plusDays(7));
        requestDto.setStatusAtividade(true);
        requestDto.setNumeroMaximoCaracteres(500L);

        when(atividadeTextoService.createAtividadeTexto(any(AtividadeTextoRequestDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(post("/atividades-texto")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAtividade").value(1L))
                .andExpect(jsonPath("$.tituloAtividade").value("Teste de Atividade de Texto"));

        verify(atividadeTextoService, times(1)).createAtividadeTexto(any(AtividadeTextoRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    void testGetAll_Success() throws Exception {
        PagedResponse<AtividadeTextoResponseDto> pagedResponse = mock(PagedResponse.class);
        List<AtividadeTextoResponseDto> content = List.of(responseDto);

        when(pagedResponse.getContent()).thenReturn(content);
        when(pagedResponse.getPage()).thenReturn(0);
        when(pagedResponse.getSize()).thenReturn(10);
        when(pagedResponse.getTotalElements()).thenReturn(1L);

        when(atividadeTextoService.getAllAtividadesTexto(any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/atividades-texto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idAtividade").value(1L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(atividadeTextoService, times(1)).getAllAtividadesTexto(any(Pageable.class));
    }

    @Test
    void testUpdate_Success() throws Exception {
        Long id = 1L;
        Long idProfessor = 50L;

        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(java.util.Optional.of("Título Atualizado"));
        updateDto.setNumeroMaximoCaracteres(java.util.Optional.of(1000L));

        CustomUserDetails userDetailsMock = mock(CustomUserDetails.class);
        when(userDetailsMock.getId()).thenReturn(idProfessor);
        when(userDetailsMock.getUsername()).thenReturn("professor_teste");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_PROFESSOR"))).when(userDetailsMock).getAuthorities();

        when(atividadeTextoService.updateAtividadeTexto(eq(id), any(AtividadeTextoUpdateDto.class), eq(idProfessor)))
            .thenReturn(responseDto);

        mockMvc.perform(patch("/atividades-texto/{id}", id)
                .with(csrf())
                .with(user(userDetailsMock))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAtividade").value(1L));

        verify(atividadeTextoService, times(1)).updateAtividadeTexto(eq(id), any(AtividadeTextoUpdateDto.class), eq(idProfessor));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        Long idInexistente = 99L;
        Long idProfessor = 50L;

        AtividadeTextoUpdateDto updateDto = new AtividadeTextoUpdateDto();
        updateDto.setTituloAtividade(java.util.Optional.of("Título Atualizado"));

        String errorMessage = String.format("Atividade de Texto com ID %d não encontrada.", idInexistente);

        CustomUserDetails userDetailsMock = mock(CustomUserDetails.class);
        when(userDetailsMock.getId()).thenReturn(idProfessor);
        when(userDetailsMock.getUsername()).thenReturn("professor_teste");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_PROFESSOR"))).when(userDetailsMock).getAuthorities();

        when(atividadeTextoService.updateAtividadeTexto(eq(idInexistente), any(AtividadeTextoUpdateDto.class), eq(idProfessor)))
            .thenThrow(new ResourceNotFoundException(errorMessage));

        mockMvc.perform(patch("/atividades-texto/{id}", idInexistente)
                .with(csrf())
                .with(user(userDetailsMock))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(atividadeTextoService, times(1)).updateAtividadeTexto(eq(idInexistente), any(AtividadeTextoUpdateDto.class), eq(idProfessor));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDelete_Success() throws Exception {
        Long id = 1L;

        doNothing().when(atividadeTextoService).deleteAtividadeTexto(id);

        mockMvc.perform(delete("/atividades-texto/{id}", id)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(atividadeTextoService, times(1)).deleteAtividadeTexto(id);
    }
}
