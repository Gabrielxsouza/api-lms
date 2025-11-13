package br.ifsp.lms_api.service;

import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.model.Tag;
import br.ifsp.lms_api.repository.AtividadeTextoRepository;
import br.ifsp.lms_api.repository.TagRepository;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;

import java.util.HashSet;
import java.util.List;


import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtividadeTextoService {

    private final AtividadeTextoRepository atividadeTextoRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;
    private final TagRepository tagRepository;

    private static final String NOT_FOUND_MSG = "Atividade de Texto com ID %d não encontrada.";

    public AtividadeTextoService(AtividadeTextoRepository atividadeTextoRepository, 
                                 ModelMapper modelMapper, 
                                 PagedResponseMapper pagedResponseMapper,
                                 TagRepository tagRepository) {
        this.atividadeTextoRepository = atividadeTextoRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
        this.tagRepository = tagRepository;
    }


    @Transactional
    public AtividadeTextoResponseDto createAtividadeTexto(AtividadeTextoRequestDto dto) {

        AtividadeTexto atividade = modelMapper.map(dto, AtividadeTexto.class);

        atividade.setIdAtividade(null);

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(dto.getTagIds());
            atividade.setTags(new HashSet<>(tags));
        }

        AtividadeTexto savedAtividade = atividadeTextoRepository.save(atividade);

        return modelMapper.map(savedAtividade, AtividadeTextoResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AtividadeTextoResponseDto> getAllAtividadesTexto(Pageable pageable) {

        Page<AtividadeTexto> atividadePage = atividadeTextoRepository.findAll(pageable);

        return pagedResponseMapper.toPagedResponse(atividadePage, AtividadeTextoResponseDto.class);
    }

    @Transactional(readOnly = true)
    public AtividadeTextoResponseDto getAtividadeTextoById(Long id) {
        AtividadeTexto atividade = findEntityById(id);
        return modelMapper.map(atividade, AtividadeTextoResponseDto.class);
    }

    @Transactional
    public AtividadeTextoResponseDto updateAtividadeTexto(Long id, AtividadeTextoUpdateDto dto) {

        AtividadeTexto atividade = findEntityById(id);

        applyUpdateFromDto(atividade, dto);

        AtividadeTexto updatedAtividade = atividadeTextoRepository.save(atividade);

        return modelMapper.map(updatedAtividade, AtividadeTextoResponseDto.class);
    }

    @Transactional
    public void deleteAtividadeTexto(Long id) {
        AtividadeTexto atividade = findEntityById(id);
        atividadeTextoRepository.delete(atividade);
    }

    private AtividadeTexto findEntityById(Long id) {
        return atividadeTextoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(NOT_FOUND_MSG, id)));
    }

    private void applyUpdateFromDto(AtividadeTexto atividade, AtividadeTextoUpdateDto dto) {
        dto.getTituloAtividade().ifPresent(atividade::setTituloAtividade);
        dto.getDescricaoAtividade().ifPresent(atividade::setDescricaoAtividade);
        dto.getDataInicioAtividade().ifPresent(atividade::setDataInicioAtividade);
        dto.getDataFechamentoAtividade().ifPresent(atividade::setDataFechamentoAtividade);
        dto.getStatusAtividade().ifPresent(atividade::setStatusAtividade);
        dto.getNumeroMaximoCaracteres().ifPresent(atividade::setNumeroMaximoCaracteres);

        dto.getTagIds().ifPresent(tagIds -> {
            if (tagIds.isEmpty()) {
                atividade.getTags().clear();
            } else {
                List<Tag> tags = tagRepository.findAllById(tagIds);
                atividade.setTags(new HashSet<>(tags));
            }
        });
    }
}