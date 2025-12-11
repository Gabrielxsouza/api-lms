package br.ifsp.lms.learning.infrastructure.mapper;

import br.ifsp.lms.learning.domain.model.*;
import br.ifsp.lms.learning.infrastructure.persistence.entity.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AtividadeMapper {

    public Atividade toDomain(AtividadeEntity entity) {
        if (entity instanceof AtividadeQuestionarioEntity) {
            AtividadeQuestionarioEntity qHelper = (AtividadeQuestionarioEntity) entity;
            return AtividadeQuestionario.builder()
                    .id(qHelper.getIdAtividade())
                    .titulo(qHelper.getTituloAtividade())
                    .descricao(qHelper.getDescricaoAtividade())
                    .dataInicio(qHelper.getDataInicioAtividade())
                    .dataFechamento(qHelper.getDataFechamentoAtividade())
                    .status(qHelper.getStatusAtividade())
                    .topicoId(qHelper.getTopicoId())
                    .tags(qHelper.getTags())
                    .duracaoMinutes(qHelper.getDuracaoQuestionario())
                    .tentativasPermitidas(qHelper.getNumeroTentativas())
                    .questoes(qHelper.getQuestoes().stream().map(this::toDomain).collect(Collectors.toList()))
                    .build();
        }
        // Handle other types later
        return null;
    }

    public AtividadeEntity toEntity(Atividade domain) {
        if (domain instanceof AtividadeQuestionario) {
            AtividadeQuestionario qDomain = (AtividadeQuestionario) domain;
            AtividadeQuestionarioEntity entity = new AtividadeQuestionarioEntity();
            entity.setIdAtividade(qDomain.getId());
            entity.setTituloAtividade(qDomain.getTitulo());
            entity.setDescricaoAtividade(qDomain.getDescricao());
            entity.setDataInicioAtividade(qDomain.getDataInicio());
            entity.setDataFechamentoAtividade(qDomain.getDataFechamento());
            entity.setStatusAtividade(qDomain.getStatus());
            entity.setTopicoId(qDomain.getTopicoId());
            entity.setTags(qDomain.getTags());
            entity.setDuracaoQuestionario(qDomain.getDuracaoMinutes());
            entity.setNumeroTentativas(qDomain.getTentativasPermitidas());
            if (qDomain.getQuestoes() != null) {
                entity.setQuestoes(qDomain.getQuestoes().stream().map(this::toEntity).collect(Collectors.toList()));
            }
            return entity;
        }
        return null;
    }

    private Questao toDomain(QuestaoEntity entity) {
        return Questao.builder()
                .id(entity.getIdQuestao())
                .enunciado(entity.getEnunciado())
                .alternativas(entity.getAlternativas().stream().map(this::toDomain).collect(Collectors.toList()))
                .build();
    }

    private QuestaoEntity toEntity(Questao domain) {
        QuestaoEntity entity = new QuestaoEntity();
        entity.setIdQuestao(domain.getId());
        entity.setEnunciado(domain.getEnunciado());
        if (domain.getAlternativas() != null) {
            entity.setAlternativas(domain.getAlternativas().stream().map(this::toEntity).collect(Collectors.toList()));
        }
        return entity;
    }

    private Alternativa toDomain(AlternativaEntity entity) {
        return Alternativa.builder()
                .id(entity.getIdAlternativa())
                .texto(entity.getAlternativa())
                .correta(entity.getAlternativaCorreta())
                .build();
    }

    private AlternativaEntity toEntity(Alternativa domain) {
        AlternativaEntity entity = new AlternativaEntity();
        entity.setIdAlternativa(domain.getId());
        entity.setAlternativa(domain.getTexto());
        entity.setAlternativaCorreta(domain.getCorreta());
        return entity;
    }
}
