package br.ifsp.lms_api.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import br.ifsp.lms_api.controller.TagController;
import br.ifsp.lms_api.dto.TagDto.TagRequestDto;
import br.ifsp.lms_api.dto.TagDto.TagResponseDto;
import br.ifsp.lms_api.dto.TagDto.TagUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.service.TagService;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @RestControllerAdvice
    static class TestControllerAdvice {
        @ExceptionHandler(ResourceNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public void handleNotFound() {}
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new TestControllerAdvice())
                .build();
    }

    @Test
    void create_Success() throws Exception {
        TagRequestDto requestDto = new TagRequestDto();
        requestDto.setNome("Nova Tag");

        TagResponseDto responseDto = new TagResponseDto();
        responseDto.setIdTag(1L);
        responseDto.setNome("Nova Tag");

        when(tagService.createTag(any(TagRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTag").value(1L))
                .andExpect(jsonPath("$.nome").value("Nova Tag"));
    }

    @Test
    void getAll_Success() throws Exception {
        PagedResponse<TagResponseDto> pagedResponse = new PagedResponse<>(
            Collections.emptyList(), 0, 10, 0, 0, true
        );

        when(tagService.getAllTags(any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/tags")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getById_Success() throws Exception {
        Long id = 1L;
        TagResponseDto responseDto = new TagResponseDto();
        responseDto.setIdTag(id);
        responseDto.setNome("Tag Teste");

        when(tagService.getTagById(id)).thenReturn(responseDto);

        mockMvc.perform(get("/tags/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTag").value(1L));
    }

    @Test
    void getById_WhenNotFound_ShouldReturn404() throws Exception {
        Long id = 99L;
        when(tagService.getTagById(id)).thenThrow(new ResourceNotFoundException("Tag não encontrada"));

        mockMvc.perform(get("/tags/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_Success() throws Exception {
        Long id = 1L;
        TagUpdateDto updateDto = new TagUpdateDto();
        updateDto.setNome(Optional.of("Tag Atualizada"));

        TagResponseDto responseDto = new TagResponseDto();
        responseDto.setIdTag(id);
        responseDto.setNome("Tag Atualizada");

        when(tagService.updateTag(eq(id), any(TagUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/tags/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Tag Atualizada"));
    }

    @Test
    void update_WhenNotFound_ShouldReturn404() throws Exception {
        Long id = 99L;
        TagUpdateDto updateDto = new TagUpdateDto();

        when(tagService.updateTag(eq(id), any(TagUpdateDto.class)))
                .thenThrow(new ResourceNotFoundException("Tag não encontrada"));

        mockMvc.perform(patch("/tags/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_Success() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/tags/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_WhenNotFound_ShouldReturn404() throws Exception {
        Long id = 99L;
        doThrow(new ResourceNotFoundException("Tag não encontrada"))
                .when(tagService).deleteTag(id);

        mockMvc.perform(delete("/tags/{id}", id))
                .andExpect(status().isNotFound());
    }
}
