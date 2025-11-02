package br.ifsp.lms_api.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.repository.AtividadeQuestionarioRepository;
import br.ifsp.lms_api.repository.QuestoesRepository;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioRequestDto;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioUpdateDto;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.AtividadeQuestionario;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;
import br.ifsp.lms_api.exception.ResourceNotFoundException;

import org.springframework.data.domain.Pageable;


@Service
public class AtividadeQuestionarioService {
    private final AtividadeQuestionarioRepository atividadeQuestionarioRepository;
    private final QuestoesRepository questoesRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    private static final String NOT_FOUND_MSG = "Atividade de Texto com ID %d não encontrada.";

    public AtividadeQuestionarioService(AtividadeQuestionarioRepository atividadeQuestionarioRepository, QuestoesRepository questoesRepository, ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper) {
        this.atividadeQuestionarioRepository = atividadeQuestionarioRepository;
        this.questoesRepository = questoesRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional
    public AtividadeQuestionarioResponseDto createAtividadeQuestionario(AtividadeQuestionarioRequestDto atividadeQuestionario) {
        AtividadeQuestionario atividadeQuestionarioEntity = modelMapper.map(atividadeQuestionario, AtividadeQuestionario.class);
        atividadeQuestionarioEntity = atividadeQuestionarioRepository.save(atividadeQuestionarioEntity);
        return modelMapper.map(atividadeQuestionarioEntity, AtividadeQuestionarioResponseDto.class);
    }

@Transactional(readOnly = true)
public PagedResponse<AtividadeQuestionarioResponseDto> getAllAtividadesQuestionario(Pageable pageable) {
    Page<AtividadeQuestionario> atividadesPage = atividadeQuestionarioRepository.findAll(pageable);

    // Map do Page direto para DTO, mantém a paginação
    Page<AtividadeQuestionarioResponseDto> dtoPage = atividadesPage.map(atividade -> {
        AtividadeQuestionarioResponseDto dto = modelMapper.map(atividade, AtividadeQuestionarioResponseDto.class);

        if (atividade.getQuestoes() != null) {
            List<QuestoesResponseDto> questoesDto = atividade.getQuestoes().stream()
                .map(questao -> modelMapper.map(questao, QuestoesResponseDto.class))
                .toList();
            dto.setQuestoesQuestionario(questoesDto);
        }

        return dto;
    });

    return pagedResponseMapper.toPagedResponse(dtoPage, AtividadeQuestionarioResponseDto.class);
}


    @Transactional(readOnly = true)
    public AtividadeQuestionarioResponseDto getAtividadeQuestionarioById(Long id) {
        return modelMapper.map(atividadeQuestionarioRepository.findById(id), AtividadeQuestionarioResponseDto.class);
    }

    @Transactional
    public AtividadeQuestionarioResponseDto updateAtividadeQuestionario(Long id, AtividadeQuestionarioUpdateDto atividadeQuestionarioUpdateDto) {
        AtividadeQuestionario atividadeQuestionario = findEntityById(id);

        applyUpdateFromDto(atividadeQuestionario, atividadeQuestionarioUpdateDto);

        return modelMapper.map(atividadeQuestionarioRepository.save(atividadeQuestionario), AtividadeQuestionarioResponseDto.class);
    }


   @Transactional
    public AtividadeQuestionarioResponseDto adicionarQuestoes(Long idQuestionario, List<Long> idsDasQuestoes) {
        AtividadeQuestionario questionario = atividadeQuestionarioRepository.findById(idQuestionario)
                .orElseThrow(() -> new RuntimeException("Questionário não encontrado com ID: " + idQuestionario));


        List<Questoes> questoesParaAdicionar = questoesRepository.findAllById(idsDasQuestoes);

        if (questoesParaAdicionar.isEmpty()) {
            throw new RuntimeException("Nenhuma questão válida encontrada com os IDs fornecidos.");
        }

        questionario.getQuestoes().addAll(questoesParaAdicionar);

        AtividadeQuestionario entidadeSalva = atividadeQuestionarioRepository.save(questionario);

        AtividadeQuestionarioResponseDto dto = modelMapper.map(entidadeSalva, AtividadeQuestionarioResponseDto.class);

        dto.setQuestoesQuestionario(
            entidadeSalva.getQuestoes()
                .stream()
                .map(q -> modelMapper.map(q, QuestoesResponseDto.class))
                .collect(Collectors.toList())
        );

        return dto;
    }




    @Transactional
    public AtividadeQuestionarioResponseDto removerQuestoes(Long idQuestionario, List<Long> idsDasQuestoes) {

        AtividadeQuestionario questionario = atividadeQuestionarioRepository.findById(idQuestionario)
                .orElseThrow(() -> new RuntimeException("Questionário não encontrado com ID: " + idQuestionario));


        List<Questoes> questoesParaRemover = questoesRepository.findAllById(idsDasQuestoes);

        if (questoesParaRemover.isEmpty()) {
            throw new RuntimeException("Nenhuma questão válida encontrada com os IDs fornecidos.");
        }

        questionario.getQuestoes().removeAll(questoesParaRemover);

        AtividadeQuestionario questionarioSalvo = atividadeQuestionarioRepository.save(questionario);

        Hibernate.initialize(questionarioSalvo.getQuestoes());

        return modelMapper.map(questionarioSalvo, AtividadeQuestionarioResponseDto.class);
    }


    public AtividadeQuestionarioResponseDto removerQuestoes(long idQuestionario) {
        AtividadeQuestionario questionario = atividadeQuestionarioRepository.findById(idQuestionario)
                .orElseThrow(() -> new RuntimeException("Questionário nao encontrado com ID: " + idQuestionario));
        questionario.getQuestoes().clear();
        return modelMapper.map(atividadeQuestionarioRepository.save(questionario), AtividadeQuestionarioResponseDto.class);


    }



    private AtividadeQuestionario findEntityById(Long id) {
        return atividadeQuestionarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(NOT_FOUND_MSG, id)));
    }

    public void applyUpdateFromDto(AtividadeQuestionario atividade, AtividadeQuestionarioUpdateDto dto) {
        dto.getTituloAtividade().ifPresent(atividade::setTituloAtividade);
        dto.getDescricaoAtividade().ifPresent(atividade::setDescricaoAtividade);
        dto.getDataInicioAtividade().ifPresent(atividade::setDataInicioAtividade);
        dto.getDataFechamentoAtividade().ifPresent(atividade::setDataFechamentoAtividade);
        dto.getStatusAtividade().ifPresent(atividade::setStatusAtividade);
        dto.getNumeroTentativas().ifPresent(atividade::setNumeroTentativas);
        dto.getDuracaoQuestionario().ifPresent(atividade::setDuracaoQuestionario);
    }
}
