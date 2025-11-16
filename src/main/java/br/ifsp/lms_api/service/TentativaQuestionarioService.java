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
import br.ifsp.lms_api.model.AtividadeQuestionario;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.model.TentativaQuestionario;
import br.ifsp.lms_api.repository.AlunoRepository;
import br.ifsp.lms_api.repository.AtividadeQuestionarioRepository; 
import br.ifsp.lms_api.repository.TentativaQuestionarioRepository;



@Service
public class TentativaQuestionarioService {

    private final AlunoRepository alunoRepository;
    private final TentativaQuestionarioRepository tentativaQuestionarioRepository;
    private final AtividadeQuestionarioRepository questionarioRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    public TentativaQuestionarioService(AlunoRepository alunoRepository,
            TentativaQuestionarioRepository tentativaQuestionarioRepository, AtividadeQuestionarioRepository questionarioRepository,
             ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper) {
        this.alunoRepository = alunoRepository;
        this.tentativaQuestionarioRepository = tentativaQuestionarioRepository;
        this.questionarioRepository = questionarioRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }


    @Transactional
    public TentativaQuestionarioResponseDto createTentativaQuestionario(TentativaQuestionarioRequestDto dto) {

        AtividadeQuestionario questionario = questionarioRepository.findById(dto.getIdQuestionario())
                .orElseThrow(() -> new RuntimeException("Questionário não encontrado com ID: " + dto.getIdQuestionario()));

        Aluno aluno = alunoRepository.findById(dto.getIdAluno())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + dto.getIdAluno()));

        List<TentativaQuestionario> tentativasAnteriores = tentativaQuestionarioRepository
                .findByAtividadeQuestionario_IdAndAluno_Id(dto.getIdQuestionario(), dto.getIdAluno());
        
        if (tentativasAnteriores.size() >= questionario.getNumeroTentativas()) {
            throw new LimiteTentativasException("Limite de tentativas atingido.");
        }

        TentativaQuestionario novaTentativa = new TentativaQuestionario();
        novaTentativa.setAtividadeQuestionario(questionario);
        novaTentativa.setAluno(aluno);
        novaTentativa.setRespostas(dto.getRespostas());
        novaTentativa.setNumeroDaTentativa(tentativasAnteriores.size() + 1);
        novaTentativa.setDataEnvio(LocalDateTime.now());
        novaTentativa.setIdTentativaQuestionario(null); 

        Double nota = calcularNota(novaTentativa);
        novaTentativa.setNota(nota);

        TentativaQuestionario tentativaSalva = tentativaQuestionarioRepository.save(novaTentativa);

        TentativaQuestionarioResponseDto responseDto = modelMapper.map(tentativaSalva, TentativaQuestionarioResponseDto.class);

        responseDto.setIdQuestionario(tentativaSalva.getAtividadeQuestionario().getIdAtividade());
        responseDto.setIdAluno(tentativaSalva.getAluno().getIdUsuario()); 

        return responseDto;
    }


    public PagedResponse<TentativaQuestionarioResponseDto> getAllTentativasQuestionario(Pageable pageable) {
        Page<TentativaQuestionario> tentativas = tentativaQuestionarioRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(tentativas, TentativaQuestionarioResponseDto.class);
    }

    public PagedResponse<TentativaQuestionarioResponseDto> getTentativasQuestionarioByAlunoId(Long alunoId, Pageable pageable) {
        Page<TentativaQuestionario> tentativas = tentativaQuestionarioRepository.findByAluno_IdUsuario(alunoId, pageable);
        return pagedResponseMapper.toPagedResponse(tentativas, TentativaQuestionarioResponseDto.class);
    }





    private Double calcularNota(TentativaQuestionario tentativa) {

        AtividadeQuestionario questionario = tentativa.getAtividadeQuestionario();
        List<Questoes> todasQuestoes = questionario.getQuestoes();

        if (todasQuestoes == null || todasQuestoes.isEmpty()) {
            return 0.0;
        }

        List<Long> idsRespostasDoAluno = tentativa.getRespostas();

        if (idsRespostasDoAluno == null || idsRespostasDoAluno.isEmpty()) {
            return 0.0; 
        }

        Set<Long> idsAlternativasCorretas = todasQuestoes.stream()
                .flatMap(questao -> questao.getAlternativas().stream())
                .filter(alternativa -> alternativa.getAlternativaCorreta()) 
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