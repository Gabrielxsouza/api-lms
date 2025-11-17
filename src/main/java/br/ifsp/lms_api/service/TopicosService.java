package br.ifsp.lms_api.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ifsp.lms_api.dto.TopicosDto.TopicosRequestDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosResponseDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosUpdateDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.exception.ResourceNotFoundException;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Atividade;
import br.ifsp.lms_api.model.Professor;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.model.Usuario;
import br.ifsp.lms_api.model.Tag;
import br.ifsp.lms_api.repository.AtividadeRepository;
import br.ifsp.lms_api.repository.TagRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.repository.TurmaRepository;

@Service
public class TopicosService {
    private final TopicosRepository topicosRepository;
    private final TurmaRepository turmaRepository;
    private final AtividadeRepository atividadeRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;
    private final TagRepository tagRepository;
    private final AutentificacaoService autentificacaoService; 
    
    private static final PolicyFactory POLITICA_DE_CONTEUDO_SEGURO = new HtmlPolicyBuilder()
            .allowElements("p", "br", "h2", "h3", "h4", "h5", "h6")
            .allowElements("ul", "li", "ol")
            .allowCommonInlineFormattingElements()

            .allowElements("a")
            .allowAttributes("href").onElements("a")
            .requireRelsOnLinks("noopener", "nofollow")
            .allowStandardUrlProtocols()

            .allowElements("img")
            .allowAttributes("src", "alt").onElements("img")
            .allowStandardUrlProtocols()

            .toFactory();

    public TopicosService(TopicosRepository topicosRepository, TurmaRepository turmaRepository, AtividadeRepository atividadeRepository,
            ModelMapper modelMapper, PagedResponseMapper pagedResponseMapper, TagRepository tagRepository, AutentificacaoService autentificacaoService) {
        this.topicosRepository = topicosRepository;
        this.turmaRepository = turmaRepository;
        this.atividadeRepository = atividadeRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
        this.tagRepository = tagRepository;
        this.autentificacaoService = autentificacaoService;
    }

    @Transactional
    public TopicosResponseDto createTopico(TopicosRequestDto topicosRequest) {

        Turma turma = turmaRepository.findById(topicosRequest.getIdTurma())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Turma com ID " + topicosRequest.getIdTurma() + " não encontrada"));

        String htmlLimpo = segurancaConteudo(topicosRequest.getConteudoHtml());
        
        Topicos topico = new Topicos();
        topico.setTituloTopico(topicosRequest.getTituloTopico());
        topico.setConteudoHtml(htmlLimpo);
        topico.setTurma(turma);
        topico.setIdTopico(null);
        topico.setAtividades(new ArrayList<>());

        if (topicosRequest.getTagIds() != null && !topicosRequest.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(topicosRequest.getTagIds());
            topico.setTags(new HashSet<>(tags));
        }
        
        Topicos topicoSalvo = topicosRepository.save(topico);

        List<Long> idsAtividades = topicosRequest.getIdAtividade();

        if (idsAtividades != null && !idsAtividades.isEmpty()) {
            List<Atividade> atividadesParaAssociar = new ArrayList<>();

            for (Long idAtividade : idsAtividades) {
                Atividade atividade = atividadeRepository.findById(idAtividade)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Atividade com ID " + idAtividade + " nao encontrada"));

                atividade.setTopico(topicoSalvo);
                atividadesParaAssociar.add(atividade);
            }

            atividadeRepository.saveAll(atividadesParaAssociar);
            topicoSalvo.setAtividades(atividadesParaAssociar);
        }

        return modelMapper.map(topicoSalvo, TopicosResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TopicosResponseDto> getAllTopicos(Pageable pageable) {
            Page<Topicos> topicos = topicosRepository.findAll(pageable);
            return pagedResponseMapper.toPagedResponse(topicos, TopicosResponseDto.class);
        }

        @Transactional(readOnly = true)
        public TopicosResponseDto getTopicoById(Long id) {
        Topicos topico = topicosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topico com ID " + id + " nao encontrado"));

        Usuario usuarioLogado = autentificacaoService.getUsuarioLogado();
        Long idUsuarioLogado = usuarioLogado.getIdUsuario();

        Optional<Long> professorId = Optional.of(topico)
            .map(Topicos::getTurma)
            .map(Turma::getProfessor)
            .map(Professor::getIdUsuario);

        boolean isProfessor = professorId.isPresent() && professorId.get().equals(idUsuarioLogado);

        boolean isAlunoMatriculado = false;
        
        if (!isProfessor && topico.getTurma() != null && topico.getTurma().getMatriculas() != null) {
            isAlunoMatriculado = topico.getTurma().getMatriculas()
                .stream()
                .anyMatch(matricula -> 
                    matricula.getAluno() != null && 
                    matricula.getAluno().getIdUsuario().equals(idUsuarioLogado)
                );
        }
        if (!isProfessor && !isAlunoMatriculado) {
            throw new AccessDeniedException("Acesso Negado. Você não é o professor ou aluno desta turma.");
        }
        
        return modelMapper.map(topico, TopicosResponseDto.class);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TopicosResponseDto> getTopicosByIdTurma(Long idTurma, Pageable pageable) {
        Page<Topicos> topicos = topicosRepository.findByTurmaIdTurma(idTurma, pageable);


        return pagedResponseMapper.toPagedResponse(topicos, TopicosResponseDto.class);
    }

    @Transactional
    public TopicosResponseDto deleteTopico(Long id) {
        Topicos topico = topicosRepository.findById(id)

                .orElseThrow(() -> new ResourceNotFoundException("Topico com ID " + id + " nao encontrado"));

        Usuario usuarioLogado = autentificacaoService.getUsuarioLogado();
        Long idUsuarioLogado = usuarioLogado.getIdUsuario();

        if (topico.getTurma().getProfessor().getIdUsuario() != idUsuarioLogado) {
            throw new AccessDeniedException("Acesso negado");
        }
        topicosRepository.delete(topico);
        return modelMapper.map(topico, TopicosResponseDto.class);
    }

    @Transactional
    public TopicosResponseDto updateTopico(Long id, TopicosUpdateDto topicosUpdate) {

        Topicos topicoExistente = topicosRepository.findById(id)

                .orElseThrow(() -> new ResourceNotFoundException("Topico com ID " + id + " nao encontrado"));

        Usuario usuarioLogado = autentificacaoService.getUsuarioLogado();
        Long idUsuarioLogado = usuarioLogado.getIdUsuario();

        if (topicoExistente.getTurma().getProfessor().getIdUsuario() != idUsuarioLogado) {
            throw new AccessDeniedException("Acesso negado");
        }

        topicosUpdate.getTituloTopico().ifPresent(topicoExistente::setTituloTopico);

        topicosUpdate.getConteudoHtml().ifPresent(htmlSuja -> {

            String htmlLimpo = segurancaConteudo(htmlSuja);
            topicoExistente.setConteudoHtml(htmlLimpo);
        });

        topicosUpdate.getTagIds().ifPresent(tagIds -> {
            if (tagIds.isEmpty()) {
                topicoExistente.getTags().clear();
            } else {
                List<Tag> tags = tagRepository.findAllById(tagIds);
                topicoExistente.setTags(new HashSet<>(tags));
            }
        });

        Topicos topico = topicosRepository.save(topicoExistente);

        return modelMapper.map(topico, TopicosResponseDto.class);
    }

    public String segurancaConteudo(String conteudoHtml) {
        return POLITICA_DE_CONTEUDO_SEGURO.sanitize(conteudoHtml);
    }
}

