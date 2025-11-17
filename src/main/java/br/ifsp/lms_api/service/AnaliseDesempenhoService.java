package br.ifsp.lms_api.service;

import br.ifsp.lms_api.dto.analise.MaterialSugeridoDto;
import br.ifsp.lms_api.dto.analise.NotaTagAgregada;
import br.ifsp.lms_api.dto.analise.RelatorioDesempenhoResponseDto;
import br.ifsp.lms_api.dto.analise.TagDesempenhoDto;

import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.Aluno;
import br.ifsp.lms_api.model.MaterialDeAula;
import br.ifsp.lms_api.model.Matricula;
import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.model.Tag;
import br.ifsp.lms_api.model.TentativaArquivo;
import br.ifsp.lms_api.model.TentativaQuestionario;
import br.ifsp.lms_api.model.TentativaTexto;
import br.ifsp.lms_api.model.Topicos;
import br.ifsp.lms_api.model.Turma;
import br.ifsp.lms_api.model.Disciplina;
import br.ifsp.lms_api.repository.DisciplinaRepository; 
import jakarta.persistence.EntityNotFoundException;
import br.ifsp.lms_api.repository.TentativaArquivoRepository;
import br.ifsp.lms_api.repository.TentativaQuestionarioRepository;
import br.ifsp.lms_api.repository.TentativaTextoRepository;
import br.ifsp.lms_api.repository.TopicosRepository;
import br.ifsp.lms_api.repository.TurmaRepository;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;


@Service
public class AnaliseDesempenhoService {

    private final TentativaTextoRepository tentativaTextoRepo;
    private final TentativaArquivoRepository tentativaArquivoRepo;
    private final TentativaQuestionarioRepository tentativaQuestionarioRepo;
    private final TopicosRepository topicosRepo;
    private final TurmaRepository turmaRepo;
    private final DisciplinaRepository disciplinaRepo;

    private static final double LIMIAR_APROVACAO = 6.0;

    public AnaliseDesempenhoService(TentativaTextoRepository tentativaTextoRepo,
                                    TentativaArquivoRepository tentativaArquivoRepo,
                                    TentativaQuestionarioRepository tentativaQuestionarioRepo,
                                    TopicosRepository topicosRepo,
                                    TurmaRepository turmaRepo,
                                    DisciplinaRepository disciplinaRepo) {
        this.tentativaTextoRepo = tentativaTextoRepo;
        this.tentativaArquivoRepo = tentativaArquivoRepo;
        this.tentativaQuestionarioRepo = tentativaQuestionarioRepo;
        this.topicosRepo = topicosRepo;
        this.turmaRepo = turmaRepo;
        this.disciplinaRepo = disciplinaRepo;
    }

    @Transactional(readOnly = true)
    public RelatorioDesempenhoResponseDto gerarRelatorioAluno(Long idAluno) {
        
        List<NotaTagAgregada> notasColetadas = new ArrayList<>();

        notasColetadas.addAll(coletarNotasDeAtividades(idAluno));
        notasColetadas.addAll(coletarNotasDeQuestionarios(idAluno));

        List<TagDesempenhoDto> desempenhoGeral = calcularMediasPorTag(notasColetadas); 

        List<TagDesempenhoDto> pontosFracos = filtrarPontosFracos(desempenhoGeral);

        List<MaterialSugeridoDto> sugestoes = buscarSugestoesDeEstudo(pontosFracos);

        RelatorioDesempenhoResponseDto relatorio = new RelatorioDesempenhoResponseDto();
        relatorio.setDesempenhoGeral(desempenhoGeral);
        relatorio.setPontosFracos(pontosFracos);
        relatorio.setSugestoesEstudo(sugestoes);
        
        return relatorio;
    }

