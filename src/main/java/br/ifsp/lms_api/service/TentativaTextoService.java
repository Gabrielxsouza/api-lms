package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoRequestDto;
import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoResponseDto;
import br.ifsp.lms_api.dto.TentativaTextoDto.TentativaTextoUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.AccessDeniedException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.model.TentativaTexto;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.AtividadeTextoRepository; 
import br.ifsp.lms_api.repository.TentativaTextoRepository;
import jakarta.persistence.EntityNotFoundException;


import jakarta.transaction.Transactional;

@Service
public class TentativaTextoService {
    private final TentativaTextoRepository tentativaTextoRepository;
    private final ModelMapper mapper;
    private final PagedResponseMapper pagedResponseMapper;

    private final AlunoRepository alunoRepository;
    private final AtividadeTextoRepository atividadeTextoRepository;

    public TentativaTextoService(TentativaTextoRepository tentativaTextoRepository,
            ModelMapper mapper, PagedResponseMapper pagedResponseMapper, AlunoRepository alunoRepository,
            AtividadeTextoRepository atividadeTextoRepository) {
        this.tentativaTextoRepository = tentativaTextoRepository;
        this.mapper = mapper;
        this.pagedResponseMapper = pagedResponseMapper;
        this.alunoRepository = alunoRepository;
        this.atividadeTextoRepository = atividadeTextoRepository;
    }

    @Transactional
    public PagedResponse<TentativaTextoResponseDto> getAllTentativasTexto(Pageable pageable) {

        Page<TentativaTexto> tentativaPage = tentativaTextoRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(tentativaPage, TentativaTextoResponseDto.class);
    }

    @Transactional
    public TentativaTextoResponseDto createTentativaTexto(
            TentativaTextoRequestDto tentativaRequest,
            Long idAlunoLogado, 
            Long idAtividade) {

        Aluno aluno = alunoRepository.findById(idAlunoLogado)
                .orElseThrow(() -> new EntityNotFoundException("Aluno não encontrado"));
        
        AtividadeTexto atividade = atividadeTextoRepository.findById(idAtividade)
                .orElseThrow(() -> new EntityNotFoundException("Atividade de Texto não encontrada"));

      
        TentativaTexto novaTentativa = new TentativaTexto();
        
        
        novaTentativa.setAluno(aluno); 
        novaTentativa.setAtividadeTexto(atividade); 
        novaTentativa.setTextoResposta(tentativaRequest.getTextoResposta()); 


        TentativaTexto tentativaSalva = tentativaTextoRepository.save(novaTentativa);

        return mapper.map(tentativaSalva, TentativaTextoResponseDto.class);
    }


    public TentativaTextoResponseDto updateTentativaTextoProfessor(TentativaTextoUpdateDto tentativaUpdate, Long idTentativa) {

        TentativaTexto tentativa = tentativaTextoRepository.findById(idTentativa)
                .orElseThrow(() -> new EntityNotFoundException("Tentativa de Texto nao encontrada"));

        tentativaUpdate.getNota().ifPresent(tentativa::setNota);
        tentativaUpdate.getFeedback().ifPresent(tentativa::setFeedBack);

        TentativaTexto tentativaSalva = tentativaTextoRepository.save(tentativa);

        return mapper.map(tentativaSalva, TentativaTextoResponseDto.class);

    }


    public TentativaTextoResponseDto updateTentativaTextoAluno(
            TentativaTextoUpdateDto tentativaUpdate, 
            Long idTentativa, 
            Long idAlunoLogado) { 

        TentativaTexto tentativa = tentativaTextoRepository.findById(idTentativa)
                .orElseThrow(() -> new EntityNotFoundException("Tentativa de Texto nao encontrada"));

        if (!tentativa.getAluno().getIdUsuario().equals(idAlunoLogado)) {
            throw new AccessDeniedException("Você não tem permissão para editar a tentativa de outro aluno.");
        }

        if (tentativa.getNota() != null) {
            throw new AccessDeniedException("Não é possível editar uma tentativa que já foi avaliada.");
        }
        
        tentativaUpdate.getTextoResposta().ifPresent(tentativa::setTextoResposta);

        TentativaTexto tentativaSalva = tentativaTextoRepository.save(tentativa);

        return mapper.map(tentativaSalva, TentativaTextoResponseDto.class);
    }

    public TentativaTextoResponseDto deleteTentativaTexto(Long idTentativa) {
        TentativaTexto tentativa = tentativaTextoRepository.findById(idTentativa)
                .orElseThrow(() -> new EntityNotFoundException("Tentativa de Texto nao encontrada"));
        tentativaTextoRepository.delete(tentativa);
        return mapper.map(tentativa, TentativaTextoResponseDto.class);
    }
}