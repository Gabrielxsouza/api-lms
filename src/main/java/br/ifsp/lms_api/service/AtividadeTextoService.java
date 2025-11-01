package br.ifsp.lms_api.service;

import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.repository.AtividadeTextoRepository;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;

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

    private static final String NOT_FOUND_MSG = "Atividade de Texto com ID %d não encontrada.";

    public AtividadeTextoService(AtividadeTextoRepository atividadeTextoRepository, 
                                 ModelMapper modelMapper, 
                                 PagedResponseMapper pagedResponseMapper) {
        this.atividadeTextoRepository = atividadeTextoRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    // --- 2. Métodos Públicos (retornam DTOs) ---

    @Transactional
    public AtividadeTextoResponseDto createAtividadeTexto(AtividadeTextoRequestDto dto) {
        // Converte DTO -> Entidade
        AtividadeTexto atividade = modelMapper.map(dto, AtividadeTexto.class);
        // Salva
        AtividadeTexto savedAtividade = atividadeTextoRepository.save(atividade);
        // Converte Entidade Salva -> Response DTO
        return modelMapper.map(savedAtividade, AtividadeTextoResponseDto.class);
    }

    @Transactional(readOnly = true) // Boa prática para métodos de leitura
    public PagedResponse<AtividadeTextoResponseDto> getAllAtividadesTexto(Pageable pageable) {
        // Busca a Página de Entidades
        Page<AtividadeTexto> atividadePage = atividadeTextoRepository.findAll(pageable);
        
        // Usa seu mapper para converter Page<Entidade> -> PagedResponse<DTO>
        return pagedResponseMapper.toPagedResponse(atividadePage, AtividadeTextoResponseDto.class);
    }

    @Transactional(readOnly = true)
    public AtividadeTextoResponseDto getAtividadeTextoById(Long id) {
        // Busca a entidade (usando helper)
        AtividadeTexto atividade = findEntityById(id);
        // Converte Entidade -> Response DTO
        return modelMapper.map(atividade, AtividadeTextoResponseDto.class);
    }

    @Transactional
    public AtividadeTextoResponseDto updateAtividadeTexto(Long id, AtividadeTextoUpdateDto dto) {
        // Busca a entidade
        AtividadeTexto atividade = findEntityById(id);
        // Aplica atualizações parciais
        applyUpdateFromDto(atividade, dto);
        // Salva
        AtividadeTexto updatedAtividade = atividadeTextoRepository.save(atividade);
        // Converte Entidade Atualizada -> Response DTO
        return modelMapper.map(updatedAtividade, AtividadeTextoResponseDto.class);
    }

    @Transactional
    public void deleteAtividadeTexto(Long id) {
        // Busca a entidade (ou lança exceção)
        AtividadeTexto atividade = findEntityById(id);
        // Deleta
        atividadeTextoRepository.delete(atividade);
    }

    // --- 3. Métodos Auxiliares (privados) ---

    /**
     * Busca a ENTIDADE no repositório. Lança exceção se não encontrar.
     * Usado internamente pelos métodos de update, delete e get.
     */
    private AtividadeTexto findEntityById(Long id) {
        return atividadeTextoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(NOT_FOUND_MSG, id)));
    }

    /**
     * Aplica as atualizações parciais do DTO de Update na Entidade.
     */
    private void applyUpdateFromDto(AtividadeTexto atividade, AtividadeTextoUpdateDto dto) {
        dto.getTituloAtividade().ifPresent(atividade::setTituloAtividade);
        dto.getDescricaoAtividade().ifPresent(atividade::setDescricaoAtividade);
        dto.getDataInicioAtividade().ifPresent(atividade::setDataInicioAtividade);
        dto.getDataFechamentoAtividade().ifPresent(atividade::setDataFechamentoAtividade);
        dto.getStatusAtividade().ifPresent(atividade::setStatusAtividade);
        dto.getNumeroMaximoCaracteres().ifPresent(atividade::setNumeroMaximoCaracteres);
    }
}