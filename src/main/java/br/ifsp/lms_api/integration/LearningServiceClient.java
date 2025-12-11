package br.ifsp.lms_api.integration;

import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioRequestDto;
import br.ifsp.lms_api.dto.atividadeQuestionarioDto.AtividadeQuestionarioResponseDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoRequestDto;
import br.ifsp.lms_api.dto.atividadeTextoDto.AtividadeTextoResponseDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosRequestDto;
import br.ifsp.lms_api.dto.atividadeArquivosDto.AtividadeArquivosResponseDto;
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

    public AtividadeTextoResponseDto createTexto(AtividadeTextoRequestDto dto) {
        return restTemplate.postForObject(BASE_URL + "/texto", dto, AtividadeTextoResponseDto.class);
    }

    public AtividadeArquivosResponseDto createArquivos(AtividadeArquivosRequestDto dto) {
        return restTemplate.postForObject(BASE_URL + "/arquivos", dto, AtividadeArquivosResponseDto.class);
    }

    public br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto[] getAllAtividades() {
        return restTemplate.getForObject(BASE_URL, br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto[].class);
    }

    public br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto getAtividadeById(Long id) {
        return restTemplate.getForObject(BASE_URL + "/" + id,
                br.ifsp.lms_api.dto.atividadesDto.AtividadesResponseDto.class);
    }

    public void deleteAtividade(Long id) {
        restTemplate.delete(BASE_URL + "/" + id);
    }

    public AtividadeQuestionarioResponseDto updateQuestionario(Long id, AtividadeQuestionarioRequestDto dto) {
        restTemplate.put(BASE_URL + "/questionario/" + id, dto);
        // Put usually returns void or updated object. RestTemplate.put is void.
        // We might need to fetch it again or assume it's updated.
        // For simplicity returning null or fetching.
        return null;
    }

    public AtividadeTextoResponseDto updateTexto(Long id, AtividadeTextoRequestDto dto) {
        restTemplate.put(BASE_URL + "/texto/" + id, dto);
        return null;
    }

    public AtividadeArquivosResponseDto updateArquivos(Long id, AtividadeArquivosRequestDto dto) {
        restTemplate.put(BASE_URL + "/arquivos/" + id, dto);
        return null;
    }
}
