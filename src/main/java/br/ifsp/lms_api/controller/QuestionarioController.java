package br.ifsp.lms_api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.lms_api.dto.questionarioDto.QuestionarioRequestDto;
import br.ifsp.lms_api.dto.questionarioDto.QuestionarioResponseDto;
import br.ifsp.lms_api.service.QuestionarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/questionario")
@Tag(name = "Questionario", description = "Operações relacionadas ao questionário")
@Validated
public class QuestionarioController {
    private final QuestionarioService questionarioService;

    public QuestionarioController(QuestionarioService questionarioService) {
        this.questionarioService = questionarioService;
    }

    @Operation(summary = "Cria um questionário")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionarioRequestDto createQuestionario(@RequestBody QuestionarioRequestDto questionarioRequestDto) {
        return questionarioService.createQuestionario(questionarioRequestDto);
    }

    @Operation(summary = "Listar todos os questionários")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<QuestionarioResponseDto> getAllQuestionario(Pageable pageable) {
        return questionarioService.getAllQuestionario(pageable);
    }

    
}
