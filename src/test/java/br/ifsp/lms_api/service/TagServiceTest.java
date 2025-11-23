package br.ifsp.lms_api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import br.ifsp.lms_api.dto.TagDto.TagRequestDto;
import br.ifsp.lms_api.dto.TagDto.TagResponseDto;
import br.ifsp.lms_api.dto.TagDto.TagUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Tag;
import br.ifsp.lms_api.repository.TagRepository;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private TagService tagService;

    @Test
    void createTag_Success() {
        TagRequestDto requestDto = new TagRequestDto();
        requestDto.setNome("Java");

        Tag tag = new Tag();
        tag.setNome("Java");

        Tag savedTag = new Tag();
        savedTag.setIdTag(1L);
        savedTag.setNome("Java");

        TagResponseDto responseDto = new TagResponseDto();
        responseDto.setIdTag(1L);
        responseDto.setNome("Java");

        when(modelMapper.map(requestDto, Tag.class)).thenReturn(tag);
        when(tagRepository.save(tag)).thenReturn(savedTag);
        when(modelMapper.map(savedTag, TagResponseDto.class)).thenReturn(responseDto);

        TagResponseDto result = tagService.createTag(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getIdTag());
        assertEquals("Java", result.getNome());
        verify(tagRepository).save(tag);
    }

    @Test
    void getAllTags_Success() {
        Pageable pageable = Pageable.unpaged();
        Page<Tag> page = new PageImpl<>(Collections.emptyList());

        PagedResponse<TagResponseDto> pagedResponse = new PagedResponse<>(
            Collections.emptyList(), 0, 10, 0, 0, true
        );

        when(tagRepository.findAll(pageable)).thenReturn(page);
        when(pagedResponseMapper.toPagedResponse(page, TagResponseDto.class)).thenReturn(pagedResponse);

        PagedResponse<TagResponseDto> result = tagService.getAllTags(pageable);

        assertNotNull(result);
        verify(tagRepository).findAll(pageable);
    }

    @Test
    void getTagById_Success() {
        Long id = 1L;
        Tag tag = new Tag();
        tag.setIdTag(id);
        TagResponseDto responseDto = new TagResponseDto();
        responseDto.setIdTag(id);

        when(tagRepository.findById(id)).thenReturn(Optional.of(tag));
        when(modelMapper.map(tag, TagResponseDto.class)).thenReturn(responseDto);

        TagResponseDto result = tagService.getTagById(id);

        assertNotNull(result);
        assertEquals(id, result.getIdTag());
    }

    @Test
    void getTagById_NotFound_ThrowsException() {
        Long id = 1L;
        when(tagRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tagService.getTagById(id));
    }

    @Test
    void updateTag_Success() {
        Long id = 1L;
        TagUpdateDto updateDto = new TagUpdateDto();
        updateDto.setNome(Optional.of("Spring"));

        Tag tag = new Tag();
        tag.setIdTag(id);
        tag.setNome("Java");

        Tag updatedTag = new Tag();
        updatedTag.setIdTag(id);
        updatedTag.setNome("Spring");

        TagResponseDto responseDto = new TagResponseDto();
        responseDto.setIdTag(id);
        responseDto.setNome("Spring");

        when(tagRepository.findById(id)).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(updatedTag);
        when(modelMapper.map(updatedTag, TagResponseDto.class)).thenReturn(responseDto);

        TagResponseDto result = tagService.updateTag(id, updateDto);

        assertNotNull(result);
        assertEquals("Spring", result.getNome());
        verify(tagRepository).save(tag);
    }

    @Test
    void updateTag_NotFound_ThrowsException() {
        Long id = 1L;
        TagUpdateDto updateDto = new TagUpdateDto();
        when(tagRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tagService.updateTag(id, updateDto));
    }

    @Test
    void deleteTag_Success() {
        Long id = 1L;
        Tag tag = new Tag();
        tag.setIdTag(id);

        when(tagRepository.findById(id)).thenReturn(Optional.of(tag));

        assertDoesNotThrow(() -> tagService.deleteTag(id));
        verify(tagRepository).delete(tag);
    }

    @Test
    void deleteTag_NotFound_ThrowsException() {
        Long id = 1L;
        when(tagRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tagService.deleteTag(id));
    }
}
