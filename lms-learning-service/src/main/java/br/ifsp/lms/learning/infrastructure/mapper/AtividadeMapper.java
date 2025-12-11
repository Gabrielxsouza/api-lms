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
        } else if (entity instanceof AtividadeTextoEntity) {
            AtividadeTextoEntity tHelper = (AtividadeTextoEntity) entity;
            return AtividadeTexto.builder()
                    .id(tHelper.getIdAtividade())
                    .titulo(tHelper.getTituloAtividade())
                    .descricao(tHelper.getDescricaoAtividade())
                    .dataInicio(tHelper.getDataInicioAtividade())
                    .dataFechamento(tHelper.getDataFechamentoAtividade())
                    .status(tHelper.getStatusAtividade())
                    .topicoId(tHelper.getTopicoId())
                    .tags(tHelper.getTags())
                    .numeroMaximoCaracteres(tHelper.getNumeroMaximoCaracteres())
                    .build();
        } else if (entity instanceof AtividadeArquivosEntity) {
            AtividadeArquivosEntity aHelper = (AtividadeArquivosEntity) entity;
            return AtividadeArquivos.builder()
                    .id(aHelper.getIdAtividade())
                    .titulo(aHelper.getTituloAtividade())
                    .descricao(aHelper.getDescricaoAtividade())
                    .dataInicio(aHelper.getDataInicioAtividade())
                    .dataFechamento(aHelper.getDataFechamentoAtividade())
                    .status(aHelper.getStatusAtividade())
                    .topicoId(aHelper.getTopicoId())
                    .tags(aHelper.getTags())
                    .arquivosPermitidos(aHelper.getArquivosPermitidos())
                    .build();
        }
        return null;
    }

    public AtividadeEntity toEntity(Atividade domain) {
        if (domain instanceof AtividadeQuestionario) {
            AtividadeQuestionario qDomain = (AtividadeQuestionario) domain;
            AtividadeQuestionarioEntity entity = new AtividadeQuestionarioEntity();
            mapCommonAttributes(domain, entity);
            entity.setDuracaoQuestionario(qDomain.getDuracaoMinutes());
            entity.setNumeroTentativas(qDomain.getTentativasPermitidas());
            if (qDomain.getQuestoes() != null) {
                entity.setQuestoes(qDomain.getQuestoes().stream().map(this::toEntity).collect(Collectors.toList()));
            }
            return entity;
        } else if (domain instanceof AtividadeTexto) {
            AtividadeTexto tDomain = (AtividadeTexto) domain;
            AtividadeTextoEntity entity = new AtividadeTextoEntity();
            mapCommonAttributes(domain, entity);
            entity.setNumeroMaximoCaracteres(tDomain.getNumeroMaximoCaracteres());
            return entity;
        } else if (domain instanceof AtividadeArquivos) {
            AtividadeArquivos aDomain = (AtividadeArquivos) domain;
            AtividadeArquivosEntity entity = new AtividadeArquivosEntity();
            mapCommonAttributes(domain, entity);
            entity.setArquivosPermitidos(aDomain.getArquivosPermitidos());
            return entity;
        }
        return null;
    }

    private void mapCommonAttributes(Atividade domain, AtividadeEntity entity) {
        entity.setIdAtividade(domain.getId());
        entity.setTituloAtividade(domain.getTitulo());
        entity.setDescricaoAtividade(domain.getDescricao());
        entity.setDataInicioAtividade(domain.getDataInicio());
        entity.setDataFechamentoAtividade(domain.getDataFechamento());
        entity.setStatusAtividade(domain.getStatus());
        entity.setTopicoId(domain.getTopicoId());
        entity.setTags(domain.getTags());
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
