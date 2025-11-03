package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Imports do Sanitizador
import org.owasp.html.PolicyFactory;
import org.owasp.html.HtmlPolicyBuilder;

import br.ifsp.lms_api.dto.TopicosDto.TopicosRequestDto;
import br.ifsp.lms_api.dto.TopicosDto.TopicosResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.mapper.PagedResponseMapper;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma; // <-- IMPORTAR TURMA
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.repository.TurmaRepository; // <-- IMPORTAR TURMA REPOSITORY
import jakarta.persistence.EntityNotFoundException; // <-- Importar exceção

@Service
public class TopicosService {
    private final TopicosRepository topicosRepository;
    private final TurmaRepository turmaRepository; // <-- ADICIONAR
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;

    // Definição da Política de Sanitização (como discutimos)
    private static final PolicyFactory POLITICA_DE_CONTEUDO_SEGURO = new HtmlPolicyBuilder()
    .allowElements("p", "br", "h2", "h3", "h4", "h5", "h6")
    .allowElements("ul", "li", "ol")
    .allowCommonInlineFormattingElements() // <b>, <i>, <strong>, <em>

    // --- CORREÇÃO DE LINKS ---
    .allowElements("a")
    .allowAttributes("href").onElements("a")
    .requireRelsOnLinks("noopener", "nofollow")
    .allowStandardUrlProtocols() // <-- ESSA LINHA É A SOLUÇÃO (para <a>)

    // --- CORREÇÃO DE IMAGENS ---
    .allowElements("img")
    .allowAttributes("src", "alt").onElements("img")
    .allowStandardUrlProtocols() // <-- ADICIONE AQUI TAMBÉM (para <img>)

    .toFactory();

    // Construtor atualizado
    public TopicosService(TopicosRepository topicosRepository, 
                          TurmaRepository turmaRepository, // <-- ADICIONAR
                          ModelMapper modelMapper, 
                          PagedResponseMapper pagedResponseMapper) {
        this.topicosRepository = topicosRepository;
        this.turmaRepository = turmaRepository; // <-- ADICIONAR
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
    }


    /**
     * Método createTopico CORRIGIDO
     */
    public TopicosResponseDto createTopico(TopicosRequestDto topicosRequest) {
        
        // 1. Buscar a entidade Turma pelo ID
        Turma turma = turmaRepository.findById(topicosRequest.getIdTurma())
            .orElseThrow(() -> new EntityNotFoundException("Turma com ID " + topicosRequest.getIdTurma() + " não encontrada"));

        // 2. Sanitizar o HTML recebido
        String htmlLimpo = POLITICA_DE_CONTEUDO_SEGURO.sanitize(topicosRequest.getConteudoHtml());

        // 3. Mapear o DTO para a entidade (manualmente ou com ModelMapper)
        // O ModelMapper pode mapear o título. O resto fazemos manualmente.
        Topicos topico = modelMapper.map(topicosRequest, Topicos.class);
        
        // 4. Definir os campos complexos
        topico.setConteudoHtml(htmlLimpo); // Define o HTML limpo
        topico.setTurma(turma);             // Define a entidade Turma completa
        topico.setIdTopico(null);           // Garante que é uma nova entidade

        // 5. Salvar no banco
        topico = topicosRepository.save(topico);

        // 6. Mapear a entidade salva para o DTO de Resposta
        return modelMapper.map(topico, TopicosResponseDto.class);
    }

    public PagedResponse<TopicosResponseDto> getAllTopicos(Pageable pageable) {
        Page<Topicos> topicos = topicosRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(topicos, TopicosResponseDto.class);
    }

    public TopicosResponseDto getTopicoById(Long id) {
        Topicos topico = topicosRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Topico com ID " + id + " nao encontrado"));
        return modelMapper.map(topico, TopicosResponseDto.class);
    }
}