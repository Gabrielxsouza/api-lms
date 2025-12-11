package br.ifsp.lms_api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.ifsp.lms_api.dto.page.PagedResponse;

import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioRequestDto;
import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioResponseDto;
import br.ifsp.lms_api.exception.LimiteTentativasException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.TentativaQuestionario;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.TentativaQuestionarioRepository;

@Service
public class TentativaQuestionarioService {

    private final AlunoRepository alunoRepository;
    private final TentativaQuestionarioRepository tentativaQuestionarioRepository;
    private final br.ifsp.lms_api.integration.LearningServiceClient learningServiceClient;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public TentativaQuestionarioService(AlunoRepository alunoRepository,
            TentativaQuestionarioRepository tentativaQuestionarioRepository,
            br.ifsp.lms_api.integration.LearningServiceClient learningServiceClient,
            ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper) {
        this.alunoRepository = alunoRepository;
        this.tentativaQuestionarioRepository = tentativaQuestionarioRepository;
        this.learningServiceClient = learningServiceClient;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }

    @Transactional
    public TentativaQuestionarioResponseDto createTentativaQuestionario(TentativaQuestionarioRequestDto dto,
            Long idAlunoLogado) {

        dto.setIdAluno(idAlunoLogado);

        br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto atividadeDto = learningServiceClient
                .getAtividadeById(dto.getIdQuestionario());

        br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto questionario = null;
        if (atividadeDto instanceof br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto) {
            questionario = (br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto) atividadeDto;
        } else {
            throw new RuntimeException("Atividade não é um Questionário com ID: " + dto.getIdQuestionario());
        }

        Aluno aluno = alunoRepository.findById(dto.getIdAluno())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + dto.getIdAluno()));

        List<TentativaQuestionario> tentativasAnteriores = tentativaQuestionarioRepository
                .findByIdAtividadeAndAluno_Id(dto.getIdQuestionario(), dto.getIdAluno());

        if (tentativasAnteriores.size() >= questionario.getNumeroTentativas()) {
            throw new LimiteTentativasException("Limite de tentativas atingido.");
        }

        TentativaQuestionario novaTentativa = new TentativaQuestionario();
        novaTentativa.setIdAtividade(questionario.getIdAtividade()); // Store ID
        novaTentativa.setAluno(aluno);
        novaTentativa.setRespostas(dto.getRespostas());
        novaTentativa.setNumeroDaTentativa(tentativasAnteriores.size() + 1);
        novaTentativa.setDataEnvio(LocalDateTime.now());
        novaTentativa.setIdTentativaQuestionario(null);

        Double nota = calcularNota(novaTentativa, questionario);
        novaTentativa.setNota(nota);

        TentativaQuestionario tentativaSalva = tentativaQuestionarioRepository.save(novaTentativa);

        TentativaQuestionarioResponseDto responseDto = modelMapper.map(tentativaSalva,
                TentativaQuestionarioResponseDto.class);

        responseDto.setIdQuestionario(tentativaSalva.getIdAtividade());
        responseDto.setIdAluno(tentativaSalva.getAluno().getIdUsuario());

        return responseDto;
    }

    public PagedResponse<TentativaQuestionarioResponseDto> getAllTentativasQuestionario(Pageable pageable) {
        Page<TentativaQuestionario> tentativas = tentativaQuestionarioRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(tentativas, TentativaQuestionarioResponseDto.class);
    }

    public PagedResponse<TentativaQuestionarioResponseDto> getTentativasQuestionarioByAlunoId(Long alunoId,
            Pageable pageable) {
        Page<TentativaQuestionario> tentativas = tentativaQuestionarioRepository.findByAluno_IdUsuario(alunoId,
                pageable);
        return pagedResponseMapper.toPagedResponse(tentativas, TentativaQuestionarioResponseDto.class);
    }

    public TentativaQuestionarioResponseDto deleteTentativaQuestionario(Long idTentativa) {
        TentativaQuestionario tentativa = tentativaQuestionarioRepository.findById(idTentativa)
                .orElseThrow(
                        () -> new RuntimeException("Tentativa de questionário nao encontrada com ID: " + idTentativa));
        tentativaQuestionarioRepository.delete(tentativa);
        return modelMapper.map(tentativa, TentativaQuestionarioResponseDto.class);
    }

    private Double calcularNota(TentativaQuestionario tentativa,
            br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto questionario) {

        List<br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto> todasQuestoes = questionario
                .getQuestoesQuestionario();

        if (todasQuestoes == null || todasQuestoes.isEmpty()) {
            return 0.0;
        }

        List<Long> idsRespostasDoAluno = tentativa.getRespostas();

        if (idsRespostasDoAluno == null || idsRespostasDoAluno.isEmpty()) {
            return 0.0;
        }

        Set<Long> idsAlternativasCorretas = todasQuestoes.stream()
                .flatMap(questao -> questao.getAlternativas().stream())
                .filter(alternativa -> Boolean.TRUE.equals(alternativa.getAlternativaCorreta()))
                .map(alternativa -> alternativa.getIdAlternativa())
                .collect(Collectors.toSet());

        int acertos = 0;
        for (Long idResposta : idsRespostasDoAluno) {
            if (idsAlternativasCorretas.contains(idResposta)) {
                acertos++;
            }
        }

        double nota = ((double) acertos / (double) todasQuestoes.size()) * 10.0;

        return nota;
    }
}