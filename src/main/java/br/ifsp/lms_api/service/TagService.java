package br.ifsp.lms_api.service;

import br.ifsp.lms_api.model.Tag;
import br.ifsp.lms_api.repository.TagRepository;
import br.ifsp.lms_api.dto.TagDto.TagRequestDto;
import br.ifsp.lms_api.dto.TagDto.TagResponseDto;
import br.ifsp.lms_api.dto.TagDto.TagUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    private static final String NOT_FOUND_MSG = "Tag com ID %d não encontrada.";

    public TagService(TagRepository tagRepository,
                      ModelMapper modelMapper,
                      PagedResponseMapper pagedResponseMapper) {
        this.tagRepository = tagRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional
    public TagResponseDto createTag(TagRequestDto dto) {
        Tag tag = modelMapper.map(dto, Tag.class);
        tag.setIdTag(null);
        Tag savedTag = tagRepository.save(tag);
        return modelMapper.map(savedTag, TagResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TagResponseDto> getAllTags(Pageable pageable) {
        Page<Tag> tagPage = tagRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(tagPage, TagResponseDto.class);
    }

    @Transactional(readOnly = true)
    public TagResponseDto getTagById(Long id) {
        Tag tag = findEntityById(id);
        return modelMapper.map(tag, TagResponseDto.class);
    }

    @Transactional
    public TagResponseDto updateTag(Long id, TagUpdateDto dto) {
        Tag tag = findEntityById(id);
        
        dto.getNome().ifPresent(tag::setNome);

        Tag updatedTag = tagRepository.save(tag);
        return modelMapper.map(updatedTag, TagResponseDto.class);
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = findEntityById(id);
        tagRepository.delete(tag);
    }

    private Tag findEntityById(Long id) {
        return tagRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(NOT_FOUND_MSG, id)));
    }
}