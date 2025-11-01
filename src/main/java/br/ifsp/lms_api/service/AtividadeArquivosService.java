package br.ifsp.lms_api.service;

import br.ifsp.lms_api.model.AtividadeArquivos;
import br.ifsp.lms_api.repository.AtividadeArquivosRepository;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtividadeArquivosService {

    // --- 1. Dependências Injetadas via Construtor ---
    private final AtividadeArquivosRepository atividadeArquivosRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    private static final String NOT_FOUND_MSG = "Atividade de Arquivos com ID %d não encontrada.";

    public AtividadeArquivosService(AtividadeArquivosRepository atividadeArquivosRepository,
                                    ModelMapper modelMapper,
                                    PagedResponseMapper pagedResponseMapper) {
        this.atividadeArquivosRepository = atividadeArquivosRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    // --- 2. Métodos Públicos (retornam DTOs) ---

    @Transactional
    public AtividadeArquivosResponseDto createAtividadeArquivos(AtividadeArquivosRequestDto dto) {
        AtividadeArquivos atividade = modelMapper.map(dto, AtividadeArquivos.class);
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
    public AtividadeArquivosResponseDto updateAtividadeArquivos(Long id, AtividadeArquivosUpdateDto dto) {
        AtividadeArquivos atividade = findEntityById(id);
        applyUpdateFromDto(atividade, dto);
        AtividadeArquivos updatedAtividade = atividadeArquivosRepository.save(atividade);
        return modelMapper.map(updatedAtividade, AtividadeArquivosResponseDto.class);
    }

    @Transactional
    public void deleteAtividadeArquivos(Long id) {
        AtividadeArquivos atividade = findEntityById(id);
        atividadeArquivosRepository.delete(atividade);
    }

    // --- 3. Métodos Auxiliares (privados) ---

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
    }
}