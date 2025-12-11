package br.ifsp.lms_api.integration;

import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioRequestDto;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LearningServiceClient {

    private final RestTemplate restTemplate;
    private final String BASE_URL = "http://localhost:8081/api/v1/atividades";

    public LearningServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public AtividadeQuestionarioResponseDto createQuestionario(AtividadeQuestionarioRequestDto dto) {
        return restTemplate.postForObject(BASE_URL + "/questionario", dto, AtividadeQuestionarioResponseDto.class);
    }
}
