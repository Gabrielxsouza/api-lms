package br.ifsp.lms_api.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 1. IMPORTAR OS DTOS DE REQUEST
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;

// ... (seus imports de DTO de Resposta)
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto;
import br.ifsp.lms_api.dto.questoesDto.QuestoesResponseDto;

// ... (seus imports de Entidades)
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

        // --- BLOCO 1: ENTIDADE -> DTO DE RESPOSTA (O SEU CÓDIGO, CORRIGIDO) ---
        
        // Mapeamento PAI: Define que 'tags' deve ser mapeado para TODOS os filhos
        modelMapper.typeMap(Atividade.class, AtividadesResponseDto.class)
            .include(AtividadeTexto.class, AtividadeTextoResponseDto.class)
            .include(AtividadeArquivos.class, AtividadeArquivosResponseDto.class)
            .include(AtividadeQuestionario.class, AtividadeQuestionarioResponseDto.class)
            .addMappings(mapper -> mapper.map(
                src -> src.getTags(), // Pega o Set<Tag> da Entidade
                AtividadesResponseDto::setTags // Seta o List<TagResponseDto> no DTO
            ));
        
        // Mapeamento FILHO (Texto): Garante que ele também mapeie as 'tags'
        modelMapper.typeMap(AtividadeTexto.class, AtividadeTextoResponseDto.class)
            .addMappings(mapper -> {
                mapper.map(src -> src.getTags(), AtividadesResponseDto::setTags); // Seta no DTO pai
                mapper.map(src -> src.getNumeroMaximoCaracteres(), AtividadeTextoResponseDto::setNumeroMaximoCaracteres);
            });

        // Mapeamento FILHO (Arquivos): Combina os dois mapeamentos
        modelMapper.typeMap(AtividadeArquivos.class, AtividadeArquivosResponseDto.class)
            .addMappings(mapper -> {
                mapper.map(src -> src.getArquivosPermitidos(), AtividadeArquivosResponseDto::setArquivosPermitidos);
                mapper.map(src -> src.getTags(), AtividadesResponseDto::setTags); // Adiciona 'tags'
            });

        // Mapeamento FILHO (Questionário): Combina os mapeamentos
        modelMapper.typeMap(AtividadeQuestionario.class, AtividadeQuestionarioResponseDto.class)
            .addMappings(mapper -> {
                mapper.map(src -> src.getQuestoes(), AtividadeQuestionarioResponseDto::setQuestoesQuestionario);
                mapper.map(src -> src.getNumeroTentativas(), AtividadeQuestionarioResponseDto::setNumeroTentativas);
                mapper.map(src -> src.getDuracaoQuestionario(), AtividadeQuestionarioResponseDto::setDuracaoQuestionario);
                mapper.map(src -> src.getTags(), AtividadesResponseDto::setTags); // Adiciona 'tags'
            });

        modelMapper.typeMap(Questoes.class, QuestoesResponseDto.class);

        // --- FIM DO BLOCO 1 ---


        // --- BLOCO 2: DTO DE REQUEST -> ENTIDADE (O BLOCO QUE FALTAVA) ---
        // (Isso corrige o bug do "numeroMaximoCaracteres: 0")
        
        modelMapper.typeMap(AtividadeTextoRequestDto.class, AtividadeTexto.class);
        modelMapper.typeMap(AtividadeArquivosRequestDto.class, AtividadeArquivos.class);
        // modelMapper.typeMap(AtividadeQuestionarioRequestDto.class, AtividadeQuestionario.class); // (Adicionar quando pronto)

        // --- FIM DO BLOCO 2 ---

        return modelMapper;
    }
}