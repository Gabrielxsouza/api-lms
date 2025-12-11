package br.ifsp.lms_api.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;
import br.ifsp.lms_api.model.Questoes;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(Questoes.class, QuestoesResponseDto.class);

        return modelMapper;
    }
}
