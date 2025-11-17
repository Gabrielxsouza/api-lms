package br.ifsp.lms_api.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.questoesDto.QuestoesRequestDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesUpdateDto;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.model.Tag;
import br.ifsp.lms_api.repository.QuestoesRepository;
import br.ifsp.lms_api.repository.TagRepository;

@Service
public class QuestoesService {
    
    private final QuestoesRepository questoesRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;
    private final TagRepository tagRepository;

    public QuestoesService(QuestoesRepository questoesRepository, ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper, TagRepository tagRepository) {
        this.questoesRepository = questoesRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
        this.tagRepository = tagRepository;
    }

    public QuestoesResponseDto createQuestao(QuestoesRequestDto questaoRequestDto) {
        Questoes questao = modelMapper.map(questaoRequestDto, Questoes.class);
        questao.setIdQuestao(null);
            if (questao.getAlternativas() != null) {
                for (Alternativas alternativa : questao.getAlternativas()){
                    alternativa.setQuestoes(questao);
                }
            }

            if (questaoRequestDto.getTagIds() != null && !questaoRequestDto.getTagIds().isEmpty()) {
                List<Tag> tags = tagRepository.findAllById(questaoRequestDto.getTagIds());
                questao.setTags(new HashSet<>(tags));
            }
        Questoes savedQuestao = questoesRepository.save(questao);
        return modelMapper.map(savedQuestao, QuestoesResponseDto.class);
    }

    public PagedResponse<QuestoesResponseDto> getAllQuestoes(Pageable pageable, String tagNome, String palavraChave) {
        Specification<Questoes> spec = (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (palavraChave != null && !palavraChave.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("enunciado")),
                    "%" + palavraChave.toLowerCase() + "%"
                ));
            }

            if (tagNome != null && !tagNome.isEmpty()) {
                Join<Questoes, Tag> tagJoin = root.join("tags");
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(tagJoin.get("nome")),
                    tagNome.toLowerCase()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Questoes> questoesPage = questoesRepository.findAll(spec, pageable);

        return pagedResponseMapper.toPagedResponse(questoesPage, QuestoesResponseDto.class);
    }

    public QuestoesResponseDto updateQuestao(Long id, QuestoesUpdateDto updateDto) {
        Questoes existingQuestao = questoesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão not found with id: " + id));


        updateDto.getEnunciado().ifPresent(existingQuestao::setEnunciado);

        updateDto.getTagIds().ifPresent(tagIds -> {
            if (tagIds.isEmpty()) {
                existingQuestao.getTags().clear();
            } else {
                List<Tag> tags = tagRepository.findAllById(tagIds);
                existingQuestao.setTags(new HashSet<>(tags));
            }
        });
        Questoes updatedQuestao = questoesRepository.save(existingQuestao);
        return modelMapper.map(updatedQuestao, QuestoesResponseDto.class);
    }

    public void deleteQuestao(Long id) {
        Questoes questao = questoesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão not found with id: " + id));
        questoesRepository.delete(questao);
    }   
}
