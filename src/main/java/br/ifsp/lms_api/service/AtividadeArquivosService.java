package br.ifsp.lms_api.service;


import br.ifsp.lms_api.model.AtividadeArquivos;

import br.ifsp.lms_api.model.Tag;


import java.util.List;

import br.ifsp.lms_api.repository.AtividadeArquivosRepository;
import br.ifsp.lms_api.repository.TagRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;

import java.util.HashSet;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtividadeArquivosService {

    private final AtividadeArquivosRepository atividadeArquivosRepository;
    private final TopicosRepository topicosRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;
    private final TagRepository tagRepository;

    private static final String NOT_FOUND_MSG = "Atividade de Arquivos com ID %d não encontrada.";
    private static final String TOPICO_NOT_FOUND_MSG = "Tópico com ID %d não encontrado.";

    public AtividadeArquivosService(AtividadeArquivosRepository atividadeArquivosRepository,
                                    TopicosRepository topicosRepository,
                                    ModelMapper modelMapper,
                                    PagedResponseMapper pagedResponseMapper,
                                    TagRepository tagRepository) {
        this.atividadeArquivosRepository = atividadeArquivosRepository;
        this.topicosRepository = topicosRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public AtividadeArquivosResponseDto createAtividadeArquivos(AtividadeArquivosRequestDto dto, Long idUsuarioLogado) {
        
        Topicos topico = topicosRepository.findById(dto.getIdTopico())
            .orElseThrow(() -> new ResourceNotFoundException(String.format(TOPICO_NOT_FOUND_MSG, dto.getIdTopico())));
        
        checkProfessorOwnership(topico, idUsuarioLogado);

        AtividadeArquivos atividade = modelMapper.map(dto, AtividadeArquivos.class);
        atividade.setIdAtividade(null);
        atividade.setTopico(topico);

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(dto.getTagIds());
            atividade.setTags(new HashSet<>(tags));
        }
        AtividadeArquivos savedAtividade = atividadeArquivosRepository.save(atividade);
        return modelMapper.map(savedAtividade, AtividadeArquivosResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AtividadeArquivosResponseDto> getAllAtividadesArquivos(Pageable pageable) {
        Page<AtividadeArquivos> atividadePage = atividadeArquivosRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(atividadePage, AtividadeArquivosResponseDto.class);
    }

    @Transactional(readOnly = true)
    public AtividadeArquivosResponseDto getAtividadeArquivosById(Long id) {
        AtividadeArquivos atividade = findEntityById(id);
        return modelMapper.map(atividade, AtividadeArquivosResponseDto.class);
    }

    @Transactional
    public AtividadeArquivosResponseDto updateAtividadeArquivos(
            Long idAtividade, 
            AtividadeArquivosUpdateDto dto, 
            Long idUsuarioLogado
    ) {
        
        AtividadeArquivos atividade = findEntityById(idAtividade);
        
        checkProfessorOwnership(atividade.getTopico(), idUsuarioLogado);

        applyUpdateFromDto(atividade, dto); 
        
        AtividadeArquivos updatedAtividade = atividadeArquivosRepository.save(atividade);
        return modelMapper.map(updatedAtividade, AtividadeArquivosResponseDto.class);
    }

    @Transactional
    public void deleteAtividadeArquivos(Long id, Long idUsuarioLogado) {
        AtividadeArquivos atividade = findEntityById(id);
        
        checkProfessorOwnership(atividade.getTopico(), idUsuarioLogado);
        
        atividadeArquivosRepository.delete(atividade);
    }

    private AtividadeArquivos findEntityById(Long id) {
        return atividadeArquivosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(NOT_FOUND_MSG, id)));
    }
    
    private void checkProfessorOwnership(Topicos topico, Long idUsuarioLogado) {
        if (topico.getTurma().getProfessor().getIdUsuario() != idUsuarioLogado) {
            throw new AccessDeniedException("O professor logado não é o dono da turma deste tópico.");
        }
    }

    private void applyUpdateFromDto(AtividadeArquivos atividade, AtividadeArquivosUpdateDto dto) {
        dto.getTituloAtividade().ifPresent(atividade::setTituloAtividade);
        dto.getDescricaoAtividade().ifPresent(atividade::setDescricaoAtividade);
        dto.getDataInicioAtividade().ifPresent(atividade::setDataInicioAtividade);
        dto.getDataFechamentoAtividade().ifPresent(atividade::setDataFechamentoAtividade);
        dto.getStatusAtividade().ifPresent(atividade::setStatusAtividade);
        dto.getArquivosPermitidos().ifPresent(atividade::setArquivosPermitidos);

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