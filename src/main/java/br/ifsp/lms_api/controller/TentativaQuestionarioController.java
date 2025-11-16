package br.ifsp.lms_api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.dto.TagDto.TagResponseDto;
import br.ifsp.lms_api.dto.page.PagedResponse;
import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioRequestDto;
import br.ifsp.lms_api.dto.tentativaQuestionarioDto.TentativaQuestionarioResponseDto;
import br.ifsp.lms_api.service.TentativaQuestionarioService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@Validated
@RequestMapping("/tentativaQuestionario")
public class TentativaQuestionarioController {
    private final TentativaQuestionarioService tentativaQuestionarioService;

    public TentativaQuestionarioController(TentativaQuestionarioService tentativaQuestionarioService) {
        this.tentativaQuestionarioService = tentativaQuestionarioService;
    }

    @PostMapping
    public TentativaQuestionarioResponseDto createTentativaQuestionario(@Validated @RequestBody TentativaQuestionarioRequestDto tentativaQuestionario) {
        return tentativaQuestionarioService.createTentativaQuestionario(tentativaQuestionario);
    }

    

    
    
}
