package br.ifsp.lms_api.service;

import br.ifsp.lms_api.model.Atividade;
import br.ifsp.lms_api.model.AtividadeArquivos;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Tag;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.model.Usuario;

import java.util.List;
import java.util.Optional;

import br.ifsp.lms_api.repository.AtividadeArquivosRepository;
import br.ifsp.lms_api.repository.TagRepository;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.AccessDeniedException;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;

import java.util.HashSet;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtividadeArquivosService {

    private final AtividadeArquivosRepository atividadeArquivosRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;
    private final TagRepository tagRepository;

    private static final String NOT_FOUND_MSG = "Atividade de Arquivos com ID %d não encontrada.";

    public AtividadeArquivosService(AtividadeArquivosRepository atividadeArquivosRepository,
                                    ModelMapper modelMapper,
                                    PagedResponseMapper pagedResponseMapper,
                                    TagRepository tagRepository) {
        this.atividadeArquivosRepository = atividadeArquivosRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public AtividadeArquivosResponseDto createAtividadeArquivos(AtividadeArquivosRequestDto dto) {
        AtividadeArquivos atividade = modelMapper.map(dto, AtividadeArquivos.class);

        atividade.setIdAtividade(null);

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
        
        // 1. Busca a atividade
        AtividadeArquivos atividade = atividadeArquivosRepository.findById(idAtividade)
            .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));

        // 2. VERIFICAÇÃO DE DONO
        Optional<Long> idProfessorDaAtividade = Optional.of(atividade)
            .map(Atividade::getTopico)
            .map(Topicos::getTurma)
            .map(Turma::getProfessor)
            .map(Professor::getIdUsuario);

        // Se o professor não for encontrado OU o ID dele for DIFERENTE do usuário logado...
        if (!idProfessorDaAtividade.isPresent() || !idProfessorDaAtividade.get().equals(idUsuarioLogado)) {
            // Lança a exceção de Acesso Negado (403)
            throw new AccessDeniedException("Acesso Negado. Você não é o professor desta turma.");
        }

        // 3. Se passou, ele é o dono. Pode atualizar.
        applyUpdateFromDto(atividade, dto); // <---- !! ADICIONE ESTA LINHA !!
        
        AtividadeArquivos updatedAtividade = atividadeArquivosRepository.save(atividade);
        return modelMapper.map(updatedAtividade, AtividadeArquivosResponseDto.class);
    }

    @Transactional
    public void deleteAtividadeArquivos(Long id) {
        AtividadeArquivos atividade = findEntityById(id);
        atividadeArquivosRepository.delete(atividade);
    }

    private AtividadeArquivos findEntityById(Long id) {
        return atividadeArquivosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(NOT_FOUND_MSG, id)));
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