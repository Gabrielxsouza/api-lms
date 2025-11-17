package br.ifsp.lms_api.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;

import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;

import br.ifsp.lms_api.model.Atividade;
import br.ifsp.lms_api.model.AtividadeArquivos;
import br.ifsp.lms_api.model.AtividadeQuestionario;
import br.ifsp.lms_api.model.AtividadeTexto;
import br.ifsp.lms_api.model.Questoes;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(Atividade.class, AtividadesResponseDto.class)
            .include(AtividadeTexto.class, AtividadeTextoResponseDto.class)
            .include(AtividadeArquivos.class, AtividadeArquivosResponseDto.class)
            .include(AtividadeQuestionario.class, AtividadeQuestionarioResponseDto.class)
            .addMappings(mapper -> mapper.map(
                src -> src.getTags(),
                AtividadesResponseDto::setTags
            ));

        modelMapper.typeMap(AtividadeTexto.class, AtividadeTextoResponseDto.class)
            .addMappings(mapper -> {
                mapper.map(src -> src.getTags(), AtividadesResponseDto::setTags);
                mapper.map(src -> src.getNumeroMaximoCaracteres(), AtividadeTextoResponseDto::setNumeroMaximoCaracteres);
            });

        modelMapper.typeMap(AtividadeArquivos.class, AtividadeArquivosResponseDto.class)
            .addMappings(mapper -> {
                mapper.map(src -> src.getArquivosPermitidos(), AtividadeArquivosResponseDto::setArquivosPermitidos);
                mapper.map(src -> src.getTags(), AtividadesResponseDto::setTags); 
            });

        modelMapper.typeMap(AtividadeQuestionario.class, AtividadeQuestionarioResponseDto.class)
            .addMappings(mapper -> {
                mapper.map(src -> src.getQuestoes(), AtividadeQuestionarioResponseDto::setQuestoesQuestionario);
                mapper.map(src -> src.getNumeroTentativas(), AtividadeQuestionarioResponseDto::setNumeroTentativas);
                mapper.map(src -> src.getDuracaoQuestionario(), AtividadeQuestionarioResponseDto::setDuracaoQuestionario);
                mapper.map(src -> src.getTags(), AtividadesResponseDto::setTags);
            });

        modelMapper.typeMap(Questoes.class, QuestoesResponseDto.class);

        modelMapper.typeMap(AtividadeTextoRequestDto.class, AtividadeTexto.class);
        modelMapper.typeMap(AtividadeArquivosRequestDto.class, AtividadeArquivos.class);

        return modelMapper;
    }
}