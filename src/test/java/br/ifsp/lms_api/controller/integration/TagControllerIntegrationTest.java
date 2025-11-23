package br.ifsp.lms_api.controller.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.lms_api.config.CustomUserDetails;
import br.ifsp.lms_api.dto.TagDto.TagRequestDto;
import br.ifsp.lms_api.dto.TagDto.TagResponseDto;
import br.ifsp.lms_api.dto.TagDto.TagUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.model.Usuario;
import br.ifsp.lms_api.service.TagService;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TagControllerIntegrationTest.TestControllerAdvice.class)
class TagControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TagService tagService;

    private CustomUserDetails professorUserDetails;
    private CustomUserDetails adminUserDetails;
    private CustomUserDetails alunoUserDetails;

    @TestConfiguration
    @RestControllerAdvice
    public static class TestControllerAdvice {

        @ExceptionHandler(ResourceNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public void handleResourceNotFound() {}

        @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public void handleAccessDenied() {}
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @BeforeEach
    void setUp() {
        // Professor
        Usuario profMock = mock(Usuario.class);
        Collection authoritiesProf = Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROFESSOR"));

        lenient().when(profMock.getIdUsuario()).thenReturn(1L);
        lenient().when(profMock.getUsername()).thenReturn("prof@email.com");
        lenient().when(profMock.getPassword()).thenReturn("senha");
        lenient().when(profMock.getAuthorities()).thenReturn(authoritiesProf);
        lenient().when(profMock.isEnabled()).thenReturn(true);
        lenient().when(profMock.isAccountNonExpired()).thenReturn(true);
        lenient().when(profMock.isAccountNonLocked()).thenReturn(true);
        lenient().when(profMock.isCredentialsNonExpired()).thenReturn(true);

        professorUserDetails = new CustomUserDetails(profMock);

        // Admin
        Usuario adminMock = mock(Usuario.class);
        Collection authoritiesAdmin = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));

        lenient().when(adminMock.getIdUsuario()).thenReturn(2L);
        lenient().when(adminMock.getUsername()).thenReturn("admin@email.com");
        lenient().when(adminMock.getPassword()).thenReturn("senha");
        lenient().when(adminMock.getAuthorities()).thenReturn(authoritiesAdmin);
        lenient().when(adminMock.isEnabled()).thenReturn(true);
        lenient().when(adminMock.isAccountNonExpired()).thenReturn(true);
        lenient().when(adminMock.isAccountNonLocked()).thenReturn(true);
        lenient().when(adminMock.isCredentialsNonExpired()).thenReturn(true);

        adminUserDetails = new CustomUserDetails(adminMock);

        // Aluno
        Usuario alunoMock = mock(Usuario.class);
        Collection authoritiesAluno = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ALUNO"));

        lenient().when(alunoMock.getIdUsuario()).thenReturn(3L);
        lenient().when(alunoMock.getUsername()).thenReturn("aluno@email.com");
        lenient().when(alunoMock.getPassword()).thenReturn("senha");
        lenient().when(alunoMock.getAuthorities()).thenReturn(authoritiesAluno);
        lenient().when(alunoMock.isEnabled()).thenReturn(true);
        lenient().when(alunoMock.isAccountNonExpired()).thenReturn(true);
        lenient().when(alunoMock.isAccountNonLocked()).thenReturn(true);
        lenient().when(alunoMock.isCredentialsNonExpired()).thenReturn(true);

        alunoUserDetails = new CustomUserDetails(alunoMock);
    }

    @Test
    void create_HappyPath() throws Exception {
        TagRequestDto requestDto = new TagRequestDto();
        requestDto.setNome("Java");

        TagResponseDto responseDto = new TagResponseDto();
        responseDto.setIdTag(1L);
        responseDto.setNome("Java");

        when(tagService.createTag(any(TagRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(authentication(new UsernamePasswordAuthenticationToken(professorUserDetails, null, professorUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTag").value(1L))
                .andExpect(jsonPath("$.nome").value("Java"));
    }

    @Test
    void create_SadPath_Forbidden() throws Exception {
        TagRequestDto requestDto = new TagRequestDto();
        requestDto.setNome("Java");

        mockMvc.perform(post("/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_HappyPath() throws Exception {
        PagedResponse<TagResponseDto> pagedResponse = new PagedResponse<>(
            Collections.emptyList(), 0, 10, 0, 0, true
        );

        when(tagService.getAllTags(any(Pageable.class))).thenReturn(pagedResponse);

        // Adicionado autenticação para passar pelo filtro global de segurança
        mockMvc.perform(get("/tags")
                .param("page", "0")
                .param("size", "10")
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getById_HappyPath() throws Exception {
        Long id = 1L;
        TagResponseDto responseDto = new TagResponseDto();
        responseDto.setIdTag(id);
        responseDto.setNome("Java");

        when(tagService.getTagById(id)).thenReturn(responseDto);

        // Adicionado autenticação para passar pelo filtro global de segurança
        mockMvc.perform(get("/tags/{id}", id)
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTag").value(1L));
    }

    @Test
    void getById_SadPath_NotFound() throws Exception {
        Long id = 99L;
        when(tagService.getTagById(id)).thenThrow(new ResourceNotFoundException("Tag não encontrada"));

        // Adicionado autenticação para passar pelo filtro global de segurança
        mockMvc.perform(get("/tags/{id}", id)
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_HappyPath() throws Exception {
        Long id = 1L;
        TagUpdateDto updateDto = new TagUpdateDto();
        updateDto.setNome(Optional.of("Spring"));

        TagResponseDto responseDto = new TagResponseDto();
        responseDto.setIdTag(id);
        responseDto.setNome("Spring");

        when(tagService.updateTag(eq(id), any(TagUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/tags/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(authentication(new UsernamePasswordAuthenticationToken(professorUserDetails, null, professorUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Spring"));
    }

    @Test
    void update_SadPath_Forbidden() throws Exception {
        Long id = 1L;
        TagUpdateDto updateDto = new TagUpdateDto();

        mockMvc.perform(patch("/tags/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(authentication(new UsernamePasswordAuthenticationToken(alunoUserDetails, null, alunoUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_HappyPath() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/tags/{id}", id)
                .with(authentication(new UsernamePasswordAuthenticationToken(adminUserDetails, null, adminUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_SadPath_Forbidden() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/tags/{id}", id)
                .with(authentication(new UsernamePasswordAuthenticationToken(professorUserDetails, null, professorUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_SadPath_NotFound() throws Exception {
        Long id = 99L;

        doThrow(new ResourceNotFoundException("Tag não encontrada"))
                .when(tagService).deleteTag(id);

        mockMvc.perform(delete("/tags/{id}", id)
                .with(authentication(new UsernamePasswordAuthenticationToken(adminUserDetails, null, adminUserDetails.getAuthorities())))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
