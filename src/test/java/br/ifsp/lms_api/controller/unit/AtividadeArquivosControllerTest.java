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
import java.util.Optional;

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
import br.ifsp.lms_api.controller.AtividadeArquivosController;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.AtividadeArquivosService;

@WebMvcTest(AtividadeArquivosController.class)
class AtividadeArquivosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AtividadeArquivosService atividadeArquivosService;

    private AtividadeArquivosResponseDto responseDto;
    private AtividadeArquivosRequestDto requestDto;
    private LocalDate dataInicio;
    private LocalDate dataFechamento;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();

        dataInicio = LocalDate.of(2025, 11, 1);
        dataFechamento = LocalDate.of(2025, 11, 30);

        responseDto = new AtividadeArquivosResponseDto();
        responseDto.setIdAtividade(1L);
        responseDto.setTituloAtividade("Trabalho de Java");
        responseDto.setDescricaoAtividade("Descricao teste");
        responseDto.setDataInicioAtividade(dataInicio);
        responseDto.setDataFechamentoAtividade(dataFechamento);
        responseDto.setStatusAtividade(true);
        responseDto.setArquivosPermitidos(List.of(".pdf", ".zip"));

        requestDto = new AtividadeArquivosRequestDto();
        requestDto.setTituloAtividade("Trabalho de Java");
        requestDto.setDescricaoAtividade("Descricao teste");
        requestDto.setDataInicioAtividade(dataInicio);
        requestDto.setDataFechamentoAtividade(dataFechamento);
        requestDto.setStatusAtividade(true);
        requestDto.setArquivosPermitidos(List.of(".pdf", ".zip"));
        requestDto.setIdTopico(10L);
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    void testCreate_Success() throws Exception {
        Long idUsuario = 1L;

        CustomUserDetails userDetailsMock = mock(CustomUserDetails.class);
        when(userDetailsMock.getId()).thenReturn(idUsuario);
        when(userDetailsMock.getUsername()).thenReturn("professor_teste");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_PROFESSOR"))).when(userDetailsMock).getAuthorities();

        when(atividadeArquivosService.createAtividadeArquivos(any(AtividadeArquivosRequestDto.class), eq(idUsuario)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/atividades-arquivo")
                .with(csrf())
                .with(user(userDetailsMock))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAtividade").value(1L));

        verify(atividadeArquivosService, times(1)).createAtividadeArquivos(any(AtividadeArquivosRequestDto.class),
                eq(idUsuario));
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    void testGetAll_Success() throws Exception {
        PagedResponse<AtividadeArquivosResponseDto> pagedResponse = new PagedResponse<>(List.of(responseDto), 0, 10, 1,
                1, true);

        when(atividadeArquivosService.getAllAtividadesArquivos(any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/atividades-arquivo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idAtividade").value(1L));

        verify(atividadeArquivosService, times(1)).getAllAtividadesArquivos(any(Pageable.class));
    }

    @Test
    void testUpdate_Success() throws Exception {
        Long id = 1L;
        Long idUsuario = 1L;

        AtividadeArquivosUpdateDto updateDto = new AtividadeArquivosUpdateDto();
        updateDto.setTituloAtividade(Optional.of("Trabalho de Java V2"));

        CustomUserDetails userDetailsMock = mock(CustomUserDetails.class);
        when(userDetailsMock.getId()).thenReturn(idUsuario);
        when(userDetailsMock.getUsername()).thenReturn("professor_teste");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_PROFESSOR"))).when(userDetailsMock).getAuthorities();

        when(atividadeArquivosService.updateAtividadeArquivos(eq(id), any(AtividadeArquivosUpdateDto.class),
                eq(idUsuario)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/atividades-arquivo/{id}", id)
                .with(csrf())
                .with(user(userDetailsMock))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        verify(atividadeArquivosService, times(1)).updateAtividadeArquivos(eq(id),
                any(AtividadeArquivosUpdateDto.class), eq(idUsuario));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        Long id = 1L;
        Long idUsuario = 1L;

        AtividadeArquivosUpdateDto updateDto = new AtividadeArquivosUpdateDto();

        CustomUserDetails userDetailsMock = mock(CustomUserDetails.class);
        when(userDetailsMock.getId()).thenReturn(idUsuario);
        when(userDetailsMock.getUsername()).thenReturn("professor_teste");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_PROFESSOR"))).when(userDetailsMock).getAuthorities();

        when(atividadeArquivosService.updateAtividadeArquivos(eq(id), any(AtividadeArquivosUpdateDto.class),
                eq(idUsuario)))
                .thenThrow(new ResourceNotFoundException("Atividade não encontrada"));

        mockMvc.perform(patch("/atividades-arquivo/{id}", id)
                .with(csrf())
                .with(user(userDetailsMock))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(atividadeArquivosService, times(1)).updateAtividadeArquivos(eq(id),
                any(AtividadeArquivosUpdateDto.class), eq(idUsuario));
    }

    @Test
    void testDelete_Success() throws Exception {
        Long id = 1L;
        Long idUsuario = 1L;

        CustomUserDetails userDetailsMock = mock(CustomUserDetails.class);
        when(userDetailsMock.getId()).thenReturn(idUsuario);
        when(userDetailsMock.getUsername()).thenReturn("professor_teste");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_PROFESSOR"))).when(userDetailsMock).getAuthorities();

        doNothing().when(atividadeArquivosService).deleteAtividadeArquivos(id, idUsuario);

        mockMvc.perform(delete("/atividades-arquivo/{id}", id)
                .with(csrf())
                .with(user(userDetailsMock)))
                .andExpect(status().isNoContent());

        verify(atividadeArquivosService, times(1)).deleteAtividadeArquivos(id, idUsuario);
    }
}