    @Transactional(readOnly = true)
    public RelatorioDesempenhoResponseDto gerarRelatorioTurma(Long idTurma) {
        Turma turma = turmaRepo.findById(idTurma)
                .orElseThrow(() -> new EntityNotFoundException("Turma não encontrada"));

        List<Aluno> alunosDaTurma = turma.getMatriculas().stream()
                                        .map(Matricula::getAluno)
                                        .toList();

        List<NotaTagAgregada> notasColetadasDaTurma = new ArrayList<>();

        for (Aluno aluno : alunosDaTurma) {
            notasColetadasDaTurma.addAll(coletarNotasDeAtividades(aluno.getIdUsuario()));
            notasColetadasDaTurma.addAll(coletarNotasDeQuestionarios(aluno.getIdUsuario()));
        }

        List<TagDesempenhoDto> desempenhoGeral = calcularMediasPorTag(notasColetadasDaTurma);

        List<TagDesempenhoDto> pontosFracos = filtrarPontosFracos(desempenhoGeral);

        List<MaterialSugeridoDto> sugestoes = buscarSugestoesDeEstudo(pontosFracos);

        RelatorioDesempenhoResponseDto relatorio = new RelatorioDesempenhoResponseDto();
        relatorio.setDesempenhoGeral(desempenhoGeral);
        relatorio.setPontosFracos(pontosFracos);
        relatorio.setSugestoesEstudo(sugestoes);

        return relatorio;
    }

    @Transactional(readOnly = true)
    public RelatorioDesempenhoResponseDto gerarRelatorioDisciplina(Long idDisciplina) {

        Disciplina disciplina = disciplinaRepo.findById(idDisciplina)
                .orElseThrow(() -> new EntityNotFoundException("Disciplina não encontrada"));

        List<NotaTagAgregada> notasColetadasDaDisciplina = new ArrayList<>();

        for (Turma turma : disciplina.getTurmas()) {
            
            for (Matricula matricula : turma.getMatriculas()) {
                Aluno aluno = matricula.getAluno();
                if (aluno == null) continue;

                notasColetadasDaDisciplina.addAll(coletarNotasDeAtividades(aluno.getIdUsuario()));
                notasColetadasDaDisciplina.addAll(coletarNotasDeQuestionarios(aluno.getIdUsuario()));
            }
        }

        List<TagDesempenhoDto> desempenhoGeral = calcularMediasPorTag(notasColetadasDaDisciplina);

        List<TagDesempenhoDto> pontosFracos = filtrarPontosFracos(desempenhoGeral);

        RelatorioDesempenhoResponseDto relatorio = new RelatorioDesempenhoResponseDto();
        relatorio.setDesempenhoGeral(desempenhoGeral);
        relatorio.setPontosFracos(pontosFracos);
        relatorio.setSugestoesEstudo(new ArrayList<>());
        
        return relatorio;
    }

    private List<NotaTagAgregada> coletarNotasDeAtividades(Long idAluno) {
        List<NotaTagAgregada> notasAgregadas = new ArrayList<>();

        List<TentativaTexto> tentativasTexto = tentativaTextoRepo.findByAluno_IdUsuario(idAluno, Pageable.unpaged()).getContent();
        
        for (TentativaTexto tentativa : tentativasTexto) {
            if (tentativa.getNota() != null) {
                double nota = tentativa.getNota();
                Set<Tag> tags = tentativa.getAtividadeTexto().getTags();
                for (Tag tag : tags) {
                    notasAgregadas.add(new NotaTagAgregada(tag.getNome(), nota));
                }
            }
        }

        List<TentativaArquivo> tentativasArquivo = tentativaArquivoRepo.findByAluno_IdUsuario(idAluno, Pageable.unpaged()).getContent();
        
        for (TentativaArquivo tentativa : tentativasArquivo) {
            if (tentativa.getNota() != null) {
                double nota = tentativa.getNota();
                Set<Tag> tags = tentativa.getAtividadeArquivo().getTags();
                for (Tag tag : tags) {
                    notasAgregadas.add(new NotaTagAgregada(tag.getNome(), nota));
                }
            }
        }
        
        return notasAgregadas;
    }

