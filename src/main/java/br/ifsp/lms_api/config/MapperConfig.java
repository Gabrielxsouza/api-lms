package br.ifsp.lms_api.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.ifsp.lms_api.model.Atividade;
import br.ifsp.lms_api.model.AtividadeArquivos;
import br.ifsp.lms_api.model.AtividadeQuestionario;
import br.ifsp.lms_api.model.AtividadeTexto;

import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;

import br.ifsp.lms_api.model.Questoes;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;

@Configuration
public class MapperConfig {

@Bean
public ModelMapper modelMapper() {
ModelMapper modelMapper = new ModelMapper();

modelMapper.typeMap(Atividade.class, AtividadesResponseDto.class)
.include(AtividadeTexto.class, AtividadeTextoResponseDto.class)
.include(AtividadeArquivos.class, AtividadeArquivosResponseDto.class)
.include(AtividadeQuestionario.class, AtividadeQuestionarioResponseDto.class);

modelMapper.typeMap(AtividadeTexto.class, AtividadeTextoResponseDto.class);

modelMapper.typeMap(AtividadeArquivos.class, AtividadeArquivosResponseDto.class);


modelMapper.typeMap(AtividadeQuestionario.class, AtividadeQuestionarioResponseDto.class)
.addMappings(mapper -> mapper.map(
    src -> src.getQuestoes(),
    AtividadeQuestionarioResponseDto::setQuestoesQuestionario
));

modelMapper.typeMap(Questoes.class, QuestoesResponseDto.class);

return modelMapper;


}
}
