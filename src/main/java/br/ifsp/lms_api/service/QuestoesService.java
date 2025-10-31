package br.ifsp.lms_api.service;

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
import br.ifsp.lms_api.repository.QuestoesRepository;

@Service
public class QuestoesService {
    
    private final QuestoesRepository questoesRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public QuestoesService(QuestoesRepository questoesRepository, ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper) {
        this.questoesRepository = questoesRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    public QuestoesResponseDto createQuestao(QuestoesRequestDto questaoRequestDto) {
        Questoes questao = modelMapper.map(questaoRequestDto, Questoes.class);
            if (questao.getAlternativas() != null) {
                for (Alternativas alternativa : questao.getAlternativas()){
                    alternativa.setQuestoes(questao);
                }
            }
        Questoes savedQuestao = questoesRepository.save(questao);
        return modelMapper.map(savedQuestao, QuestoesResponseDto.class);
    }

    public PagedResponse<QuestoesResponseDto> getAllQuestoes(Pageable pageable) {
        Page<Questoes> questoes = questoesRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(questoes, QuestoesResponseDto.class);
    }

    public QuestoesResponseDto updateQuestao(Long id, QuestoesUpdateDto updateDto) {
        Questoes existingQuestao = questoesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão not found with id: " + id));

        updateDto.getEnunciado().ifPresent(existingQuestao::setEnunciado);
        Questoes updatedQuestao = questoesRepository.save(existingQuestao);
        return modelMapper.map(updatedQuestao, QuestoesResponseDto.class);
    }

    public void deleteQuestao(Long id) {
        Questoes questao = questoesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão not found with id: " + id));
        questoesRepository.delete(questao);
    }   
}
