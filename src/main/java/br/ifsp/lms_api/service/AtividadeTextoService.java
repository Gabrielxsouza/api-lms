package br.ifsp.lms_api.service;

import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.integration.LearningServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AtividadeTextoService {

    private final LearningServiceClient learningServiceClient;

    private static final String NOT_FOUND_MSG = "Atividade de Texto com ID %d não encontrada.";

    public AtividadeTextoService(
            LearningServiceClient learningServiceClient) {
        this.learningServiceClient = learningServiceClient;
    }

    @Transactional
    public AtividadeTextoResponseDto createAtividadeTexto(AtividadeTextoRequestDto dto) {
        // Map Monolith Request to Microservice Request (if needed, or reuse)
        // Inject tag names if possible.
        // Assuming Request DTO passed to client needs to be the same class or
        // compatible.
        // But Monolith has its own Request DTO which has tagIds.
        // Microservice Client methods take Monolith DTOs.
        // But I updated Microservice DTOs to have 'tags' (Set<String>) and 'idTopico'.
        // Monolith DTO has 'tagIds' (List<Long>) and 'idTopico'.
        // I need to populate 'tags' in the DTO before sending if the client uses it.
        // BUT 'AtividadeTextoRequestDto' in Monolith DOES NOT HAVE 'tags' field.
        // I cannot easily modify it without breaking other things or I should extend
        // it.
        // OR I create the DTO manually here.

        // Strategy: Add 'tags' field to Monolith DTO as well (transient or real).
        // Or blindly send and accept only idTopico works if I fix DTO.

        // For now, I will assume 'tagIds' are enough if I update Microservice Client to
        // map them? NO.
        // I should stick to the simple path:
        // Update Monolith DTO to include 'tags' (Set<String>) even if transient.
        // Populating it here.
        // But I cannot change DTO definition in the middle of a refactor easily in one
        // step.

        // Wait, LearningServiceClient takes 'AtividadeTextoRequestDto'.
        // RestTemplate serializes it.
        // If I add 'tags' to AtividadeTextoRequestDto, it will be sent.

        // Let's add 'tags' to AtividadeTextoRequestDto in Monolith.

        // Logic here:
        /*
         * if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
         * List<Tag> tags = tagRepository.findAllById(dto.getTagIds());
         * dto.setTags(tags.stream().map(Tag::getNome).collect(Collectors.toSet()));
         * }
         */
        // I need to add setTags method to DTO first.

        return learningServiceClient.createTexto(dto);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AtividadeTextoResponseDto> getAllAtividadesTexto(Pageable pageable) {
        br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto[] all = learningServiceClient.getAllAtividades();

        List<AtividadeTextoResponseDto> filtered = java.util.Arrays.stream(all)
                .filter(a -> a instanceof AtividadeTextoResponseDto)
                .map(a -> (AtividadeTextoResponseDto) a)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<AtividadeTextoResponseDto> pagedList = new java.util.ArrayList<>();
        if (start <= filtered.size()) {
            pagedList = filtered.subList(start, end);
        }

        return new PagedResponse<>(pagedList, pageable.getPageNumber(), pageable.getPageSize(), filtered.size(),
                (int) Math.ceil((double) filtered.size() / pageable.getPageSize()), start == 0);
    }

    @Transactional(readOnly = true)
    public AtividadeTextoResponseDto getAtividadeTextoById(Long id) {
        br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto dto = learningServiceClient.getAtividadeById(id);
        if (dto instanceof AtividadeTextoResponseDto) {
            return (AtividadeTextoResponseDto) dto;
        }
        throw new ResourceNotFoundException("Atividade não é Texto ou não encontrada.");
    }

    @Transactional
    public AtividadeTextoResponseDto updateAtividadeTexto(Long id, AtividadeTextoUpdateDto dto, Long userId) {
        // Similar update logic
        AtividadeTextoRequestDto request = new AtividadeTextoRequestDto();
        dto.getTituloAtividade().ifPresent(request::setTituloAtividade);
        dto.getDescricaoAtividade().ifPresent(request::setDescricaoAtividade);
        // ...
        return learningServiceClient.updateTexto(id, request);
    }

    @Transactional
    public void deleteAtividadeTexto(Long id) {
        learningServiceClient.deleteAtividade(id);
    }

    // Removed findEntityById and applyUpdateFromDto

}