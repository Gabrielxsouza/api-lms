package br.ifsp.lms_api.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import br.ifsp.lms_api.model.Alternativas;
import br.ifsp.lms_api.model.Questionario;
import br.ifsp.lms_api.model.Questoes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.ifsp.lms_api.repository.QuestionarioRepository;
import br.ifsp.lms_api.dto.questionarioDto.QuestionarioRequestDto;
import br.ifsp.lms_api.dto.questionarioDto.QuestionarioResponseDto;

@Service
public class QuestionarioService {
    private final QuestionarioRepository questionarioRepository;
    private final ModelMapper modelMapper;

    public QuestionarioService(QuestionarioRepository questionarioRepository, ModelMapper modelMapper) {
        this.questionarioRepository = questionarioRepository;
        this.modelMapper = modelMapper;
    }

    public QuestionarioRequestDto createQuestionario(QuestionarioRequestDto questionarioRequestDto) {
        Questionario questionarioEntidade = modelMapper.map(questionarioRequestDto, Questionario.class);

        if (questionarioEntidade.getQuestoes() != null) {
            for (Questoes questao : questionarioEntidade.getQuestoes()) {
                questao.setQuestionario(questionarioEntidade);

                if (questao.getAlternativas() != null) {
                    for (Alternativas alternativa : questao.getAlternativas()) {
                        alternativa.setQuestoes(questao);
                    }
                }
            }
        }

        Questionario questionarioSalvo = questionarioRepository.save(questionarioEntidade);

        return modelMapper.map(questionarioSalvo, QuestionarioRequestDto.class);
    }

    public Page<QuestionarioResponseDto> getAllQuestionario(Pageable pageable) {
        return questionarioRepository.findAll(pageable).map(questionario -> modelMapper.map(questionario, QuestionarioResponseDto.class));
    }
}