    private List<NotaTagAgregada> coletarNotasDeQuestionarios(Long idAluno) {
        List<NotaTagAgregada> notasAgregadas = new ArrayList<>();

        List<TentativaQuestionario> tentativasQuest = tentativaQuestionarioRepo.findByAluno_IdUsuario(idAluno, Pageable.unpaged()).getContent();

        for (TentativaQuestionario tentativa : tentativasQuest) {
            List<Questoes> questoesDoQuestionario = tentativa.getAtividadeQuestionario().getQuestoes();
            List<Long> respostasDoAluno = tentativa.getRespostas();

            for (Questoes questao : questoesDoQuestionario) {
                
                Long idAlternativaCorreta = null;
                for (Alternativas alt : questao.getAlternativas()) {
                    if (alt.getAlternativaCorreta()) {
                        idAlternativaCorreta = alt.getIdAlternativa();
                        break;
                    }
                }
                
                if (idAlternativaCorreta == null) continue;

                double notaDaQuestao = 0.0; 
                if (respostasDoAluno.contains(idAlternativaCorreta)) {
                    notaDaQuestao = 10.0; 
                }

                Set<Tag> tagsDaQuestao = questao.getTags();
                for (Tag tag : tagsDaQuestao) {
                    notasAgregadas.add(new NotaTagAgregada(tag.getNome(), notaDaQuestao));
                }
            }
        }
        
        return notasAgregadas;
    }

    private List<TagDesempenhoDto> calcularMediasPorTag(List<NotaTagAgregada> notasColetadas) {

        Map<String, List<NotaTagAgregada>> notasAgrupadas = notasColetadas.stream()
                .collect(Collectors.groupingBy(NotaTagAgregada::getNomeTag));

        List<TagDesempenhoDto> desempenhoGeral = new ArrayList<>();

        for (Map.Entry<String, List<NotaTagAgregada>> entry : notasAgrupadas.entrySet()) {
            String nomeTag = entry.getKey();
            List<NotaTagAgregada> notasDaTag = entry.getValue();

            double media = notasDaTag.stream()
                    .mapToDouble(NotaTagAgregada::getNota)
                    .average()
                    .orElse(0.0);

            TagDesempenhoDto dto = new TagDesempenhoDto();
            dto.setNomeTag(nomeTag);
            dto.setTotalAvaliacoes(notasDaTag.size());
            dto.setMediaNota(media);

            desempenhoGeral.add(dto);
        }

        return desempenhoGeral;
    }

    private List<TagDesempenhoDto> filtrarPontosFracos(List<TagDesempenhoDto> desempenhoGeral) {

        return desempenhoGeral.stream()
                .filter(tag -> tag.getMediaNota() < LIMIAR_APROVACAO)
                .toList();
    }

    private List<MaterialSugeridoDto> buscarSugestoesDeEstudo(List<TagDesempenhoDto> pontosFracos) {

        Set<String> nomesTagsFracas = pontosFracos.stream()
                .map(TagDesempenhoDto::getNomeTag)
                .collect(Collectors.toSet());

        if (nomesTagsFracas.isEmpty()) {
            return new ArrayList<>();
        }

        List<Topicos> topicosSugeridos = topicosRepo.findByTags_NomeIn(nomesTagsFracas);

        Set<MaterialSugeridoDto> sugestoesUnicas = new HashSet<>();

        for (Topicos topico : topicosSugeridos) {
            for (MaterialDeAula material : topico.getMateriaisDeAula()) {

                MaterialSugeridoDto dto = new MaterialSugeridoDto();
                dto.setNomeTopicoRelacionado(topico.getTituloTopico());
                dto.setNomeMaterial(material.getNomeArquivo());
                dto.setUrlMaterial(material.getUrlArquivo());

                sugestoesUnicas.add(dto);
            }
        }

        return new ArrayList<>(sugestoesUnicas);
    }
}